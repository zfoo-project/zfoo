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

package com.zfoo.protocol.model;


import java.util.Objects;

/**
 * Key-value pair; values can only be set via the constructor
 *
 * @param <V> value type
 * @author godotg
 */
public class PairLong<V> {

    private long key;
    private V value;

    public PairLong() {

    }

    /**
     * Constructor
     *
     * @param key   the key
     * @param value the value
     */
    public PairLong(long key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the key
     *
     * @return the key
     */
    public long getKey() {
        return this.key;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public V getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairLong<?> pairLong = (PairLong<?>) o;
        return key == pairLong.key && Objects.equals(value, pairLong.value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(key);
    }

    @Override
    public String toString() {
        return "Pair [key=" + key + ", value=" + value + "]";
    }
}
