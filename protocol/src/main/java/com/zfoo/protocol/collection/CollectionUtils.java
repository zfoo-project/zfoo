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


import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class CollectionUtils {

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }


    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    public static <T> Iterator<T> iterator(Collection<T> collection) {
        return isEmpty(collection) ? Collections.emptyIterator() : collection.iterator();
    }

    public static <K, V> Iterator<Map.Entry<K, V>> iterator(Map<K, V> map) {
        return isEmpty(map) ? Collections.emptyIterator() : map.entrySet().iterator();
    }

    public static <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    public static <T> Set<T> emptySet() {
        return new HashSet<>();
    }

    public static <K, V> Map<K, V> emptyMap() {
        return new HashMap<>();
    }

    public static <T> List<T> newList(int size) {
        return size <= 0 ? new ArrayList<>() : new ArrayList<>(comfortableLength(size));
    }

    public static <T> Set<T> newSet(int size) {
        return size <= 0 ? new HashSet<>() : new HashSet<>(comfortableCapacity(size));
    }

    public static <K, V> Map<K, V> newMap(int size) {
        return size <= 0 ? new HashMap<>() : new HashMap<>(comfortableCapacity(size));
    }

    /**
     * 数组初始化长度的安全上限限制，防止反序列化异常导致内存突然升高
     */
    public static int comfortableLength(int length) {
        if (length >= IOUtils.BYTES_PER_MB) {
            throw new ArrayStoreException(StringUtils.format("新建数组的长度[{}]超过设置的安全范围[{}]", length, IOUtils.BYTES_PER_MB));
        }
        return length;
    }

    /**
     * 计算List和HashMap初始化合适的大小，为了安全必须给初始化的集合一个最大上限，防止反序列化一个不合法的包导致内存突然升高
     */
    public static int comfortableCapacity(int capacity) {
        return capacity < 16
                ? (capacity < 8 ? 16 : 32)
                : (capacity < 32 ? 64 : Math.min(capacity << 1, IOUtils.BYTES_PER_MB));
    }

    // ----------------------------------归并排序----------------------------------

    /**
     * Merges two sorted Collections, a and b, into a single, sorted List
     * such that the natural ordering of the elements is retained.
     * <p>
     * Uses the standard O(n) merge algorithm for combining two sorted lists.
     * </p>
     *
     * @param aList the first collection, must not be null
     * @param bList the second collection, must not be null
     * @return a new sorted List, containing the elements of Collection a and b
     */
    public static <T extends Comparable<? super T>> List<T> collate(List<? extends T> aList, List<? extends T> bList) {
        return collate(aList, bList, Comparator.naturalOrder(), true);
    }

    public static <T extends Comparable<? super T>> List<T> collate(List<? extends T> aList, List<? extends T> bList, boolean includeDuplicates) {
        return collate(aList, bList, Comparator.naturalOrder(), includeDuplicates);
    }

    public static <T> List<T> collate(List<T> aList, List<T> bList, Comparator<T> comparator) {
        return collate(aList, bList, comparator, true);
    }

    /**
     * Merges two sorted Collections, a and b, into a single, sorted List
     * such that the ordering of the elements according to Comparator c is retained.
     * <p>
     * Uses the standard O(n) merge algorithm for combining two sorted lists.
     * </p>
     *
     * @param <T>               the element type
     * @param aList             the first collection, must not be null
     * @param bList             the second collection, must not be null
     * @param comparator        the comparator to use for the merge.
     * @param includeDuplicates if {@code true} duplicate elements will be retained, otherwise
     *                          they will be removed in the output collection
     * @return a new sorted List, containing the elements of Collection a and b
     */
    public static <T> List<T> collate(List<? extends T> aList, List<? extends T> bList, Comparator<? super T> comparator, boolean includeDuplicates) {

        if (aList == null || bList == null) {
            throw new NullPointerException("The collections must not be null");
        }
        if (comparator == null) {
            throw new NullPointerException("The comparator must not be null");
        }

        var totalSize = aList.size() + bList.size();

        var mergedList = new ArrayList<T>(totalSize);

        var aIndex = 0;
        var bIndex = 0;

        T lastItem = null;
        while (aIndex < aList.size() && bIndex < bList.size()) {
            var a = aList.get(aIndex);
            var b = bList.get(bIndex);
            if (a == null) {
                aIndex++;
                continue;
            }
            if (b == null) {
                bIndex++;
                continue;
            }

            if (comparator.compare(a, b) >= 0) {
                bIndex++;
                if (!includeDuplicates && lastItem != null && lastItem.equals(b)) {
                    continue;
                }
                mergedList.add(b);
                lastItem = b;
            } else {
                aIndex++;
                if (!includeDuplicates && lastItem != null && lastItem.equals(a)) {
                    continue;
                }
                mergedList.add(a);
                lastItem = a;
            }

        }

        if (aIndex < aList.size()) {
            for (var i = aIndex; i < aList.size(); i++) {
                var value = aList.get(i);

                if (!includeDuplicates && lastItem != null && lastItem.equals(value)) {
                    continue;
                }

                mergedList.add(value);
                lastItem = value;
            }
        }

        if (bIndex < bList.size()) {
            for (var i = bIndex; i < bList.size(); i++) {
                var value = bList.get(i);

                if (!includeDuplicates && lastItem != null && lastItem.equals(value)) {
                    continue;
                }

                mergedList.add(value);
                lastItem = value;
            }
        }

        mergedList.trimToSize();
        return mergedList;
    }


    /**
     * list合并
     *
     * @param exclusive 元素是否是独占的，也就是说是否可以重复
     * @param pairs     需要被合并的pairs集合，第一个参数是步数，第二个参数是集合
     * @return 返回合并后的list
     */
    public static <T> List<T> listJoinList(boolean exclusive, Pair<Integer, List<T>>... pairs) {
        return listJoinList(exclusive, List.of(pairs));
    }

    public static <T> List<T> listJoinList(boolean exclusive, List<Pair<Integer, List<T>>> pairs) {
        var iteratorList = new ArrayList<List<T>>();
        var iteratorMap = new HashMap<List<T>, Iterator<T>>();
        var stepMap = new HashMap<List<T>, Integer>();
        for (var pair : pairs) {
            var step = pair.getKey();
            var list = pair.getValue();
            AssertionUtils.ge1(step);
            if (isNotEmpty(list)) {
                var iterator = list.iterator();
                iteratorList.add(list);
                iteratorMap.put(list, iterator);
                stepMap.put(list, step);
            }
        }

        var result = new ArrayList<T>();

        while (iteratorMap.values().stream().anyMatch(it -> it.hasNext())) {
            for (var list : iteratorList) {
                var iterator = iteratorMap.get(list);
                var step = stepMap.get(list);
                for (var i = 0; i < step && iterator.hasNext(); i++) {
                    var element = iterator.next();
                    if (exclusive && result.contains(element)) {
                        i--;
                        continue;
                    }
                    result.add(element);
                }
            }
        }

        return result;
    }

    /**
     * 获取集合的最后几个元素
     */
    public static <T> List<T> subListLast(List<T> list, int num) {
        if (isEmpty(list)) {
            return emptyList();
        }

        var startIndex = list.size() - num;
        if (startIndex <= 0) {
            return new ArrayList<>(list);
        }

        var result = new ArrayList<T>();


        for (T element : list) {
            startIndex--;
            if (startIndex < 0) {
                result.add(element);
            }
        }

        return result;
    }
}
