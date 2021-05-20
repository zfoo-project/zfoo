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

import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.protocol.model.Pair;

import java.util.List;

/**
 * 对数据库进行（查找）的相关方法
 *
 * @author jaysunxiao
 * @version 3.0
 */
public interface IQuery {

    <E extends IEntity<?>> List<E> queryFieldLike(String fieldName, String fieldValue, Class<E> entityClazz);

    <E extends IEntity<?>> List<E> queryAll(Class<E> entityClazz);

    <E extends IEntity<?>> List<E> queryFieldEqual(String fieldName, Object fieldValue, Class<E> entityClazz);

    <E extends IEntity<?>> List<E> queryFieldIn(String fieldName, List<?> fieldValueList, Class<E> entityClazz);

    /**
     * 分页查询，默认按照id排序
     *
     * @param page         第几页
     * @param itemsPerPage 每页容量
     * @param entityClazz  实体类
     */
    <E extends IEntity<?>> Pair<Page, List<E>> pageQuery(int page, int itemsPerPage, Class<E> entityClazz);

}
