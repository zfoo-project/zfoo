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

package com.zfoo.orm.lpmap;

import com.zfoo.protocol.IPacket;

import java.util.function.BiConsumer;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class FileChannelHeapMap<V extends IPacket> implements LpMap<V> {

    private FileChannelMap<V> fileChannelMap;

    private HeapMap<V> heapMap;

    public FileChannelHeapMap(String dbPath, int initialCapacity, Class<V> clazz) {
        fileChannelMap = new FileChannelMap<>(dbPath, clazz);
        heapMap = new HeapMap<>(initialCapacity);

        load();
    }

    @Override
    public long insert(V value) {
        var key = fileChannelMap.insert(value);
        heapMap.put(key, value);
        return key;
    }

    @Override
    public V put(long key, V value) {
        fileChannelMap.put(key, value);
        return heapMap.put(key, value);
    }

    @Override
    public V delete(long key) {
        fileChannelMap.delete(key);
        return heapMap.delete(key);
    }

    @Override
    public V get(long key) {
        return heapMap.get(key);
    }

    @Override
    public long getMaxIndex() {
        return heapMap.getMaxIndex();
    }

    private void load() {
        var maxIndex = fileChannelMap.getMaxIndex();
        if (maxIndex <= 0) {
            return;
        }

        for (var key = 1; key <= maxIndex; key++) {
            var value = fileChannelMap.get(key);
            if (value == null) {
                continue;
            }
            heapMap.put(key, value);
        }
    }

    @Override
    public void forEach(BiConsumer<Long, V> biConsumer) {
        heapMap.forEach(biConsumer);
    }
}
