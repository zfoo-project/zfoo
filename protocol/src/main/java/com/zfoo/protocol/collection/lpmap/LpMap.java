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

import com.zfoo.protocol.exception.RunException;

import java.util.function.BiConsumer;

/**
 * 类型固定的map，key为long，value为Object
 * 其中long必须大于等于0，value可以为null
 *
 * @author godotg
 */
public interface LpMap<V> {

    /**
     * @param packet the previous value associated with key, or null if there was no mapping for key.
     */
    V put(long key, V packet);

    default V putIfAbsent(long key, V packet) {
        var v = get(key);
        if (v == null) {
            v = put(key, packet);
        }
        return v;
    }

    /**
     * @return 返回被删除的那个值
     */
    V delete(long key);

    V get(long key);

    long getMaxIndex();

    long getIncrementIndex();

    void clear();

    void forEach(BiConsumer<Long, V> biConsumer);

    default void checkKey(long key) {
        if (key < 0) {
            throw new RunException("key[{}]只能为大于等于0的正数", key);
        }
    }
}
