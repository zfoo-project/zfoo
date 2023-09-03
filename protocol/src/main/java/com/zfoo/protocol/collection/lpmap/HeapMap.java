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

package com.zfoo.protocol.collection.lpmap;

import io.netty.util.collection.LongObjectHashMap;

import java.util.function.BiConsumer;

/**
 * @author godotg
 * @version 3.0
 */
public class HeapMap<V> implements LpMap<V> {

    protected LongObjectHashMap<V> map;

    protected long maxIndex = 0;

    public HeapMap() {
        this(128);
    }

    public HeapMap(int initialCapacity) {
        map = new LongObjectHashMap<>(initialCapacity);
    }


    @Override
    public V put(long key, V value) {
        checkKey(key);

        if (key > maxIndex) {
            maxIndex = key;
        }

        return map.put(key, value);
    }

    @Override
    public V delete(long key) {
        checkKey(key);
        if (key > maxIndex) {
            return null;
        }

        return map.remove(key);
    }

    @Override
    public V get(long key) {
        checkKey(key);
        return map.get(key);
    }

    @Override
    public long getMaxIndex() {
        return maxIndex;
    }

    @Override
    public long getIncrementIndex() {
        maxIndex++;
        return maxIndex;
    }

    @Override
    public void clear() {
        maxIndex = 0;
        map.clear();
    }

    @Override
    public void forEach(BiConsumer<Long, V> biConsumer) {
        map.forEach(biConsumer);
    }
}
