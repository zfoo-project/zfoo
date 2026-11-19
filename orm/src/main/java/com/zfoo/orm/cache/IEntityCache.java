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
     * Load the entity from the database into the cache; returns null if not found.
     */
    @Nullable
    E load(PK pk);

    /**
     * Load the entity from the database into the cache; if not found, create a default instance with the given id and insert it into the database.
     * (Cannot be used with entities that have unique indexes.)
     */
    E loadOrCreate(PK pk);

    /**
     * Get the cached entity; returns null if it does not exist in the cache.
     */
    @Nullable
    E get(PK pk);

    /**
     * Update the cached entity; only refreshes the modification timestamp and writes to the database according to the persistence strategy.
     * <p>
     * The first update() call records the thread ID; subsequent calls from a different thread will trigger a thread-safety warning.
     */
    void update(E entity);

    /**
     * Same as update(), except it does not check for thread consistency.
     */
    void updateUnsafe(E entity);

    /**
     * Update the cached entity and write it to the database immediately.
     */
    void updateNow(E entity);

    /**
     * Same as updateNow(), except it does not check for thread consistency.
     */
    void updateUnsafeNow(E entity);

    /**
     * Remove the entity from the cache only; does not delete it from the database.
     *
     * @param pk primary key
     */
    void invalidate(PK pk);

    /**
     * Persist the cached entity with the given primary key to the database.
     *
     * @param pk primary key
     */
    void persist(PK pk);

    void persistAll();

    void persistAllBlock();


    void forEach(BiConsumer<PK, E> biConsumer);

    long size();

}
