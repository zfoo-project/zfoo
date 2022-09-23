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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author godotg
 * @version 3.0
 */
public class FixedSizeListFloat implements List<Float> {

    private final float[] array;

    public FixedSizeListFloat(int initialCapacity) {
        this.array = new float[initialCapacity];
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return ArrayUtils.isEmpty(array);
    }

    @Override
    public Float get(int index) {
        return array[index];
    }

    public float getRaw(int index) {
        return array[index];
    }

    @Override
    public Float set(int index, Float ele) {
        var old = array[index];
        array[index] = ele;
        return old;
    }

    public void set(int index, float ele) {
        array[index] = ele;
    }

    @Override
    public boolean contains(Object ele) {
        return ArrayUtils.toList(array).stream().anyMatch(it -> ele.equals(it));
    }

    @Override
    public Object[] toArray() {
        return ArrayUtils.toList(array).toArray();
    }

    @Override
    public <T> T[] toArray(T[] arrays) {
        ArrayUtils.toList(array).toArray(arrays);
        return arrays;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return ArrayUtils.toList(array).containsAll(collection);
    }

    @Override
    public int indexOf(Object ele) {
        return ArrayUtils.toList(array).indexOf(ele);
    }

    @Override
    public int lastIndexOf(Object ele) {
        return ArrayUtils.toList(array).lastIndexOf(ele);
    }

    @Override
    public Iterator<Float> iterator() {
        return ArrayUtils.toList(array).iterator();
    }

    @Override
    public ListIterator<Float> listIterator() {
        return ArrayUtils.toList(array).listIterator();
    }

    @Override
    public ListIterator<Float> listIterator(int index) {
        return ArrayUtils.toList(array).listIterator(index);
    }

    @Override
    public List<Float> subList(int fromIndex, int toIndex) {
        return ArrayUtils.toList(array).subList(fromIndex, toIndex);
    }

    @Override
    public boolean add(Float e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Float element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Float> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Float> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Float remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
