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

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ConcurrentFileChannelHeapMap<V extends IPacket> implements LpMap<V> {

    private ReentrantLock fileChannelLock = new ReentrantLock();

    private FileChannelMap<V> fileChannelMap;

    private ConcurrentHeapMap<V> concurrentHeapMap;

    public ConcurrentFileChannelHeapMap(String dbPath, int initialCapacity, Class<V> clazz) {
        fileChannelMap = new FileChannelMap<>(dbPath, clazz);
        concurrentHeapMap = new ConcurrentHeapMap<>();

        load();
    }

    @Override
    public V put(long key, V value) {
        fileChannelLock.lock();
        try {
            fileChannelMap.put(key, value);
        } finally {
            fileChannelLock.unlock();
        }
        return concurrentHeapMap.put(key, value);
    }

    @Override
    public V delete(long key) {
        fileChannelLock.lock();
        try {
            fileChannelMap.delete(key);
        } finally {
            fileChannelLock.unlock();
        }
        return concurrentHeapMap.delete(key);
    }

    @Override
    public V get(long key) {
        return concurrentHeapMap.get(key);
    }

    @Override
    public long getMaxIndex() {
        return concurrentHeapMap.getMaxIndex();
    }

    @Override
    public long getIncrementIndex() {
        return concurrentHeapMap.getIncrementIndex();
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
            concurrentHeapMap.put(key, value);
        }
    }

    @Override
    public void forEach(BiConsumer<Long, V> biConsumer) {
        concurrentHeapMap.forEach(biConsumer);
    }
}
