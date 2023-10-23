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
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.storage.model.IdDef;
import com.zfoo.storage.model.IndexDef;
import io.netty.util.collection.IntObjectHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author godotg
 */
public class StorageInt<K, V> extends AbstractStorage<K, V> {

    private IntObjectHashMap<V> dataMap;

    public StorageInt(Class<?> clazz, IdDef idDef, Map<String, IndexDef> indexDefMap, List<?> values) {
        super(clazz, idDef, indexDefMap, values);

        this.dataMap = new IntObjectHashMap<>(values.size());
        for (var value : values) {
            var id = (Integer) ReflectionUtils.getField(idDef.getField(), value);
            @SuppressWarnings("unchecked")
            var v = (V) value;
            dataMap.put(id, v);
        }
    }


    @Override
    public boolean contain(K id) {
        return contain((int) id);
    }

    @Override
    public boolean contain(int id) {
        return dataMap.containsKey(id);
    }

    @Override
    public boolean contain(long id) {
        return contain((int) id);
    }

    @Override
    public V get(K id) {
        return get((int) id);
    }

    @Override
    public V get(int id) {
        V result = dataMap.get(id);
        AssertionUtils.notNull(result, "The static resource represented as [id:{}] in the static resource [resource:{}] does not exist", id, clazz.getSimpleName());
        return result;
    }

    @Override
    public V get(long id) {
        return get((int) id);
    }

    @Override
    public Collection<V> getAll() {
        return dataMap.values().stream().toList();
    }

    @Override
    public Map<K, V> getData() {
        @SuppressWarnings("unchecked")
        var map = (Map<K, V>) Collections.unmodifiableMap(dataMap);
        return map;
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

}
