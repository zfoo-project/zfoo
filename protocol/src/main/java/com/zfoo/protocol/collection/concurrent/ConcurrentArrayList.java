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

package com.zfoo.protocol.collection.concurrent;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author godotg
 */
public class ConcurrentArrayList<E> implements List<E> {

    private final ReentrantLock lock;

    private final ArrayList<E> list;

    public ConcurrentArrayList() {
        this.lock = new ReentrantLock();
        this.list = new ArrayList<>();
    }

    public ConcurrentArrayList(int initialCapacity) {
        this.lock = new ReentrantLock();
        this.list = new ArrayList<>(initialCapacity);
    }


    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<E> clearAndReturn() {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            var newList = (ArrayList<E>) list.clone();
            list.clear();
            return newList;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        lock.lock();
        try {
            return list.contains(o);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            var newList = (ArrayList<E>) list.clone();
            return newList.iterator();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        lock.lock();
        try {
            return list.toArray();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        lock.lock();
        try {
            return list.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(E e) {
        lock.lock();
        try {
            return list.add(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        lock.lock();
        try {
            return list.remove(o);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        lock.lock();
        try {
            return list.containsAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        lock.lock();
        try {
            return list.addAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        lock.lock();
        try {
            return list.addAll(index, c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lock.lock();
        try {
            return list.removeAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        lock.lock();
        try {
            return list.retainAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            list.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        lock.lock();
        try {
            return list.set(index, element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        lock.lock();
        try {
            list.add(index, element);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E remove(int index) {
        lock.lock();
        try {
            return list.remove(index);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int indexOf(Object o) {
        lock.lock();
        try {
            return list.indexOf(o);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        lock.lock();
        try {
            return list.lastIndexOf(o);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            var newList = (ArrayList<E>) list.clone();
            return newList.listIterator();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            var newList = (ArrayList<E>) list.clone();
            return newList.listIterator(index);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        lock.lock();
        try {
            return list.subList(fromIndex, toIndex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this != o) {
            return false;
        }

        return list.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
