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
import java.util.function.BiConsumer;

/**
 * @author godotg
 * @version 3.0
 */
public class FileChannelHeapMap<V> implements LpMap<V>, Closeable {

    private final FileChannelMap<V> fileChannelMap;

    private final HeapMap<V> heapMap;

    public FileChannelHeapMap(String dbPath, int initialCapacity, Class<V> clazz) {
        fileChannelMap = new FileChannelMap<>(dbPath, clazz);
        heapMap = new HeapMap<>(initialCapacity);

        fileChannelMap.forEach((key, v) -> heapMap.put(key, v));
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

    @Override
    public long getIncrementIndex() {
        return heapMap.getIncrementIndex();
    }

    @Override
    public void clear() {
        fileChannelMap.clear();
        heapMap.clear();
    }

    @Override
    public void close() throws IOException {
        fileChannelMap.close();
        heapMap.clear();
    }

    @Override
    public void forEach(BiConsumer<Long, V> biConsumer) {
        heapMap.forEach(biConsumer);
    }

}
