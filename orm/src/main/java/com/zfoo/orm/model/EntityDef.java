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

package com.zfoo.orm.model;

import com.zfoo.orm.config.PersisterStrategy;

import java.util.Map;

/**
 * @author godotg
 */
public class EntityDef {

    // 线程安全指的是内部没有使用集合或者使用的集合全部支持并发操作
    private boolean threadSafe;

    private int cacheSize;

    private long expireMillisecond;

    private PersisterStrategy persisterStrategy;

    private Map<String, IndexDef> indexDefMap;

    private Map<String, IndexTextDef> indexTextDefMap;


    public static EntityDef valueOf(boolean threadSafe, int cacheSize, long expireMillisecond
            , PersisterStrategy persisterStrategy, Map<String, IndexDef> indexDefMap, Map<String, IndexTextDef> indexTextDefMap) {
        var entityDef = new EntityDef();
        entityDef.threadSafe = threadSafe;
        entityDef.cacheSize = cacheSize;
        entityDef.expireMillisecond = expireMillisecond;
        entityDef.persisterStrategy = persisterStrategy;
        entityDef.indexDefMap = indexDefMap;
        entityDef.indexTextDefMap = indexTextDefMap;
        return entityDef;
    }

    public boolean isThreadSafe() {
        return threadSafe;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public long getExpireMillisecond() {
        return expireMillisecond;
    }

    public PersisterStrategy getPersisterStrategy() {
        return persisterStrategy;
    }

    public Map<String, IndexDef> getIndexDefMap() {
        return indexDefMap;
    }

    public Map<String, IndexTextDef> getIndexTextDefMap() {
        return indexTextDefMap;
    }
}
