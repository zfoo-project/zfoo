/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.util.math;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author godotg
 * @version 3.0
 */
public class RandomSelector<T> {

    private int cursor = 0;

    private final TreeMap<Integer, T> elementMap = new TreeMap<>();

    public void addElement(@Nullable T value, int weight) {
        if (value == null || weight <= 0) {
            return;
        }

        cursor += weight;
        elementMap.put(cursor, value);
    }

    public void clear() {
        elementMap.clear();
        cursor = 0;
    }

    public int size() {
        return elementMap.size();
    }

    public T select() {
        if (cursor <= 0) {
            throw new IllegalStateException("全部的权重是0");
        }
        if (elementMap.isEmpty()) {
            throw new IllegalStateException("选择的元素为空");
        }

        var randomInt = RandomUtils.randomInt(cursor) + 1;
        return elementMap.ceilingEntry(randomInt).getValue();
    }

    public List<T> select(int count) {
        List<T> resultList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            resultList.add(select());
        }
        return resultList;
    }

}
