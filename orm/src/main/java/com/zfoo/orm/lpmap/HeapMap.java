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
import io.netty.util.collection.LongObjectHashMap;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class HeapMap<V extends IPacket> implements LpMap<V> {

    protected LongObjectHashMap<V> map;

    /**
     * 没有被使用的key
     */
    private Queue<Long> freeKeyQueue = new LinkedList<>();

    private long index = 0;

    public HeapMap(int initialCapacity) {
        map = new LongObjectHashMap<>(initialCapacity);
    }

    @Override
    public long insert(V packet) {
        if (freeKeyQueue.isEmpty()) {
            map.put(++index, packet);
            return index;
        } else {
            var freeKey = freeKeyQueue.poll();
            map.put(freeKey, packet);
            return freeKey;
        }
    }

    @Override
    public V put(long key, V packet) {
        checkKey(key);
        if (key <= index) {
            return map.put(key, packet);
        } else {
            for (var i = index + 1; i < key; i++) {
                freeKeyQueue.add(i);
            }
            index = key;
            map.put(key, packet);
            return null;
        }
    }

    @Override
    public V delete(long key) {
        checkKey(key);
        if (key > index) {
            return null;
        } else {
            var previousValue = map.remove(key);
            freeKeyQueue.add(key);
            return previousValue;
        }
    }

    @Override
    public V get(long key) {
        checkKey(key);
        return map.get(key);
    }
}
