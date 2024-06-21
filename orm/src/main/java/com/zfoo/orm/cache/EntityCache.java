/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.cache;

import com.mongodb.WriteConcern;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.zfoo.event.manager.EventBus;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.cache.persister.IOrmPersister;
import com.zfoo.orm.cache.persister.PNode;
import com.zfoo.orm.model.EntityDef;
import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.query.Page;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.util.LazyCache;
import com.zfoo.scheduler.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author godotg
 */
public class EntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> implements IEntityCache<PK, E> {

    private static final Logger logger = LoggerFactory.getLogger(EntityCache.class);

    private static final int BATCH_SIZE = 512;

    private final EntityDef entityDef;

    private final LazyCache<PK, PNode<E>> cache;

    public EntityCache(EntityDef entityDef) {
        var removeCallback = new BiConsumer<Pair<PK, PNode<E>>, LazyCache.RemovalCause>() {
            @Override
            public void accept(Pair<PK, PNode<E>> pair, LazyCache.RemovalCause removalCause) {
                if (removalCause == LazyCache.RemovalCause.EXPLICIT) {
                    return;
                }

                var pk = pair.getKey();
                var pnode = pair.getValue();
                if (pnode.getWriteToDbTime() == pnode.getModifiedTime()) {
                    return;
                }

                // 缓存失效之前，将数据写入数据库
                var entity = pnode.getEntity();
                @SuppressWarnings("unchecked")
                var entityClass = (Class<E>) entityDef.getClazz();
                EventBus.asyncExecute(entityClass.hashCode(), new Runnable() {
                    @Override
                    public void run() {
                        var collection = OrmContext.getOrmManager().getCollection(entityClass);

                        var version = entity.gvs();
                        entity.svs(version + 1);

                        var filter = entity.gvs() > 0
                                ? Filters.and(Filters.eq("_id", entity.id()), Filters.eq("vs", version))
                                : Filters.eq("_id", entity.id());
                        var result = collection.replaceOne(filter, entity);
                        if (result.getModifiedCount() <= 0) {
                            // 移除缓存时，更新数据库中的实体文档异常
                            logger.error("onRemoval(): update entity to db failed when remove [{}] [pk:{}] by [removalCause:{}]", entityClass.getSimpleName(), entity.id(), removalCause);
                        }
                    }
                });
            }
        };
        var expireCheckIntervalMillis = Math.max(3 * TimeUtils.MILLIS_PER_SECOND, entityDef.getExpireMillisecond() / 10);
        this.entityDef = entityDef;
        this.cache = new LazyCache<>(entityDef.getCacheSize(), entityDef.getExpireMillisecond(), expireCheckIntervalMillis, removeCallback);

        if (CollectionUtils.isNotEmpty(entityDef.getIndexDefMap())) {
            // indexMap
        }

        if (CollectionUtils.isNotEmpty(entityDef.getIndexTextDefMap())) {
            // indexText
        }

        var persisterDef = entityDef.getPersisterStrategy();
        IOrmPersister persister = persisterDef.getType().createPersister(entityDef, this);
        persister.start();
    }


    @Override
    public E load(PK pk) {
        AssertionUtils.notNull(pk);
        var pnode = cache.get(pk);
        if (pnode != null) {
            return pnode.getEntity();
        }

        @SuppressWarnings("unchecked")
        var entity = (E) OrmContext.getAccessor().load(pk, (Class<IEntity<?>>) entityDef.getClazz());

        // 如果数据库中不存在则给一个默认值
        if (entity == null) {
            // 数据库无法加载缓存，返回默认值
            logger.warn("[{}] can not load [pk:{}] and use default entity to replace it", entityDef.getClazz().getSimpleName(), pk);
            entity = (E) entityDef.newEmptyEntity();
        }
        pnode = new PNode<>(entity);
        cache.put(pk, pnode);
        return entity;
    }

    @Override
    public E loadOrCreate(PK pk) {
        AssertionUtils.notNull(pk);
        var pnode = cache.get(pk);
        if (pnode != null) {
            return pnode.getEntity();
        }

        @SuppressWarnings("unchecked")
        var entity = (E) OrmContext.getAccessor().load(pk, (Class<IEntity<?>>) entityDef.getClazz());

        // 如果数据库中不存在则给一个默认值
        if (entity == null) {
            entity = (E) entityDef.newEntity(pk);
            OrmContext.getAccessor().insert(entity);
        }
        pnode = new PNode<>(entity);
        cache.put(pk, pnode);
        return entity;
    }

