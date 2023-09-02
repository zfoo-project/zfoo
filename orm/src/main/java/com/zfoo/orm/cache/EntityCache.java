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

import com.github.benmanes.caffeine.cache.*;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.cache.persister.IOrmPersister;
import com.zfoo.orm.cache.persister.PNode;
import com.zfoo.orm.model.EntityDef;
import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.query.Page;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author godotg
 * @version 3.0
 */
public class EntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> implements IEntityCache<PK, E> {

    private static final Logger logger = LoggerFactory.getLogger(EntityCache.class);

    private static final int BATCH_SIZE = 512;

    private final EntityDef entityDef;

    private final LoadingCache<PK, PNode<E>> cache;

    public EntityCache(EntityDef entityDef) {
        this.entityDef = entityDef;

        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(entityDef.getExpireMillisecond(), TimeUnit.MILLISECONDS)
                .maximumSize(entityDef.getCacheSize())
                .initialCapacity(CollectionUtils.comfortableCapacity(entityDef.getCacheSize()))
                //.recordStats() // 开启统计信息开关，cache.stats()获取统计信息
                .removalListener(new RemovalListener<PK, PNode<E>>() {
                    @Override
                    public void onRemoval(@Nullable PK pk, @Nullable PNode<E> pnode, @NonNull RemovalCause removalCause) {
                        if (pnode.getWriteToDbTime() == pnode.getModifiedTime()) {
                            return;
                        }

                        // 缓存失效之前，将数据写入数据库
                        var entity = pnode.getEntity();
                        var collection = OrmContext.getOrmManager().getCollection((Class<E>) entityDef.getClazz());

                        var version = entity.gvs();
                        entity.svs(version + 1);

                        var filter = entity.gvs() > 0
                                ? Filters.and(Filters.eq("_id", entity.id()), Filters.eq("vs", version))
                                : Filters.eq("_id", entity.id());
                        var result = collection.replaceOne(filter, entity);
                        if (result.getModifiedCount() <= 0) {
                            logger.warn("移除[removalCause:{}]缓存时，更新数据库[{}]中的实体主键[pk:{}]的文档异常"
                                    , removalCause, entityDef.getClazz().getSimpleName(), entity.id());
                        }
                    }
                })
                .build(new CacheLoader<PK, PNode<E>>() {
                    @Override
                    public @Nullable PNode<E> load(@NonNull PK pk) {
                        var entity = (E) OrmContext.getAccessor().load(pk, (Class<IEntity<?>>) entityDef.getClazz());

                        // 如果数据库中不存在则给一个默认值
                        if (entity == null) {
                            entity = (E) entityDef.newEntity(pk);
                        }

                        return new PNode<E>(entity);
                    }
                });

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
        try {
            return cache.get(pk).getEntity();
        } catch (Exception e) {
            logger.error("数据库[{}]缓存[pk:{}]加载发生exception异常", entityDef.getClazz().getSimpleName(), pk, e);
        } catch (Throwable t) {
            logger.error("数据库[{}]缓存[pk:{}]加载发生error异常", entityDef.getClazz().getSimpleName(), pk, t);
        }

        logger.warn("数据库[{}]无法加载缓存[pk:{}]，返回默认值", entityDef.getClazz().getSimpleName(), pk);
        var entity = (E) entityDef.newEntity(pk);
        var pnode = new PNode<E>(entity);
        cache.put(pk, pnode);
        return entity;
    }

    @Override
    public void update(E entity) {
        AssertionUtils.notNull(entity);

        var currentPnode = cache.getIfPresent(entity.id());

        if (currentPnode == null) {
            currentPnode = new PNode<>(entity);
            cache.put(entity.id(), currentPnode);
        }

        var pnodeThreadId = currentPnode.getThreadId();
        var currentThreadId = Thread.currentThread().getId();
        if (pnodeThreadId != currentThreadId) {
            if (pnodeThreadId == 0) {
                currentPnode.setThreadId(currentThreadId);
            } else {
                var pnodeThread = ThreadUtils.findThread(pnodeThreadId);
                if (pnodeThread == null) {
                    logger.warn("[{}][id:{}]有并发写风险，第一次更新的线程[threadId:{}]，第2次更新的线程[threadId:{}]", entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, currentThreadId);
                } else {
                    logger.warn("[{}][id:{}]有并发写风险，第一次更新的线程[threadId:{}][threadName:{}]，第2次更新的线程[threadId:{}][threadName:{}]"
                            , entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, pnodeThread.getName(), currentThreadId, Thread.currentThread().getName());
                }
            }
        }

        // 加100以防止，立刻加载并且立刻修改数据的情况发生时，服务器取到的时间戳相同
        currentPnode.setModifiedTime(TimeUtils.now() + 100);
    }

    @Override
    public void invalidate(PK pk) {
        // 游戏业务中，操作最频繁的是update，不是insert，delete，query
        // 所以这边并不考虑
        AssertionUtils.notNull(pk);
        cache.invalidate(pk);
    }

