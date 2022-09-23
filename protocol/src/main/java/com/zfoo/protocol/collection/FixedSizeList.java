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
 * 初始化就确定了大小的List，不可添加和删除，但是可以set，就像一个数组一样
 *
 * @author godotg
 * @version 3.0
 */
public class FixedSizeList<E> implements List<E> {

    private final Object[] array;

    public FixedSizeList(int initialCapacity) {
        this.array = new Object[initialCapacity];
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
    public E get(int index) {
        return (E) array[index];
    }

    @Override
    public E set(int index, E ele) {
        var old = array[index];
        array[index] = ele;
        return (E) old;
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
    public Iterator<E> iterator() {
        return (Iterator<E>) ArrayUtils.toList(array).iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return (ListIterator<E>) ArrayUtils.toList(array).listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return (ListIterator<E>) ArrayUtils.toList(array).listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return (List<E>) ArrayUtils.toList(array).subList(fromIndex, toIndex);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
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
