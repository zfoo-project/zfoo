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

package com.zfoo.storage.manager;

import com.zfoo.protocol.util.AssertionUtils;
import io.netty.util.collection.LongObjectHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author godotg
 * @version 3.0
 */
public class StorageLong<K, V> extends StorageObject<K, V> {

    private LongObjectHashMap<V> dataMap;

    public StorageLong(StorageObject<K, V> storage) {
        this.dataMap = new LongObjectHashMap<V>(storage.size());
        this.dataMap.putAll((Map<? extends Long, ? extends V>) storage.getData());
        super.indexMap = storage.indexMap;
        super.uniqueIndexMap = storage.uniqueIndexMap;
        super.clazz = storage.clazz;
        super.idDef = storage.idDef;
        super.indexDefMap = storage.indexDefMap;
        super.recycle = storage.recycle;
        storage.recycleStorage();
    }

    @Override
    public boolean contain(K key) {
        return contain((long) key);
    }

    @Override
    public boolean contain(int key) {
        return contain((long) key);
    }

    @Override
    public boolean contain(long key) {
        return dataMap.containsKey(key);
    }

    @Override
    public V get(K id) {
        return get((long) id);
    }

    @Override
    public V get(int id) {
        return get((long) id);
    }

    @Override
    public V get(long id) {
        V result = dataMap.get(id);
        AssertionUtils.notNull(result, "The static resource represented as [id:{}] in the static resource [resource:{}] does not exist", id, clazz.getSimpleName());
        return result;
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    @Override
    public void recycleStorage() {
        super.recycleStorage();
        dataMap = null;
    }

    @Override
    public Collection<V> getAll() {
        return dataMap.values();
    }

    @Override
    public Map<K, V> getData() {
        return (Map<K, V>) Collections.unmodifiableMap(dataMap);
    }

}
