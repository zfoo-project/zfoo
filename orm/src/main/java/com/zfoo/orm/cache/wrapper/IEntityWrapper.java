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

package com.zfoo.orm.cache.wrapper;


import com.zfoo.orm.model.IEntity;

/**
 * @author godotg
 */
public interface IEntityWrapper<PK extends Comparable<PK>, E extends IEntity<PK>> {

    E newEntity(PK id);

    /**
     * Name of the version field
     */
    String versionFieldName();

    /**
     * Document write version; getter and setter for the version field.
     * <p>
     * On write, compares the entity's version with the DB version; if they differ the write is rejected; if equal, data is written and version is incremented.
     * Prevents multiple servers from modifying the same document simultaneously, ensuring data consistency in a distributed environment.
     * In a distributed environment, stateful servers exist; a gateway may route a user to server A, but after adding server B, the next request might go to B.
     * Even with consistent hashing, there is still a chance that some requests are routed to different servers.
     * The version number ensures only one server can write to the database at a time.
     * This is a fallback operation; it rarely occurs in production. It is the last line of defense for data consistency in high-performance stateful servers.
     * MongoDB updates are atomic; under concurrency only one write wins. The second writer's version will be stale and rejected.
     * When a version mismatch is detected on write, invalidate the cache and reload from DB.
     * <p>
     * For high performance in a distributed system you need stateful servers, but stateful servers face this multi-writer consistency dilemma — an ancient unsolved trade-off.
     */
    long gvs(E entity);

    void svs(E entity, long vs);

}