    // 游戏中80%都是执行更新的操作，这样做会极大的提高更新速度
    @Override
    public void persistAll() {
        try {
            var allPnodes = cache.asMap().values();

            if (allPnodes.isEmpty()) {
                return;
            }

            var updateList = new ArrayList<E>();
            var currentTime = TimeUtils.currentTimeMillis();
            for (var pnode : allPnodes) {
                var entity = pnode.getEntity();
                if (pnode.getModifiedTime() != pnode.getWriteToDbTime()) {
                    pnode.setWriteToDbTime(currentTime);
                    pnode.setModifiedTime(currentTime);
                    updateList.add(entity);
                    continue;
                }

                if (currentTime - pnode.getModifiedTime() >= entityDef.getExpireMillisecond()) {
                    invalidate(pnode.getEntity().id());
                }
            }

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
                    var collection = OrmContext.getOrmManager().getCollection((Class<E>) entityDef.getClazz()).withWriteConcern(WriteConcern.ACKNOWLEDGED);

                    var batchList = currentUpdateList.stream()
                            .map(it -> {
                                var version = it.gvs();
                                it.svs(version + 1);

                                var filter = it.gvs() > 0
                                        ? Filters.and(Filters.eq("_id", it.id()), Filters.eq("vs", version))
                                        : Filters.eq("_id", it.id());

                                return new ReplaceOneModel<>(filter, it);
                            })
                            .collect(Collectors.toList());

                    var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));
                    if (result.getModifiedCount() == batchList.size()) {
                        continue;
                    }

                    logger.warn("在数据库[{}]的批量更新操作中需要更新的数量[{}]和最终更新的数量[{}]不相同，开始执行容错操作（大部分原因都是因为需要更新的文档和数据库的文档相同）"
                            , entityDef.getClazz().getSimpleName(), currentUpdateList.size(), result.getModifiedCount());
                    persistAllAndCompare(currentUpdateList);
                } catch (Throwable t) {
                    logger.error("数据库[{}]批量更新操作未知异常，开始执行容错操作", entityDef.getClazz().getSimpleName(), t);
                    persistAllAndCompare(currentUpdateList);
                }
            }

            updateList.clear();

        } catch (Exception e) {
            logger.error("数据库持久化器[{}]的持久化过程中exception异常退出", entityDef.getClazz().getSimpleName(), e);
        } catch (Throwable t) {
            logger.error("数据库持久化器[{}]的持久化过程中throwable异常退出", entityDef.getClazz().getSimpleName(), t);
        } finally {
        }
    }

    private void persistAllAndCompare(List<E> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }

        var ids = updateList.stream().map(it -> it.id()).collect(Collectors.toList());

        try {
            var dbList = OrmContext.getQuery((Class<E>) entityDef.getClazz()).in("_id", ids).queryAll();
            var dbMap = dbList.stream().collect(Collectors.toMap(key -> key.id(), value -> value));
            for (var entity : updateList) {
                var dbEntity = dbMap.get(entity.id());

                if (dbEntity == null) {
                    cache.invalidate(entity.id());
                    continue;
                }

                // 如果没有版本号，则写入数据库并清除缓存
                if (entity.gvs() <= 0) {
                    OrmContext.getAccessor().update(entity);
                    cache.invalidate(entity.id());
                    continue;
                }

                // 如果版本号相同，说明已经更新到
                if (dbEntity.gvs() == entity.gvs()) {
                    cache.invalidate(entity.id());
                    continue;
                }

                // 如果数据库版本号较大，说明缓存的数据不是最新的，直接清除缓存，下次重新加载
                if (dbEntity.gvs() > entity.gvs()) {
                    cache.invalidate(entity.id());
                    continue;
                }

                // 如果数据库版本号较小，说明缓存的数据是最新的，直接写入数据库
                if (dbEntity.gvs() < entity.gvs()) {
                    OrmContext.getAccessor().update(entity);
                    cache.invalidate(entity.id());
                    continue;
                }
            }
        } catch (Throwable t) {
            logger.error("数据库[{}]容错操作异常,", entityDef.getClazz().getSimpleName(), t);
        }
    }

    @Override
    public List<E> allPresentCaches() {
        var allPnodes = cache.asMap().values();

        if (allPnodes.isEmpty()) {
            return Collections.emptyList();
        }
        return allPnodes.stream().map(it -> it.getEntity()).collect(Collectors.toList());
    }

    @Override
    public void forEach(BiConsumer<PK, E> biConsumer) {
        cache.asMap().forEach((pk, pNode) -> biConsumer.accept(pk, pNode.getEntity()));
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public String recordStatus() {
        var stats = cache.stats();
        return StringUtils.format("数据库[{}]缓存命中率[hitRate:{}]，命中次数[hitCount:{}]，加载次数[loadCount:{}]，加载新值的平均时间秒[averageLoadPenalty:{}]，缓存项被回收的总数[evictionCount:{}]"
                , entityDef.getClazz().getSimpleName(), stats.hitRate(), stats.hitCount(), stats.loadCount(), stats.averageLoadPenalty() / TimeUtils.NANO_PER_SECOND, stats.evictionCount());
    }

}
