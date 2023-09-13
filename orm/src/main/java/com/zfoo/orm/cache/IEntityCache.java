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

import com.zfoo.orm.model.IEntity;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author godotg
 */
public interface IEntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> {

    /**
     * 从数据库中加载数据到缓存，如果数据库不存在则返回一个id为空的默认值，并且将这个默认值加入缓存
     */
    E load(PK pk);

    /**
     * 更新缓存中的数据，只更新缓存的时间戳，并通过一定策略写入到数据库
     */
    void update(E entity);

    /**
     * 不会删除数据库中的数据，只会删除缓存数据
     *
     * @param pk 组要删除的主键
     */
    void invalidate(PK pk);

    /**
     * 持久化所有缓存数据
     */
    void persistAll();

    /**
     * 获取所有存在的缓存对象
     */
    List<E> allPresentCaches();

    void forEach(BiConsumer<PK, E> biConsumer);

    long size();

    /**
     * 统计缓存命中率
     */
    String recordStatus();

}
