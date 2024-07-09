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
import com.zfoo.orm.cache.wrapper.EnhanceUtils;
import com.zfoo.orm.cache.wrapper.EntityWrapper;
import com.zfoo.orm.cache.wrapper.IEntityWrapper;
import com.zfoo.orm.model.EntityDef;
import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.query.Page;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.GraalVmUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.scheduler.util.LazyCache;
import com.zfoo.scheduler.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author godotg
 */
public class EntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> implements IEntityCache<PK, E> {

    private static final Logger logger = LoggerFactory.getLogger(EntityCache.class);

    private static final int DEFAULT_BATCH_SIZE = 512;

    private final Class<E> clazz;
    private final EntityDef entityDef;
    private final LazyCache<PK, PNode<PK, E>> cache;
    private final IEntityWrapper<PK, E> wrapper;


    public EntityCache(Class<E> entityClass, EntityDef entityDef) {
        this.clazz = entityClass;
        this.entityDef = entityDef;
        // 创建CacheVersion
        var entityWrapper = new EntityWrapper<>(clazz);
        if (GraalVmUtils.isGraalVM()) {
            wrapper = entityWrapper;
        } else {
            wrapper = EnhanceUtils.createEntityWrapper(entityWrapper);
        }

        var removeCallback = new BiConsumer<Pair<PK, PNode<PK, E>>, LazyCache.RemovalCause>() {
            @Override
            public void accept(Pair<PK, PNode<PK, E>> pair, LazyCache.RemovalCause removalCause) {
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
                EventBus.asyncExecute(clazz.hashCode(), new Runnable() {
                    @Override
                    public void run() {
                        var collection = OrmContext.getOrmManager().getCollection(clazz);

                        var version = wrapper.gvs(entity);
                        wrapper.svs(entity, version + 1);

                        var filter = wrapper.gvs(entity) > 0
                                ? Filters.and(Filters.eq("_id", entity.id()), Filters.eq(wrapper.versionFieldName(), version))
                                : Filters.eq("_id", entity.id());
                        var result = collection.replaceOne(filter, entity);
                        if (result.getModifiedCount() <= 0) {
                            // 移除缓存时，更新数据库中的实体文档异常
                            logger.error("onRemoval(): update entity to db failed when remove [{}] [pk:{}] by [removalCause:{}]", clazz.getSimpleName(), entity.id(), removalCause);
                        }
                    }
                });
            }
        };
        var expireCheckIntervalMillis = Math.max(3 * TimeUtils.MILLIS_PER_SECOND, entityDef.getExpireMillisecond() / 10);
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

        var entity = OrmContext.getAccessor().load(pk, clazz);

        // 如果数据库中不存在则返回null，并将null放入缓存，防止频繁查库
        if (entity == null) {
            logger.warn("[{}] can not load [pk:{}] and use null to replace it", clazz.getSimpleName(), pk);
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

        var entity = (E) OrmContext.getAccessor().load(pk, clazz);

        // 如果数据库中不存在则给一个默认值
        if (entity == null) {
            entity = wrapper.newEntity(pk);
            OrmContext.getAccessor().insert(entity);
        }
        pnode = new PNode<>(entity);
        cache.put(pk, pnode);
        return entity;
    }

    /**
     * 校验需要更新的entity和缓存的entity是否为同一个entity
     */
    private PNode<PK, E> fetchCachePnode(E entity, boolean safe) {
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
                    var builder = new StringBuilder();
                    for(var stack : Thread.currentThread().getStackTrace()) {
                        builder.append(FileUtils.LS).append(stack);
                    }
                    // 有并发写风险，第一次更新文档的线程和第2次更新更新文档的线程不相等
                    if (pnodeThread == null) {
                        logger.warn("[{}][id:{}] concurrent write warning, first update [threadId:{}], second update [threadId:{}], current stack trace as following:{}"
                                , entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, currentThreadId, builder);
                    } else {
                        logger.warn("[{}][id:{}] concurrent write warning, first update [threadId:{}][threadName:{}], second update [threadId:{}][threadName:{}], current stack trace as following:{}"
                                , entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, pnodeThread.getName(), currentThreadId, Thread.currentThread().getName(), builder);
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
        cachePnode.resetTime(TimeUtils.now());
    }

    @Override
    public void updateUnsafeNow(E entity) {
        var cachePnode = fetchCachePnode(entity, false);
        OrmContext.getAccessor().update(cachePnode.getEntity());
        cachePnode.resetTime(TimeUtils.now());
    }

    @Override
    public void invalidate(PK pk) {
        // 游戏业务中，操作最频繁的是update，不是insert，delete，query
        // 所以这边并不考虑
        AssertionUtils.notNull(pk);
        cache.remove(pk);
    }

    @Override
    public void persist(PK pk) {
        var pnode = cache.get(pk);
        if (pnode == null) {
            return;
        }
        if (pnode.getModifiedTime() == pnode.getWriteToDbTime()) {
            return;
        }
        pnode.resetTime(TimeUtils.currentTimeMillis());
        var updateList = new ArrayList<E>();
        updateList.add(pnode.getEntity());
        doPersist(updateList);
    }


    // 游戏中80%都是执行更新的操作，这样做会极大的提高更新速度
    // 没有并发问题的entity指的是内部没有使用集合或者使用的集合全部支持并发操作
    // 没有并发问题的entity还是在异步线程池Event慢慢更新，有并发问题的entity才放到原来的update线程去更新（第一次update会记录entity所在线程）
    @Override
    public void persistAll() {
        if (entityDef.isThreadSafe()) {
            EventBus.asyncExecute(clazz.hashCode(), () -> persistAllBlock());
        } else {
            var currentTime = TimeUtils.currentTimeMillis();
            // key为threadId
            var updateMap = new HashMap<Long, List<E>>();
            cache.forEach(new BiConsumer<PK, PNode<PK, E>>() {
                @Override
                public void accept(PK pk, PNode<PK, E> pnode) {
                    var entity = pnode.getEntity();
                    if (pnode.getModifiedTime() != pnode.getWriteToDbTime()) {
                        pnode.resetTime(currentTime);
                        var updateList = updateMap.computeIfAbsent(pnode.getThreadId(), it -> new ArrayList<>());
                        updateList.add(entity);
                    }
                }
            });
            var count = 0;
            for (var entry : updateMap.entrySet()) {
                var threadId = entry.getKey();
                var updateList = entry.getValue();
                var executor = ThreadUtils.executorByThreadId(threadId);
                if (executor == null) {
                    EventBus.asyncExecute(clazz.hashCode(), () -> doPersist(updateList));
                } else {
                    // 使用scheduler均匀的分配入库的时间点，减少数据库的并发写入压力
                    SchedulerBus.schedule(() -> executor.execute(() -> doPersist(updateList)), count++ * 100L, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void persistAllBlock() {
        var currentTime = TimeUtils.currentTimeMillis();
        var updateList = new ArrayList<E>();
        cache.forEach(new BiConsumer<PK, PNode<PK, E>>() {
            @Override
            public void accept(PK pk, PNode<PK, E> pnode) {
                var entity = pnode.getEntity();
                if (pnode.getModifiedTime() != pnode.getWriteToDbTime()) {
                    pnode.resetTime(currentTime);
                    updateList.add(entity);
                }
            }
        });
        doPersist(updateList);
    }

    private void doPersist(List<E> updateList) {
        // 执行更新
        if (updateList.isEmpty()) {
            return;
        }

        var page = Page.valueOf(1, DEFAULT_BATCH_SIZE, updateList.size());
        var maxPageSize = page.totalPage();

        for (var currentPage = 1; currentPage <= maxPageSize; currentPage++) {
            page.setPage(currentPage);
            var currentUpdateList = page.currentPageList(updateList);
            try {
                var collection = OrmContext.getOrmManager().getCollection(clazz).withWriteConcern(WriteConcern.ACKNOWLEDGED);

                var batchList = currentUpdateList.stream()
                        .map(it -> {
                            var version = wrapper.gvs(it);
                            wrapper.svs(it, version + 1);

                            var filter = wrapper.gvs(it) > 0
                                    ? Filters.and(Filters.eq("_id", it.id()), Filters.eq(wrapper.versionFieldName(), version))
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
                        , clazz.getSimpleName(), currentUpdateList.size(), result.getModifiedCount());
            } catch (Throwable t) {
                logger.error("persistAll(): [{}] batch update unknown error and try ", clazz.getSimpleName(), t);
            }
            persistAllAndCompare(currentUpdateList);
        }

        updateList.clear();
    }

    private void persistAllAndCompare(List<E> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }

        var ids = updateList.stream().map(it -> it.id()).toList();

        var dbList = OrmContext.getQuery(clazz).in("_id", ids).queryAll();
        var dbMap = dbList.stream().collect(Collectors.toMap(key -> key.id(), value -> value));
        for (var entity : updateList) {
            var id = entity.id();
            var dbEntity = dbMap.get(id);

            if (dbEntity == null) {
                cache.remove(entity.id());
                logger.warn("[database:{}] not found entity [id:{}]", clazz.getSimpleName(), id);
                continue;
            }

            // 如果没有版本号，则直接更新数据库
            var entityVersion = wrapper.gvs(entity);
            var dbEntityVersion = wrapper.gvs(dbEntity);
            if (entityVersion <= 0) {
                OrmContext.getAccessor().update(entity);
                continue;
            }

            // 如果版本号相同，说明已经更新到
            if (dbEntityVersion == entityVersion) {
                continue;
            }

            // 如果数据库版本号较小，说明缓存的数据是最新的，直接写入数据库
            if (dbEntityVersion < entityVersion) {
                OrmContext.getAccessor().update(entity);
                continue;
            }

            // 数据库版本号较大，说明缓存的数据不是最新的，直接清除缓存，下次重新加载
            cache.remove(id);
            load(id);
            logger.warn("[database:{}] document of entity [id:{}] version [{}] is greater than cache [vs:{}]", clazz.getSimpleName(), id, dbEntityVersion, entityVersion);
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