    /**
     * 校验需要更新的entity和缓存的entity是否为同一个entity
     */
    private PNode<E> fetchCachePnode(E entity, boolean safe) {
        var id = entity.id();
        var cachePnode = cache.get(id);
        if (cachePnode == null) {
            cachePnode = new PNode<>(entity);
            cache.put(entity.id(), cachePnode);
        }

        // 比较地址是否相等
        if (entity != cachePnode.getEntity()) {
            throw new RunException("fetchCachePnode(): cache entity [id:{}] not equal with update entity [id:{}]", cachePnode.getEntity().id(), id);
        }

        if (safe) {
            var pnodeThreadId = cachePnode.getThreadId();
            var currentThreadId = Thread.currentThread().getId();
            if (pnodeThreadId != currentThreadId) {
                if (pnodeThreadId == 0) {
                    cachePnode.setThreadId(currentThreadId);
                } else {
                    var pnodeThread = ThreadUtils.findThread(pnodeThreadId);
                    // 有并发写风险，第一次更新文档的线程和第2次更新更新文档的线程不相等
                    if (pnodeThread == null) {
                        logger.warn("[{}][id:{}] concurrent write warning, first update [threadId:{}], second update [threadId:{}]", entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, currentThreadId);
                    } else {
                        logger.warn("[{}][id:{}] concurrent write warning, first update [threadId:{}][threadName:{}], second update [threadId:{}][threadName:{}]"
                                , entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, pnodeThread.getName(), currentThreadId, Thread.currentThread().getName());
                    }
                }
            }
        }

        return cachePnode;
    }

    @Override
    public void update(E entity) {
        var cachePnode = fetchCachePnode(entity, true);
        // 加100以防止，立刻加载并且立刻修改数据的情况发生时，服务器取到的时间戳相同
        cachePnode.setModifiedTime(TimeUtils.now() + 100);
    }

    @Override
    public void updateUnsafe(E entity) {
        var cachePnode = fetchCachePnode(entity, false);
        cachePnode.setModifiedTime(TimeUtils.now() + 100);
    }

    @Override
    public void updateNow(E entity) {
        var cachePnode = fetchCachePnode(entity, true);
        OrmContext.getAccessor().update(cachePnode.getEntity());
        var currentTime = TimeUtils.now();
        cachePnode.setWriteToDbTime(currentTime);
        cachePnode.setModifiedTime(currentTime);
    }

    @Override
    public void updateNowUnsafe(E entity) {
        var cachePnode = fetchCachePnode(entity, false);
        OrmContext.getAccessor().update(cachePnode.getEntity());
        var currentTime = TimeUtils.now();
        cachePnode.setWriteToDbTime(currentTime);
        cachePnode.setModifiedTime(currentTime);
    }

    @Override
    public void invalidate(PK pk) {
        // 游戏业务中，操作最频繁的是update，不是insert，delete，query
        // 所以这边并不考虑
        AssertionUtils.notNull(pk);
        cache.remove(pk);
    }

    // 游戏中80%都是执行更新的操作，这样做会极大的提高更新速度
    @Override
    public void persistAll() {
        @SuppressWarnings("unchecked")
        var entityClass = (Class<E>) entityDef.getClazz();

        var updateList = new ArrayList<E>();
        var currentTime = TimeUtils.currentTimeMillis();
        cache.forEach(new BiConsumer<PK, PNode<E>>() {
            @Override
            public void accept(PK pk, PNode<E> pnode) {
                var entity = pnode.getEntity();
                if (pnode.getModifiedTime() != pnode.getWriteToDbTime()) {
                    pnode.setWriteToDbTime(currentTime);
                    pnode.setModifiedTime(currentTime);
                    updateList.add(entity);
                }
            }
        });
        persistList(updateList, entityClass);
    }

