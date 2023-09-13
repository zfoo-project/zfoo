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

package com.zfoo.orm.query;

import com.zfoo.orm.model.IEntity;
import com.zfoo.protocol.model.Pair;

import java.util.List;


/**
 * @author sinprog
 */
public interface IQueryBuilder<E extends IEntity> {

    // EQ、=	等于（=）
    IQueryBuilder<E> eq(String fieldName, Object fieldValue);

    // NE、<>	不等于（<>）
    IQueryBuilder<E> ne(String fieldName, Object fieldValue);

    IQueryBuilder<E> in(String fieldName, List<?> fieldValueList);

    // [n] in	（不在）IN 查询
    IQueryBuilder<E> nin(String fieldName, List<?> fieldValueList);

    // LT、<	小于（<）
    IQueryBuilder<E> lt(String fieldName, Object fieldValue);

    // lte、<=	小于等于（<=）
    IQueryBuilder<E> lte(String fieldName, Object fieldValue);

    //  GT、>	大于（>）
    IQueryBuilder<E> gt(String fieldName, Object fieldValue);

    // GTE、>=	大于等于（>=）
    IQueryBuilder<E> gte(String fieldName, Object fieldValue);

    // LIKE	模糊查询
    IQueryBuilder<E> like(String fieldName, String fieldValue);

    List<E> queryAll();


    /**
     * 分页查询，默认按照id排序
     *
     * @param page         第几页
     * @param itemsPerPage 每页容量
     */
    Pair<Page, List<E>> queryPage(int page, int itemsPerPage);

    E queryFirst();
}
