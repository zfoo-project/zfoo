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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author godotg
 */
public class ArrayListTest {

    @Test
    public void testAdd() {
        var list = new ArrayListInt(1);
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(list.size(), 0);
        int[] array = new int[100];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        for (int i = 0; i < array.length; i++) {
            Assert.assertTrue(list.add(array[i]));
            Assert.assertArrayEquals(list.toArray(), ArrayUtils.toList(Arrays.copyOfRange(array, 0, i + 1)).toArray());
        }

        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals(list.size(), array.length);
        Assert.assertTrue(list.contains(55));
        Assert.assertFalse(list.contains(9999));
    }

    @Test
    public void testAddAll() {
        var array1 = new int[]{0, 1, 2};
        var array2 = new int[]{3, 4, 5};
        var array3 = new int[]{6, 7, 8};
        var list1 = new ArrayListInt(array1);
        var list2 = new ArrayListInt(array2);
        var list3 = new ArrayListInt(array3);
        list2.addAll(0, list1);
        Assert.assertArrayEquals(list2.toArrayPrimitive(), new int[]{0, 1, 2, 3, 4, 5});
        try {
            list2.addAll(7, list3);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        list2.addAll(6, list3);
        Assert.assertArrayEquals(list2.toArrayPrimitive(), new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        list2.addAll(6, list3);
        Assert.assertArrayEquals(list2.toArrayPrimitive(), new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 6, 7, 8});
    }

    @Test
    public void testRemove() {
        var list = new ArrayListInt(1);
        for (var i = 1; i <= 5; i++) {
            list.add(i);
        }
        Assert.assertArrayEquals(list.toArray(), ArrayUtils.toList(new int[]{1, 2, 3, 4, 5}).toArray());
        Assert.assertTrue(list.remove(Integer.valueOf(2)));
        Assert.assertArrayEquals(list.toArray(), ArrayUtils.toList(new int[]{1, 3, 4, 5}).toArray());
        Assert.assertFalse(list.remove(Integer.valueOf(6)));
        Assert.assertArrayEquals(list.toArray(), ArrayUtils.toList(new int[]{1, 3, 4, 5}).toArray());
        Assert.assertEquals(list.remove(2).intValue(), 4);
        Assert.assertArrayEquals(list.toArray(), ArrayUtils.toList(new int[]{1, 3, 5}).toArray());
        Assert.assertEquals(list.remove(2).intValue(), 5);
        Assert.assertArrayEquals(list.toArray(), ArrayUtils.toList(new int[]{1, 3}).toArray());
        try {
            list.remove(2);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        try {
            list.remove(-1);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{1, 3});
        Assert.assertEquals(list.remove(0).intValue(), 1);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{3});
        Assert.assertTrue(list.remove(Integer.valueOf(3)));
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[0]);
    }

    @Test
    public void testRemoveAll() {
        var array1 = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 6, 7, 8};
        var array2 = new int[]{3, 4, 5};
        var array3 = new int[]{6, 7, 8};
        var list1 = new ArrayListInt(array1);
        var list2 = new ArrayListInt(array2);
        var list3 = new ArrayListInt(array3);
        list1.removeAll(list3);
        Assert.assertArrayEquals(list1.toArrayPrimitive(), new int[]{0, 1, 2, 3, 4, 5});

        list1.addAll(list3);
        list1.retainAll(list2);
        Assert.assertArrayEquals(list1.toArrayPrimitive(), new int[]{3, 4, 5});
        Assert.assertEquals(list1.size(), 3);
    }

    @Test
    public void testInsert() {
        var list = new ArrayListInt(1);
        for (var i = 1; i <= 3; i++) {
            list.add(i);
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{1, 2, 3});
        list.add(0, 4);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 1, 2, 3});
        list.add(2, 5);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 1, 5, 2, 3});
        list.add(5, 6);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 1, 5, 2, 3, 6});
        try {
            list.add(8, 7);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        try {
            list.add(-1, 7);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 1, 5, 2, 3, 6});
    }


    @Test
    public void testSet() {
        var list = new ArrayListInt(1);
        for (var i = 1; i <= 3; i++) {
            list.add(i);
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{1, 2, 3});
        list.set(0, 4);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 2, 3});
        list.set(2, 5);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 2, 5});
        try {
            list.set(3, 6);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        try {
            list.set(-1, 6);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{4, 2, 5});
    }

    @Test
    public void testAddRemoveLast() {
        var list = new ArrayListInt(1);
        for (var i = 1; i <= 3; i++) {
            list.add(i);
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{1, 2, 3});
        list.add(4);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{1, 2, 3, 4});
        Assert.assertEquals(list.getPrimitive(list.size() - 1), 4);
        Assert.assertEquals(list.remove(list.size() - 1).intValue(), 4);
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[]{1, 2, 3});
        for (int i = 3; i >= 1; i--) {
            Assert.assertEquals(list.remove(list.size() - 1).intValue(), i);
        }
        try {
            list.getPrimitive(list.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        try {
            list.remove(list.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            // as expected
        }
        Assert.assertArrayEquals(list.toArrayPrimitive(), new int[0]);
    }

    @Test
    public void testIndexOf() {
        var srcArray = new int[]{1, 2, 3, 2, 1, 2, 3, 2, 1};
        var list = new ArrayListInt(0);
        list.addAll(ArrayUtils.toList(srcArray));
        Assert.assertArrayEquals(list.toArrayPrimitive(), srcArray);
        Assert.assertEquals(list.indexOf(1), 0);
        Assert.assertEquals(list.indexOf(2), 1);
        Assert.assertEquals(list.indexOf(3), 2);
        Assert.assertEquals(list.indexOf(4), -1);
        Assert.assertEquals(list.lastIndexOf(1), 8);
        Assert.assertEquals(list.lastIndexOf(2), 7);
        Assert.assertEquals(list.lastIndexOf(3), 6);
        Assert.assertEquals(list.lastIndexOf(4), -1);
    }

    @Test
    public void testIterator() {
        var list = new ArrayListInt(0);
        for (var i = 0; i < 42; i++) {
            Assert.assertTrue(list.add(i));
        }
        Assert.assertEquals(list.size(), 42);
        var it = list.iterator();
        for (var i = 0; i < 42; i++) {
            Assert.assertTrue(it.hasNext());
            Assert.assertEquals(it.next().intValue(), i);
        }
        Assert.assertFalse(it.hasNext());
        try {
            it.next();
        } catch (NoSuchElementException e) {
            // as expected
        }

        var index = 0;
        for (var e : list) {
            Assert.assertEquals(e, list.get(index++));
        }

        index = 0;
        var iterator = list.iterator();
        while (iterator.hasNext()) {
            Assert.assertEquals(iterator.next(), list.get(index++));
        }

        iterator = list.iterator();
        while (iterator.hasNext()) {
            Assert.assertEquals(iterator.next(), list.get(0));
            iterator.remove();
        }

        index = 0;
        var listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            Assert.assertEquals(listIterator.next(), list.get(index++));
        }
    }
}
