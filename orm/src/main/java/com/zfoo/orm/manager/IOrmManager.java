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

package com.zfoo.orm.manager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.zfoo.orm.cache.IEntityCache;
import com.zfoo.orm.model.IEntity;
import org.bson.Document;

import java.util.Collection;

/**
 * @author godotg
 * @version 3.0
 */
public interface IOrmManager {

    void initBefore();

    void inject();

    void initAfter();

    /**
     * EN:Get Mongo Client, through which you can get other databases or do some other complex operations
     * CN:获取MongoClient，通过MongoClient可以获取到其它数据库或者做一些其它的复杂操作
     */
    MongoClient mongoClient();

    <E extends IEntity<?>> IEntityCache<?, E> getEntityCaches(Class<E> clazz);

    Collection<IEntityCache<?, ?>> getAllEntityCaches();

    /**
     * 基于对象的orm操作
     */
    <E extends IEntity<?>> MongoCollection<E> getCollection(Class<E> entityClazz);

    /**
     * 更加细粒度的操作
     */
    MongoCollection<Document> getCollection(String collection);

}
