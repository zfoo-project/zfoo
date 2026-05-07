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
public interface IQueryBuilder<PK extends Comparable<PK>, E extends IEntity<PK>> {

    // EQ  = equal to
    IQueryBuilder<PK, E> eq(String fieldName, Object fieldValue);

    // NE  <> not equal to
    IQueryBuilder<PK, E> ne(String fieldName, Object fieldValue);

    IQueryBuilder<PK, E> in(String fieldName, List<?> fieldValueList);

    // [n] IN / NOT IN query
    IQueryBuilder<PK, E> nin(String fieldName, List<?> fieldValueList);

    // LT  < less than
    IQueryBuilder<PK, E> lt(String fieldName, Object fieldValue);

    // LTE <= less than or equal to
    IQueryBuilder<PK, E> lte(String fieldName, Object fieldValue);

    // GT  > greater than
    IQueryBuilder<PK, E> gt(String fieldName, Object fieldValue);

    // GTE >= greater than or equal to
    IQueryBuilder<PK, E> gte(String fieldName, Object fieldValue);

    // LIKE fuzzy/regex query
    IQueryBuilder<PK, E> like(String fieldName, String fieldValue);

    List<E> queryAll();


    /**
     * Paginated query; sorted by id by default
     *
     * @param page         page number (1-based)
     * @param itemsPerPage items per page
     */
    Pair<Page, List<E>> queryPage(int page, int itemsPerPage);

    E queryFirst();
}
