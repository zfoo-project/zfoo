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

        // TreeMap遍历是有序的，直接放入数组中就能直接得到从小到大的有序数组
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
     * 在一个从小到大的升序数组中，使用二分查找算法搜索给定的key
     *
     * @param key 需要搜索的值
     * @return 返回搜索到值的数组下标索引，如果没有查找到返回-1
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
     * returns the index of entry for the least key greater than the specified key;
     * if key greater than the max key, returns the last index of entry;
     * if no such entry exists, returns the index of entry for the least key greater than the specified key;
     * if no such entry exists, returns -1.
     */
    public int indexOfNearestCeilingKey(int key) {
        if (ArrayUtils.isEmpty(keys)) {
            return -1;
        }

        var size = keys.length;
        // 目标数小于或等于数组的第一个元素
        if (key <= keys[0]) {
            return 0;
        }
        // 目标数大于数组的最后元素
        if (key > keys[size - 1]) {
            return size - 1;
        }

        // 二分查找目标数
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

        // 如果找到了目标数，那么它的邻近最大值就是目标数本身
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
