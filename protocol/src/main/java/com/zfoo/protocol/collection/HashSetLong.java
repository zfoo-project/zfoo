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

package com.zfoo.protocol.collection;

import io.netty.util.collection.LongObjectHashMap;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author godotg
 */
public class HashSetLong extends AbstractSet<Long> {

    private final LongObjectHashMap<Boolean> map;

    public HashSetLong() {
        map = new LongObjectHashMap<>();
    }

    public HashSetLong(int initialCapacity) {
        map = new LongObjectHashMap<>(initialCapacity);
    }

    @Override
    public Iterator<Long> iterator() {
        return map.keySet().iterator();
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
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(Long e) {
        return map.put(e, Boolean.TRUE) == null;
    }

    public boolean add(long e) {
        return map.put(e, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var obj = (HashSetLong) o;
        return Objects.equals(map, obj.map);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }
}
