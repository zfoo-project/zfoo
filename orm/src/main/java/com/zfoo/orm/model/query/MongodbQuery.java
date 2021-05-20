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

package com.zfoo.orm.model.query;

import com.mongodb.client.model.Filters;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class MongodbQuery implements IQuery {

    @Override
    public <E extends IEntity<?>> List<E> queryAll(Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var list = new ArrayList<E>();
        collection.find().forEach(new Consumer<IEntity<?>>() {
            @Override
            public void accept(IEntity<?> entity) {
                list.add((E) entity);
            }
        });
        return list;
    }

    @Override
    public <E extends IEntity<?>> List<E> queryFieldEqual(String fieldName, Object fieldValue, Class<E> entityClazz) {
        if (fieldValue == null) {
            return Collections.emptyList();
        }
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var list = new ArrayList<E>();
        collection.find(Filters.eq(fieldName, fieldValue)).forEach(new Consumer<IEntity<?>>() {
            @Override
            public void accept(IEntity<?> entity) {
                list.add((E) entity);
            }
        });
        return list;
    }

    @Override
    public <E extends IEntity<?>> List<E> queryFieldLike(String fieldName, String fieldValue, Class<E> entityClazz) {
        if (StringUtils.isBlank(fieldValue)) {
            return Collections.emptyList();
        }
        var collection = OrmContext.getOrmManager().getCollection((Class<? extends IEntity<?>>) entityClazz);
        var list = new ArrayList<E>();
        var regex = StringUtils.format("^{}.*", fieldValue);
        collection.find(Filters.regex(fieldName, regex)).forEach((Consumer<IEntity<?>>) entity -> list.add((E) entity));
        return list;
    }

    @Override
    public <E extends IEntity<?>> List<E> queryFieldIn(String fieldName, List<?> fieldValueList, Class<E> entityClazz) {
        if (CollectionUtils.isEmpty(fieldValueList)) {
            return Collections.emptyList();
        }

        var collection = OrmContext.getOrmManager().getCollection(entityClazz);
        var list = new ArrayList<E>();
        collection.find(Filters.in(fieldName, fieldValueList)).forEach(new Consumer<IEntity<?>>() {
            @Override
            public void accept(IEntity<?> entity) {
                list.add((E) entity);
            }
        });
        return list;
    }

    @Override
    public <E extends IEntity<?>> Pair<Page, List<E>> pageQuery(int page, int itemsPerPage, Class<E> entityClazz) {
        var collection = OrmContext.getOrmManager().getCollection(entityClazz);

        var p = Page.valueOf(page, itemsPerPage, collection.countDocuments());

        var list = new ArrayList<E>();
        collection.find()
                .skip(p.skipNum())
                .limit(p.getItemsPerPage())
                .forEach(new Consumer<IEntity<?>>() {
                    @Override
                    public void accept(IEntity<?> entity) {
                        list.add((E) entity);
                    }
                });

        return new Pair<>(p, list);
    }


}
