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

import java.util.function.BiConsumer;

/**
 * @author godotg
 */
public interface IEntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> {

    /**
     * EN: Load data from the database to the cache and return a default value with an empty ID if the database does not exist.
     * This can use the entity.empty() method to determine if the id is empty
     * <p>
     * CN: 从数据库中加载数据到缓存，如果数据库不存在则返回一个id为空的默认值。可以通过 entity.empty() 方法判断id是否为空
     */
    E load(PK pk);

    /**
     * EN: Load data from the database into the cache, and if the database does not exist,
     * return a default example with the id as the passed in value. And put it into storage.
     * <p>
     * CN: 从数据库中加载数据到缓存，如果数据库不存在则返回一个id为传入值的默认示例。并且入库。(设置了索引唯一的无法使用该方法)
     */
    E loadOrInit(PK pk);

    /**
     * 更新缓存中的数据，只更新缓存的时间戳，并通过一定策略写入到数据库
     * <p>
     * 第一次update()会记录线程号表面当前哪个线程在更新这个entity，后面如果发现update()线程号和第一次不一致会给出线程安全的警告
     */
    void update(E entity);

    /**
     * 同update()，不会校验更新的线程是否一致
     */
    void updateUnsafe(E entity);

    /**
     * 更新缓存中的数据，立刻写入到数据库
     */
    void updateNow(E entity);

    /**
     * 同updateNow()，不会校验更新的线程是否一致
     */
    void updateNowUnsafe(E entity);

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

    void forEach(BiConsumer<PK, E> biConsumer);

    long size();

}
