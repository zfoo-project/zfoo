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

package com.zfoo.orm.entity.wrapper;

import com.zfoo.orm.anno.EntityCache;
import com.zfoo.orm.anno.Id;
import com.zfoo.orm.anno.Version;
import com.zfoo.orm.model.IEntity;
import org.bson.types.ObjectId;

/**
 * @author godotg
 */
@EntityCache
public class ObjectIdEntity implements IEntity<ObjectId> {

    @Id
    private ObjectId id;

    private String message;

    @Version
    private long version;

    public ObjectIdEntity() {
    }

    public ObjectIdEntity(ObjectId id, String message, long version) {
        this.id = id;
        this.message = message;
        this.version = version;
    }

    @Override
    public ObjectId id() {
        return id;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ObjectIdEntity{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", version=" + version +
                '}';
    }
}
