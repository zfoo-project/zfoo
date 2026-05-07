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
import com.zfoo.protocol.util.*;
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
    private final LazyCache<PK, PNode<PK, E>> caches;
    private final IEntityWrapper<PK, E> wrapper;


    public EntityCache(Class<E> entityClass, EntityDef entityDef) {
        this.clazz = entityClass;
        this.entityDef = entityDef;
        // Create CacheVersion
        var entityWrapper = new EntityWrapper<>(clazz);
        if (GraalVmUtils.isGraalVM()) {
            wrapper = entityWrapper;
        } else {
            wrapper = EnhanceUtils.createEntityWrapper(entityWrapper);
        }

        var removeCallback = new BiConsumer<List<LazyCache.Cache<PK, PNode<PK, E>>>, LazyCache.RemovalCause>() {
            @Override
            public void accept(List<LazyCache.Cache<PK, PNode<PK, E>>> removes, LazyCache.RemovalCause removalCause) {
                if (removalCause == LazyCache.RemovalCause.EXPLICIT) {
                    return;
                }
                var updateList = removes.stream()
                        .map(it -> it.v)
                        .filter(it -> !noNeedUpdate(it))
                        .toList();
                EventBus.asyncExecute(clazz.hashCode(), () -> doPersist(updateList));
            }
        };
        var expireCheckIntervalMillis = Math.max(3 * TimeUtils.MILLIS_PER_SECOND, entityDef.getExpireMillisecond() / 10);
        this.caches = new LazyCache<>(entityDef.getCacheSize(), entityDef.getExpireMillisecond(), expireCheckIntervalMillis, removeCallback);

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
        var pnode = caches.get(pk);
        if (pnode != null) {
            return pnode.getEntity();
        }

        var entity = OrmContext.getAccessor().load(pk, clazz);

        // Return null if not in DB; cache null to prevent repeated DB queries
        if (entity == null) {
            logger.warn("[{}] can not load [pk:{}] and use null to replace it", clazz.getSimpleName(), pk);
        }
        pnode = new PNode<>(entity);
        caches.put(pk, pnode);
        return entity;
    }

    @Override
    public E loadOrCreate(PK pk) {
        AssertionUtils.notNull(pk);
        var pnode = caches.get(pk);
        if (pnode != null) {
            return pnode.getEntity();
        }

        var entity = (E) OrmContext.getAccessor().load(pk, clazz);

        // If not in DB, use a default value
        if (entity == null) {
            entity = wrapper.newEntity(pk);
            var inserted = OrmContext.getAccessor().insert(entity);
            if (!inserted) {
                throw new RunException("loadOrCreate: [{}] can not create [pk:{}]", clazz.getSimpleName(), pk);
            }
        }
        pnode = new PNode<>(entity);
        caches.put(pk, pnode);
        return entity;
    }

    /**
     * Verify that the entity to be updated is the same object as the cached entity
     */
    private PNode<PK, E> fetchCachePnode(E entity, boolean safe) {
        var id = entity.id();
        var cachePnode = caches.get(id);
        if (cachePnode == null) {
            cachePnode = new PNode<>(entity);
            caches.put(entity.id(), cachePnode);
        }

        // Compare object references
        if (entity != cachePnode.getEntity()) {
            throw new RunException("fetchCachePnode: cache entity [id:{}] not equal with update entity [id:{}]", cachePnode.getEntity().id(), id);
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
                    for (var stack : Thread.currentThread().getStackTrace()) {
                        builder.append(FileUtils.LS).append(stack);
                    }
                    // Concurrent write risk: the thread for the 1st and 2nd update are different
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
    public E get(PK pk) {
        PNode<PK, E> cachePnode = caches.get(pk);
        return cachePnode == null ? null : cachePnode.getEntity();
    }

    @Override
    public void update(E entity) {
        var cachePnode = fetchCachePnode(entity, true);
        // Add 128ms buffer to prevent same-timestamp edge case when data is loaded and modified immediately
        cachePnode.setModifiedTime(TimeUtils.now() + 128);
    }

    @Override
    public void updateUnsafe(E entity) {
        var cachePnode = fetchCachePnode(entity, false);
        cachePnode.setModifiedTime(TimeUtils.now() + 128);
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
        // In game services, update is the most frequent operation, not insert/delete/query
        // So other operations are not the main concern here
        AssertionUtils.notNull(pk);
        caches.remove(pk);
    }

    @Override
    public void persist(PK pk) {
        var pnode = caches.get(pk);
        if (noNeedUpdate(pnode)) {
            return;
        }
        doPersist(List.of(pnode));
    }

    private boolean noNeedUpdate(PNode<PK, E> pnode) {
        return pnode == null || pnode.getEntity() == null || pnode.getModifiedTime() == pnode.getWriteToDbTime();
    }


    // ~80% of game operations are updates; this approach greatly improves update performance
    // Thread-safe entity: no collections, or all collections support concurrent access
    // Thread-safe entities use async event pool; non-thread-safe entities use the original update thread (tracked on first update)
    @Override
    public void persistAll() {
        if (entityDef.isThreadSafe()) {
            EventBus.asyncExecute(clazz.hashCode(), () -> persistAllBlock());
        } else {
            // key is threadId
            var updateMap = new HashMap<Long, List<PNode<PK, E>>>();
            var initSize = caches.size() >> 2;
            caches.forEach(new BiConsumer<PK, PNode<PK, E>>() {
                @Override
                public void accept(PK pk, PNode<PK, E> pnode) {
                    if (noNeedUpdate(pnode)) {
                        return;
                    }
                    var updateList = updateMap.computeIfAbsent(pnode.getThreadId(), it -> new ArrayList<>(initSize));
                    updateList.add(pnode);
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
                    // Use scheduler to evenly distribute DB write timestamps to reduce concurrent write pressure
                    SchedulerBus.schedule(() -> executor.execute(() -> doPersist(updateList)), count++ * 128L, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    @Override
    public void persistAllBlock() {
        var updateList = new ArrayList<PNode<PK, E>>(caches.size());
        caches.forEach(new BiConsumer<PK, PNode<PK, E>>() {
            @Override
            public void accept(PK pk, PNode<PK, E> pnode) {
                if (noNeedUpdate(pnode)) {
                    return;
                }
                updateList.add(pnode);
            }
        });
        doPersist(updateList);
    }

    private void doPersist(List<PNode<PK,E>> updateList) {
        // Execute update
        if (updateList.isEmpty()) {
            return;
        }

        if (StringUtils.isEmpty(wrapper.versionFieldName())) {
            doPersistNoVersion(updateList);
        } else {
            doPersistWithVersion(updateList);
        }
    }

    private void doPersistNoVersion(List<PNode<PK,E>> updateList) {
        var page = Page.valueOf(1, DEFAULT_BATCH_SIZE, updateList.size());
        var maxPageSize = page.totalPage();
        for (var currentPage = 1; currentPage <= maxPageSize; currentPage++) {
            page.setPage(currentPage);
            var currentUpdateList = page.currentPageList(updateList);
            try {
                @SuppressWarnings("unchecked")
                var collection = OrmContext.getOrmManager().getCollection(clazz);
                List<E> entities = currentUpdateList.stream().map(PNode::getEntity).toList();
                var batchList = entities.stream()
                        .map(it -> new ReplaceOneModel<E>(Filters.eq("_id", it.id()), it))
                        .toList();

                var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));

                // Set modification time
                long  currentTime = TimeUtils.currentTimeMillis();
                currentUpdateList.forEach(k->k.resetTime(currentTime));

                if (result.getMatchedCount() != entities.size()) {
                    // The expected update count does not match the actual update count in batch operation
                    logger.warn("database:[{}] update size:[{}] not equal with matched size:[{}](some entity of id not exist in database)"
                            , clazz.getSimpleName(), entities.size(), result.getMatchedCount());
                }
            } catch (Throwable t) {
                logger.error("batchUpdate unknown exception", t);
            }
        }
    }

    private void doPersistWithVersion(List<PNode<PK,E>> updateList) {
        var page = Page.valueOf(1, DEFAULT_BATCH_SIZE, updateList.size());
        var maxPageSize = page.totalPage();
        var versionFiledName = wrapper.versionFieldName();

        for (var currentPage = 1; currentPage <= maxPageSize; currentPage++) {
            page.setPage(currentPage);
            var currentUpdateList = page.currentPageList(updateList);
            List<E> entities = currentUpdateList.stream().map(PNode::getEntity).toList();
            try {
                var collection = OrmContext.getOrmManager().getCollection(clazz).withWriteConcern(WriteConcern.ACKNOWLEDGED);
                var batchList = entities.stream()
                        .map(it -> {
                            var version = wrapper.gvs(it);
                            wrapper.svs(it, version + 1);
                            var filter = Filters.and(Filters.eq("_id", it.id()), Filters.eq(versionFiledName, version));
                            return new ReplaceOneModel<>(filter, it);
                        })
                        .toList();

                var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));

                long currentTime = TimeUtils.currentTimeMillis();
                currentUpdateList.forEach(node->node.resetTime(currentTime));
                if (result.getMatchedCount() == batchList.size()) {
                    continue;
                }

                // mostly because the document that needs to be updated is the same as the document in the database
                // Begin fallback operation (most cases: cached document already matches database)
                logger.warn("doPersistWithVersion(): [{}] batch update [{}] not equal to final update [{}], and try to use persistAndCompareVersion() to update every single entity."
                        , clazz.getSimpleName(), currentUpdateList.size(), result.getMatchedCount());
            } catch (Throwable t) {
                logger.error("doPersistWithVersion(): [{}] batch update unknown error and try ", clazz.getSimpleName(), t);
            }
            persistAndCompareVersion(entities);
        }
    }


    private void persistAndCompareVersion(List<E> updateList) {
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
                caches.remove(entity.id());
                logger.warn("[database:{}] not found entity [id:{}]", clazz.getSimpleName(), id);
                continue;
            }

            // No version number: update the database directly
            var entityVersion = wrapper.gvs(entity);
            var dbEntityVersion = wrapper.gvs(dbEntity);

            // Same version: already up-to-date
            if (dbEntityVersion == entityVersion) {
                continue;
            }

            // DB version smaller: cache is newer, write to database
            if (dbEntityVersion < entityVersion) {
                OrmContext.getAccessor().update(entity);
                continue;
            }

            // DB version larger: cache is stale, evict it, reload on next access
            caches.remove(id);
            load(id);
            logger.warn("[database:{}] document of entity [id:{}] version [{}] is greater than cache [vs:{}] and reload db entity to cache", clazz.getSimpleName(), id, dbEntityVersion, entityVersion);
        }
    }

    @Override
    public void forEach(BiConsumer<PK, E> biConsumer) {
        caches.forEach((pk, pnode) -> biConsumer.accept(pk, pnode.getEntity()));
    }

    @Override
    public long size() {
        return caches.size();
    }

}
