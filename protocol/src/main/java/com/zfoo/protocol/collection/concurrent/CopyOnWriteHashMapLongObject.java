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

package com.zfoo.protocol.collection.concurrent;

import io.netty.util.collection.LongObjectHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author godotg
 */
public class CopyOnWriteHashMapLongObject<V> implements Map<Long, V> {

    private final ReentrantLock lock = new ReentrantLock();
    private volatile LongObjectHashMap<V> map;

    public CopyOnWriteHashMapLongObject() {
        map = new LongObjectHashMap<>();
    }

    public CopyOnWriteHashMapLongObject(int initialCapacity) {
        map = new LongObjectHashMap<>(initialCapacity);
    }

    private LongObjectHashMap<V> newCopyMap() {
        var newMap = new LongObjectHashMap<V>();
        newMap.putAll(map);
        return newMap;
    }

    private void setNewMap(LongObjectHashMap<V> newMap) {
        map = newMap;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    public V getPrimitive(long key) {
        return map.get(key);
    }

    @Override
    public V put(Long key, V value) {
        lock.lock();
        try {
            var newMap = newCopyMap();
            var oldValue = newMap.put(key, value);
            setNewMap(newMap);
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        lock.lock();
        try {
            var newMap = newCopyMap();
            var oldValue = newMap.remove(key);
            setNewMap(newMap);
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends Long, ? extends V> m) {
        lock.lock();
        try {
            var newMap = newCopyMap();
            newMap.putAll(m);
            setNewMap(newMap);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            var newMap = newCopyMap();
            setNewMap(newMap);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<Long> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<Long, V>> entrySet() {
        return map.entrySet();
    }
}
