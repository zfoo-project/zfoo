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


import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * @author godotg
 * @version 3.0
 */
public class ConcurrentFileChannelHeapMap<V> implements LpMap<V>, Closeable {

    private final ReentrantLock fileChannelLock = new ReentrantLock();

    private final FileChannelMap<V> fileChannelMap;

    private final ConcurrentHeapMap<V> concurrentHeapMap;

    public ConcurrentFileChannelHeapMap(String dbPath, Class<V> clazz) {
        fileChannelMap = new FileChannelMap<>(dbPath, clazz);
        concurrentHeapMap = new ConcurrentHeapMap<>();

        fileChannelMap.forEach((key, v) -> concurrentHeapMap.put(key, v));
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
    public V putIfAbsent(long key, V packet) {
        var previousValue = concurrentHeapMap.putIfAbsent(key, packet);
        if (previousValue == null) {
            previousValue = put(key, packet);
        }
        return previousValue;
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

    @Override
    public void clear() {
        fileChannelLock.lock();
        try {
            fileChannelMap.clear();
            concurrentHeapMap.clear();
        } finally {
            fileChannelLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        fileChannelLock.lock();
        try {
            fileChannelMap.close();
            concurrentHeapMap.clear();
        } finally {
            fileChannelLock.unlock();
        }
    }

    @Override
    public void forEach(BiConsumer<Long, V> biConsumer) {
        concurrentHeapMap.forEach(biConsumer);
    }

}
