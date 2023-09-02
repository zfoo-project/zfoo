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

import com.zfoo.orm.model.IEntity;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 对数据库进行（增，删，改）的相关方法
 *
 * @author godotg
 * @version 3.0
 */
public interface IAccessor {

    <E extends IEntity<?>> boolean insert(E entity);

    <E extends IEntity<?>> void batchInsert(List<E> entities);

    <E extends IEntity<?>> boolean update(E entity);

    <E extends IEntity<?>> void batchUpdate(List<E> entities);

    <E extends IEntity<?>> boolean delete(E entity);

    <E extends IEntity<?>> boolean delete(Object pk, Class<E> entityClazz);

    <E extends IEntity<?>> void batchDelete(List<E> entities);

    <E extends IEntity<?>> void batchDelete(List<?> pks, Class<E> entityClazz);

    @Nullable
    <E extends IEntity<?>> E load(Object pk, Class<E> entityClazz);

}
