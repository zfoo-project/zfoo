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
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author godotg
 */
public interface IEntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> {

    /**
     * EN: Load data from the database to the cache and return null if the database does not exist.
     * CN: 从数据库中加载数据到缓存，如果数据库不存在则返回null。
     */
    @Nullable
    E load(PK pk);

    /**
     * EN: Load data from the database into the cache, and if the database does not exist,
     * return a default example with the id as the passed in value and put it into storage.
     * <p>
     * CN: 从数据库中加载数据到缓存，如果数据库不存在则返回一个id为传入值的默认示例，并且入库。(设置了索引唯一的无法使用该方法)
     */
    E loadOrCreate(PK pk);

    /**
     * EN: Get the data in the cache or returns null if it does not exist in the cache.
     * CN: 获取缓存中的数据，如果缓存中不存在则返回null。
     */
    @Nullable
    E get(PK pk);

    /**
     * EN: Update the timestamp of the cache in the memory.
     * The first update() will record the thread number, and a thread-safe warning will be given if the thread number of next update() is not equal with the first time
     * <p>
     * CN: 更新缓存中的数据，只更新缓存的时间戳，并通过一定策略写入到数据库
     * 第一次update()会记录更新的线程号，后面如果发现update()所在的线程号和第一次不一致会给出线程安全的警告
     */
    void update(E entity);

    /**
     * EN: The similar as update() will not check whether the updated thread number is equal.
     * <p>
     * CN: 同update()，不会校验更新的线程是否一致
     */
    void updateUnsafe(E entity);

    /**
     * EN: Update the cached entity and write it to the database immediately
     * <p>
     * CN: 更新缓存中的数据，立刻写入到数据库
     */
    void updateNow(E entity);

    /**
     * EN: The similar as updateNow() will not check whether the updated thread number is equal.
     * <p>
     * CN: 同updateNow()，不会校验更新的线程是否一致
     */
    void updateUnsafeNow(E entity);

    /**
     * EN: The data in the database will not be deleted, only the cached entity will be deleted.
     * <p>
     * CN: 不会删除数据库中的数据，只会删除缓存数据
     *
     * @param pk primary key
     */
    void invalidate(PK pk);

    /**
     * EN: Persistence cached entity in memory
     * <p>
     * CN: 持久化缓存数据
     *
     * @param pk primary key
     */
    void persist(PK pk);

    void persistAll();

    void persistAllBlock();


    void forEach(BiConsumer<PK, E> biConsumer);

    long size();

}
