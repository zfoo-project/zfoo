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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author godotg
 * @version 3.0
 */
public class FixedHashSetDouble extends AbstractSet<Double> {

    private final double[] array;
    private final boolean[] existArray;

    public FixedHashSetDouble(FixedSizeListDouble list) {
        var length = list.size();
        this.array = new double[length];
        this.existArray = new boolean[length];
        var hashCollisionList = new ArrayList<Double>();
        for (var i = 0; i < length; i++) {
            var ele = list.getRaw(i);
            var hash = Math.abs((int) ele % length);
            if (existArray[hash]) {
                hashCollisionList.add(ele);
            } else {
                array[hash] = ele;
                existArray[hash] = true;
            }
        }
        if (CollectionUtils.isNotEmpty(hashCollisionList)) {
            for (int i = 0, j = 0; i < length; i++) {
                if (hashCollisionList.size() == j) {
                    break;
                }
                if (existArray[i]) {
                    continue;
                }
                array[i] = hashCollisionList.get(j++);
                existArray[i] = true;
            }
        }
    }

    @Override
    public boolean contains(Object ele) {
        var e = (Double) ele;
        return contains(e.doubleValue());
    }

    public boolean contains(double ele) {
        var hash = Math.abs((int) ele % array.length);
        if (array[hash] == ele && existArray[hash]) {
            return true;
        }
        for (var i = 0; i < array.length; i++) {
            if (array[i] == ele && existArray[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        var length = 0;
        for (var i = 0; i < array.length; i++) {
            if (existArray[i]) {
                length++;
            }
        }
        return length;
    }

    @Override
    public Iterator<Double> iterator() {
        var list = new ArrayList<Double>();
        for (var i = 0; i < array.length; i++) {
            if (existArray[i]) {
                list.add(array[i]);
            }
        }
        return list.iterator();
    }

    @Override
    public boolean add(Double e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
