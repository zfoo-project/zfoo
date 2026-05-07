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

import java.util.function.BiConsumer;

/**
 * @author godotg
 */
public interface IEntityCache<PK extends Comparable<PK>, E extends IEntity<PK>> {

    /**
     * EN: Load data from the database to the cache and return null if the database does not exist.
     * Load entity from database into cache; returns null if not found.
     */
    @Nullable
    E load(PK pk);

    /**
     * EN: Load data from the database into the cache, and if the database does not exist,
     * return a default example with the id as the passed in value and put it into storage.
     * <p>
     * Load entity from DB into cache; if not found, creates a default instance with the given id and inserts it. (Cannot be used with unique-indexed entities)
     */
    E loadOrCreate(PK pk);

    /**
     * EN: Get the data in the cache or returns null if it does not exist in the cache.
     * Get the cached entity; returns null if not cached.
     */
    @Nullable
    E get(PK pk);

    /**
     * EN: Update the timestamp of the cache in the memory.
     * The first update() will record the thread number, and a thread-safe warning will be given if the thread number of next update() is not equal with the first time
     * <p>
     * Update cached entity; only refreshes the timestamp and writes to DB according to the persistence strategy.
     * The first update() call records the thread ID; subsequent calls from a different thread will trigger a thread-safety warning.
     */
    void update(E entity);

    /**
     * EN: The similar as update() will not check whether the updated thread number is equal.
     * <p>
     * Same as update(), but does not check for thread consistency.
     */
    void updateUnsafe(E entity);

    /**
     * EN: Update the cached entity and write it to the database immediately
     * <p>
     * Update cached entity and immediately persist to database.
     */
    void updateNow(E entity);

    /**
     * EN: The similar as updateNow() will not check whether the updated thread number is equal.
     * <p>
     * Same as updateNow(), but does not check for thread consistency.
     */
    void updateUnsafeNow(E entity);

    /**
     * EN: The data in the database will not be deleted, only the cached entity will be deleted.
     * <p>
     * Remove from cache only; does not delete from database.
     *
     * @param pk primary key
     */
    void invalidate(PK pk);

    /**
     * EN: Persistence cached entity in memory
     * <p>
     * Persist all cached data to database.
     *
     * @param pk primary key
     */
    void persist(PK pk);

    void persistAll();

    void persistAllBlock();


    void forEach(BiConsumer<PK, E> biConsumer);

    long size();

}
