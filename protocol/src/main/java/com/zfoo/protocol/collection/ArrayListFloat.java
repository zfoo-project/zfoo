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

import java.util.*;

/**
 * @author godotg
 */
public class ArrayListFloat implements List<Float> {

    private float[] array;
    private int size;

    public ArrayListFloat(int initialCapacity) {
        this.array = new float[initialCapacity];
    }

    public ArrayListFloat(float[] array) {
        this.array = array;
        this.size = array.length;
    }

    public void fromList(List<Float> list) {
        if (CollectionUtils.isEmpty(list)) {
            this.size = 0;
            return;
        }
        this.size = list.size();
        this.array = ArrayUtils.floatToArray(list);
    }

    public List<Float> toList() {
        if (isEmpty()) {
            return new ArrayList<>();
        }
        return ArrayUtils.toList(toArrayPrimitive());
    }

    public float[] toArrayPrimitive() {
        return Arrays.copyOf(array, size);
    }

    private int getCapacity() {
        return array.length - size;
    }

    private void ensureCapacity(int capacity) {
        var remainCapacity = getCapacity();
        if (capacity > remainCapacity) {
            array = Arrays.copyOf(array, size + capacity + 16);
        }
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of range, size = " + size);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object ele) {
        return indexOfPrimitive(ArrayUtils.floatValue((Float) ele)) >= 0;
    }

    @Override
    public Float get(int index) {
        return getPrimitive(index);
    }

    public float getPrimitive(int index) {
        Objects.checkIndex(index, size);
        return array[index];
    }

    @Override
    public Float set(int index, Float ele) {
        var old = array[index];
        setPrimitive(index, ArrayUtils.floatValue(ele));
        return old;
    }

    public void setPrimitive(int index, float ele) {
        Objects.checkIndex(index, size);
        array[index] = ele;
    }

    @Override
    public boolean add(Float ele) {
        return addPrimitive(ArrayUtils.floatValue(ele));
    }

    public boolean addPrimitive(float ele) {
        ensureCapacity(1);
        array[size++] = ele;
        return true;
    }

    @Override
    public void add(int index, Float ele) {
        addPrimitive(index, ArrayUtils.floatValue(ele));
    }

    public void addPrimitive(int index, float ele) {
        checkIndexForAdd(index);
        ensureCapacity(1);
        System.arraycopy(array, index, array, index + 1, size - index);
        array[index] = ele;
        size++;
    }

    @Override
    public boolean addAll(Collection<? extends Float> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        ensureCapacity(collection.size());
        for (var ele : collection) {
            add(ele);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Float> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        checkIndexForAdd(index);
        ensureCapacity(collection.size());
        var appendArray = ArrayUtils.floatToArray(new ArrayList<>(collection));
        var appendLength = appendArray.length;

        var moved = size - index;
        if (moved > 0) {
            System.arraycopy(array, index, array, index + appendLength, moved);
        }

        System.arraycopy(appendArray, 0, array, index, appendLength);
        size += appendLength;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        var index = indexOf(o);
        if (index < 0) {
            return false;
        }
        remove(index);
        return true;
    }

    @Override
    public Float remove(int index) {
        return removeAt(index);
    }

    public float removeAt(int index) {
        Objects.checkIndex(index, size);
        var oldValue = array[index];
        var newSize = size - 1;
        if (newSize > index) {
            System.arraycopy(array, index + 1, array, index, newSize - index);
        }
        size--;
        return oldValue;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return false;
        }
        var list = toList();
        var flag = list.removeAll(collection);
        fromList(list);
        return flag;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        var list = toList();
        var flag = list.retainAll(collection);
        fromList(list);
        return flag;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public Object[] toArray() {
        return toList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] arrays) {
        return toList().toArray(arrays);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return toList().containsAll(collection);
    }

    public int indexOfPrimitive(float ele) {
        for (var i = 0; i < size; i++) {
            if (array[i] == ele) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOfPrimitive(float ele) {
        for (var i = size - 1; i >= 0; i--) {
            if (array[i] == ele) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(Object ele) {
        return indexOfPrimitive(ArrayUtils.floatValue((Float) ele));
    }

    @Override
    public int lastIndexOf(Object ele) {
        return lastIndexOfPrimitive(ArrayUtils.floatValue((Float) ele));
    }

    @Override
    public Iterator<Float> iterator() {
        return new FastIterator();
    }

    @Override
    public ListIterator<Float> listIterator() {
        return new FastListIterator(0);
    }

    @Override
    public ListIterator<Float> listIterator(int index) {
        return new FastListIterator(index);
    }

    @Override
    public List<Float> subList(int fromIndex, int toIndex) {
        return ArrayUtils.toList(array).subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append('[');
        for (var i = 0; i < size; i++) {
            builder.append(array[i]);
            if (i < size - 1) {
                builder.append(", ");
            }
        }
        builder.append(']');
        return builder.toString();
    }

    private class FastIterator implements Iterator<Float> {
        int cursor;       // index of next element to return
        int lastCursor = -1; // index of last element returned; -1 if no such

        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public Float next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            lastCursor = cursor;
            return array[cursor++];
        }

        @Override
        public void remove() {
            if (lastCursor < 0) {
                throw new IllegalStateException();
            }
            removeAt(lastCursor);
            cursor = lastCursor;
            lastCursor = -1;
        }
    }

    private class FastListIterator extends FastIterator implements ListIterator<Float> {
        FastListIterator(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public Float previous() {
            if (cursor <= 0) {
                throw new NoSuchElementException();
            }
            cursor--;
            lastCursor = cursor;
            return array[cursor];
        }

        @Override
        public void set(Float e) {
            if (lastCursor < 0) {
                throw new IllegalStateException();
            }
            setPrimitive(lastCursor, ArrayUtils.floatValue(e));
        }

        @Override
        public void add(Float e) {
            addPrimitive(cursor++, e);
            lastCursor = -1;
        }
    }
}
