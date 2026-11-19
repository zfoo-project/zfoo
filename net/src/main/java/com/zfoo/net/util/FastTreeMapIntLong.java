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

package com.zfoo.net.util;

import com.zfoo.protocol.collection.ArrayUtils;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author godotg
 */
public class FastTreeMapIntLong {

    private int[] keys;
    private long[] values;

    public FastTreeMapIntLong(TreeMap<Integer, Long> treeMap) {
        var size = treeMap.size();
        keys = new int[size];
        values = new long[size];

        // TreeMap iteration is ordered, placing entries directly into an array yields a sorted ascending array
        var index = 0;
        for (var entry : treeMap.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            keys[index] = key;
            values[index] = value;
            index++;
        }
    }

    public boolean contains(int key) {
        return indexOf(key) >= 0;
    }

    public long get(int key) {
        var index = indexOf(key);
        if (index < 0) {
            throw new NoSuchElementException();
        }
        return values[index];
    }

    public long getByIndex(int index) {
        Objects.checkIndex(index, values.length);
        return values[index];
    }

    /**
     * Binary search for the given key in an ascending sorted array
     *
     * @param key the value to search for
     * @return the array index of the found value, or -1 if not found
     */
    public int indexOf(int key) {
        var low = 0;
        var high = keys.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (keys[mid] == key) {
                return mid;
            } else if (keys[mid] < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the entry with the least key greater than or equal to the specified key;
     * if {@code key} is greater than the maximum key, returns the last index;
     * if the array is empty, returns -1.
     */
    public int indexOfNearestCeilingKey(int key) {
        if (ArrayUtils.isEmpty(keys)) {
            return -1;
        }

        var size = keys.length;
        // Target is less than or equal to the first element
        if (key <= keys[0]) {
            return 0;
        }
        // Target is greater than the last element
        if (key > keys[size - 1]) {
            return size - 1;
        }

        // Binary search for the target
        var low = 0;
        var high = size - 1;
        var nearestIndex = -1;

        while (low <= high) {
            var mid = low + (high - low) / 2;
            if (keys[mid] == key) {
                nearestIndex = mid;
                break;
            } else if (keys[mid] < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
                nearestIndex = mid;
            }
        }

        // Fallback if no ceiling index was matched
        if (nearestIndex == -1) {
            return 0;
        }

        return nearestIndex;
    }

    public long getValueByCeilingKey(int key) {
        var index = indexOfNearestCeilingKey(key);
        if (index < 0) {
            throw new NoSuchElementException();
        }
        return values[index];
    }

}
