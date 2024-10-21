/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.accessor;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.cache.persister.PNode;
import com.zfoo.orm.model.IEntity;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author godotg
 */
public class MongodbAccessor implements IAccessor {

    private static final Logger logger = LoggerFactory.getLogger(MongodbAccessor.class);


    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> boolean insert(E entity) {
        @SuppressWarnings("unchecked")
        var entityClazz = (Class<E>) entity.getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = collection.insertOne(entity);
        return result.getInsertedId() != null;
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> void batchInsert(List<E> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        @SuppressWarnings("unchecked")
        var entityClazz = (Class<E>) entities.get(0).getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        collection.insertMany(entities);
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> boolean update(E entity) {
        try {
            @SuppressWarnings("unchecked")
            var entityClazz = (Class<E>) entity.getClass();
            var collection = OrmContext.getOrmManager().getCollection(entityClazz);

            var filter = Filters.eq("_id", entity.id());

            var result = collection.replaceOne(filter, entity);
            if (result.getMatchedCount() <= 0) {
                // 数据库中没有这个id
                logger.warn("database:[{}] [_id:{}] not exist", entityClazz.getSimpleName(), entity.id());
                return false;
            }
            return true;
        } catch (Throwable t) {
            logger.error("update unknown exception", t);
        }
        return false;
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> void batchUpdate(List<E> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            var entityClazz = (Class<E>) entities.get(0).getClass();
            var collection = OrmContext.getOrmManager().getCollection(entityClazz);

            var batchList = entities.stream()
                    .map(it -> new ReplaceOneModel<E>(Filters.eq("_id", it.id()), it))
                    .toList();

            var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));
            if (result.getMatchedCount() != entities.size()) {
                // 在数据库的批量更新操作中需要更新的数量和最终更新的数量不相同
                logger.warn("database:[{}] update size:[{}] not equal with matched size:[{}](some entity of id not exist in database)"
                        , entityClazz.getSimpleName(), entities.size(), result.getMatchedCount());
            }
        } catch (Throwable t) {
            logger.error("batchUpdate unknown exception", t);
        }
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> void batchUpdateNode(List<PNode<PK,E>> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            var entityClazz = (Class<E>) nodes.get(0).getClass();
            var collection = OrmContext.getOrmManager().getCollection(entityClazz);
            List<E> entities = nodes.stream().map(PNode::getEntity).toList();
            var batchList = entities.stream()
                    .map(it -> new ReplaceOneModel<E>(Filters.eq("_id", it.id()), it))
                    .toList();

            var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));

            //设置修改时间
            long  currentTime = TimeUtils.currentTimeMillis();
            nodes.forEach(k->k.resetTime(currentTime));

            if (result.getMatchedCount() != entities.size()) {
                // 在数据库的批量更新操作中需要更新的数量和最终更新的数量不相同
                logger.warn("database:[{}] update size:[{}] not equal with matched size:[{}](some entity of id not exist in database)"
                        , entityClazz.getSimpleName(), entities.size(), result.getMatchedCount());
            }
        } catch (Throwable t) {
            logger.error("batchUpdate unknown exception", t);
        }
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> boolean delete(E entity) {
        @SuppressWarnings("unchecked")
        var entityClazz = (Class<E>) entity.getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = collection.deleteOne(Filters.eq("_id", entity.id()));
        return result.getDeletedCount() > 0;
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> boolean delete(PK pk, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = collection.deleteOne(Filters.eq("_id", pk));
        return result.getDeletedCount() > 0;
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> void batchDelete(List<E> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        @SuppressWarnings("unchecked")
        var entityClazz = (Class<E>) entities.get(0).getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var ids = entities.stream().map(it -> (it).id()).toList();
        collection.deleteMany(Filters.in("_id", ids));
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> void batchDelete(List<PK> pks, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        collection.deleteMany(Filters.in("_id", pks));
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> E load(PK pk, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = new ArrayList<E>(1);
        collection.find(Filters.eq("_id", pk)).forEach(document -> result.add(document));
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.get(0);
    }

}
