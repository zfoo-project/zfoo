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

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ConcurrentHeapMap<V extends IPacket> implements LpMap<V> {

    private ConcurrentNavigableMap<Long, V> map = new ConcurrentSkipListMap<>();

    private AtomicLong maxIndexAtomic = new AtomicLong(0);

    @Override
    public long insert(V value) {
        var key = maxIndexAtomic.incrementAndGet();
        map.put(key, value);
        return key;
    }

    @Override
    public V put(long key, V value) {
        checkKey(key);
        return map.put(key, value);
    }

    @Override
    public V delete(long key) {
        checkKey(key);
        return map.remove(key);
    }

    @Override
    public V get(long key) {
        checkKey(key);
        return map.get(key);
    }

    @Override
    public long getMaxIndex() {
        return maxIndexAtomic.get();
    }
}
