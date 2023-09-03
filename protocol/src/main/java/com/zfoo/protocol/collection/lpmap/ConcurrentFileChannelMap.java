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
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author godotg
 * @version 3.0
 */
public class ConcurrentFileChannelMap<V> implements LpMap<V>, Closeable {


    private final FileChannelMap<V> fileChannelMap;

    public ConcurrentFileChannelMap(String dbPath, Class<V> clazz) {
        fileChannelMap = new FileChannelMap<>(dbPath, clazz);
    }

    @Override
    public synchronized V put(long key, V value) {
        return fileChannelMap.put(key, value);
    }

    @Override
    public synchronized V putIfAbsent(long key, V packet) {
        return fileChannelMap.put(key, packet);
    }

    @Override
    public synchronized V delete(long key) {
        return fileChannelMap.delete(key);
    }

    @Override
    public synchronized V get(long key) {
        return fileChannelMap.get(key);
    }

    public synchronized List<V> getFrom(long startKey, long endKey) {
        return fileChannelMap.getFrom(startKey, endKey);
    }

    @Override
    public synchronized long getMaxIndex() {
        return fileChannelMap.getMaxIndex();
    }

    @Override
    public synchronized long getIncrementIndex() {
        return fileChannelMap.getIncrementIndex();
    }

    @Override
    public synchronized void clear() {
        fileChannelMap.clear();
    }

    @Override
    public synchronized void close() throws IOException {
        fileChannelMap.close();
    }

    @Override
    public synchronized void forEach(BiConsumer<Long, V> biConsumer) {
        fileChannelMap.forEach(biConsumer);
    }

}
