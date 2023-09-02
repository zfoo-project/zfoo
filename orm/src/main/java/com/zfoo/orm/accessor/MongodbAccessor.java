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
import com.zfoo.orm.model.IEntity;
import com.zfoo.protocol.collection.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * @author godotg
 * @version 3.0
 */
public class MongodbAccessor implements IAccessor {

    private static final Logger logger = LoggerFactory.getLogger(MongodbAccessor.class);


    @Override
    public <E extends IEntity<?>> boolean insert(E entity) {
        var entityClazz = (Class<E>) entity.getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = collection.insertOne(entity);
        return result.getInsertedId() != null;
    }

    @Override
    public <E extends IEntity<?>> void batchInsert(List<E> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        var entityClazz = (Class<E>) entities.get(0).getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        collection.insertMany(entities);
    }

    @Override
    public <E extends IEntity<?>> boolean update(E entity) {
        try {
            var entityClazz = (Class<E>) entity.getClass();
            var collection = OrmContext.getOrmManager().getCollection(entityClazz);

            var filter = Filters.eq("_id", entity.id());

            var result = collection.replaceOne(filter, entity);
            if (result.getModifiedCount() <= 0) {
                logger.warn("数据库[{}]中没有[id:{}]的字段，或者需要更新的数据和数据库中的相同", entityClazz.getSimpleName(), entity.id());
                return false;
            }
            return true;
        } catch (Throwable t) {
            logger.error("更新update未知异常", t);
        }
        return false;
    }

    @Override
    public <E extends IEntity<?>> void batchUpdate(List<E> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        try {
            var entityClazz = (Class<E>) entities.get(0).getClass();
            var collection = OrmContext.getOrmManager().getCollection(entityClazz);

            var batchList = entities.stream()
                    .map(it -> new ReplaceOneModel<E>(Filters.eq("_id", it.id()), it))
                    .collect(Collectors.toList());

            var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));
            if (result.getModifiedCount() != entities.size()) {
                logger.warn("在数据库[{}]的批量更新操作中需要更新的数量[{}]和最终更新的数量[{}]不相同（大部分原因都是因为需要更新的文档和数据库的文档相同）"
                        , entityClazz.getSimpleName(), entities.size(), result.getModifiedCount());
            }
        } catch (Throwable t) {
            logger.error("批量更新batchUpdate未知异常", t);
        }
    }

    @Override
    public <E extends IEntity<?>> boolean delete(E entity) {
        var entityClazz = (Class<E>) entity.getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = collection.deleteOne(eq("_id", entity.id()));
        return result.getDeletedCount() > 0;
    }

    @Override
    public <E extends IEntity<?>> boolean delete(Object pk, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = collection.deleteOne(eq("_id", pk));
        return result.getDeletedCount() > 0;
    }

    @Override
    public <E extends IEntity<?>> void batchDelete(List<E> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        var entityClazz = (Class<E>) entities.get(0).getClass();
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var ids = entities.stream().map(it -> (it).id()).collect(Collectors.toList());
        collection.deleteMany(in("_id", ids));
    }

    @Override
    public <E extends IEntity<?>> void batchDelete(List<?> pks, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        collection.deleteMany(in("_id", pks));
    }

    @Override
    public <E extends IEntity<?>> E load(Object pk, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var result = new ArrayList<E>(1);
        collection.find(eq("_id", pk)).forEach(document -> result.add(document));
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return result.get(0);
    }

}
