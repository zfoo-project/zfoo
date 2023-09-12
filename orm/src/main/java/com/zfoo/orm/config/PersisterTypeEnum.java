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

package com.zfoo.orm.config;

import com.zfoo.orm.cache.EntityCache;
import com.zfoo.orm.cache.persister.CronOrmPersister;
import com.zfoo.orm.cache.persister.IOrmPersister;
import com.zfoo.orm.cache.persister.TimeOrmPersister;
import com.zfoo.orm.model.EntityDef;
import com.zfoo.protocol.util.StringUtils;

/**
 * @author godotg
 */
public enum PersisterTypeEnum {

    QUEUE {
        @Override
        public IOrmPersister createPersister(EntityDef entityDef, EntityCache<?, ?> entityCaches) {
            return null;
        }
    },
    CRON {
        @Override
        public IOrmPersister createPersister(EntityDef entityDef, EntityCache<?, ?> entityCaches) {
            return new CronOrmPersister(entityDef, entityCaches);
        }
    },
    TIME {
        @Override
        public IOrmPersister createPersister(EntityDef entityDef, EntityCache<?, ?> entityCaches) {
            return new TimeOrmPersister(entityDef, entityCaches);
        }
    };


    public static PersisterTypeEnum getPersisterType(String persisterType) {
        for (PersisterTypeEnum persister : values()) {
            if (persister.name().equalsIgnoreCase(persisterType)) {
                return persister;
            }
        }
        throw new IllegalArgumentException(StringUtils.format("无效的持久化类型[persisterType:{}]", persisterType));
    }

    public abstract IOrmPersister createPersister(EntityDef entityDef, EntityCache<?, ?> entityCaches);

}