    @Override
    public void persist(PK pk) {
        var pnode = cache.get(pk);
        if (pnode == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        var entityClass = (Class<E>) entityDef.getClazz();
        var updateList = new ArrayList<E>();
        var currentTime = TimeUtils.currentTimeMillis();
        if (pnode.getModifiedTime() == pnode.getWriteToDbTime()) {
            return;
        }
        var entity = pnode.getEntity();
        pnode.setWriteToDbTime(currentTime);
        pnode.setModifiedTime(currentTime);
        updateList.add(entity);
        persistList(updateList, entityClass);
    }

    private void persistList(ArrayList<E> updateList, Class<E> entityClass) {
        // 执行更新
        if (updateList.isEmpty()) {
            return;
        }
        var page = Page.valueOf(1, BATCH_SIZE, updateList.size());
        var maxPageSize = page.totalPage();

        for (var currentPage = 1; currentPage <= maxPageSize; currentPage++) {
            page.setPage(currentPage);
            var currentUpdateList = page.currentPageList(updateList);
            try {
                var collection = OrmContext.getOrmManager().getCollection(entityClass).withWriteConcern(WriteConcern.ACKNOWLEDGED);

                var batchList = currentUpdateList.stream()
                        .map(it -> {
                            var version = it.gvs();
                            it.svs(version + 1);

                            var filter = it.gvs() > 0
                                    ? Filters.and(Filters.eq("_id", it.id()), Filters.eq("vs", version))
                                    : Filters.eq("_id", it.id());

                            return new ReplaceOneModel<>(filter, it);
                        })
                        .toList();

                var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));
                if (result.getModifiedCount() == batchList.size()) {
                    continue;
                }

                // mostly because the document that needs to be updated is the same as the document in the database
                // 开始执行容错操作（大部分原因都是因为需要更新的文档和数据库的文档相同）
                logger.warn("persistAll(): [{}] batch update [{}] not equal to final update [{}], and try to use persistAllAndCompare() to update every single entity."
                        , entityClass.getSimpleName(), currentUpdateList.size(), result.getModifiedCount());
            } catch (Throwable t) {
                logger.error("persistAll(): [{}] batch update unknown error and try ", entityClass.getSimpleName(), t);
            }
            persistAllAndCompare(currentUpdateList);
        }

        updateList.clear();
    }

    private void persistAllAndCompare(List<E> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }

        @SuppressWarnings("unchecked")
        var entityClass = (Class<E>) entityDef.getClazz();
        var ids = updateList.stream().map(it -> it.id()).toList();

        try {
            var dbList = OrmContext.getQuery(entityClass).in("_id", ids).queryAll();
            var dbMap = dbList.stream().collect(Collectors.toMap(key -> key.id(), value -> value));
            for (var entity : updateList) {
                var id = entity.id();
                var dbEntity = dbMap.get(id);

                if (dbEntity == null) {
                    cache.remove(entity.id());
                    logger.warn("[database:{}] not found entity [id:{}]", entityClass.getSimpleName(), id);
                    continue;
                }

                // 如果没有版本号，则直接更新数据库
                if (entity.gvs() <= 0) {
                    OrmContext.getAccessor().update(entity);
                    continue;
                }

                // 如果版本号相同，说明已经更新到
                if (dbEntity.gvs() == entity.gvs()) {
                    continue;
                }

                // 如果数据库版本号较小，说明缓存的数据是最新的，直接写入数据库
                if (dbEntity.gvs() < entity.gvs()) {
                    OrmContext.getAccessor().update(entity);
                    continue;
                }

                // 如果数据库版本号较大，说明缓存的数据不是最新的，直接清除缓存，下次重新加载
                if (dbEntity.gvs() > entity.gvs()) {
                    cache.remove(id);
                    load(id);
                    logger.warn("[database:{}] document of entity [id:{}] version [{}] is greater than cache [vs:{}]", entityClass.getSimpleName(), id, dbEntity.gvs(), entity.gvs());
                    continue;
                }
            }
        } catch (Throwable t) {
            logger.error("persistAllAndCompare(): [{}] unknown error", entityClass.getSimpleName(), t);
        }
    }

    @Override
    public void forEach(BiConsumer<PK, E> biConsumer) {
        cache.forEach((pk, pnode) -> biConsumer.accept(pk, pnode.getEntity()));
    }

    @Override
    public long size() {
        return cache.size();
    }

}
