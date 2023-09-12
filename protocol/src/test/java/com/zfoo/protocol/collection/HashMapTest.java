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

import java.util.*;

/**
 * @author godotg
 */
public class HashMapTest {

    @Test
    public void putTest() {
        var map = new HashMapIntInt(0);
        map.put(0, 0);
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        map.remove(0);
        map.remove(1);
        map.remove(2);
        map.remove(3);
        map.put(0, 0);
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);
        System.out.println(map.size());
        System.out.println(map);
    }

    private void assertKey(HashMapIntInt primitiveMap, HashMap<Integer, Integer> javaMap, int key) {
        var primitiveValue = primitiveMap.get(key);
        var javaValue = javaMap.get(key);
        Assert.assertEquals(primitiveValue, javaValue);
        Assert.assertEquals(primitiveMap.containsKey(key), javaMap.containsKey(key));
        Assert.assertEquals(primitiveMap.size(), javaMap.size());
    }

    @Test
    public void testPutGetRemoveContainsSmallMaps() {
        var random = new Random(322);
        for (var it = 0; it < 10000; it++) {
            var primitiveMap = new HashMapIntInt(0);
            var javaMap = new HashMap<Integer, Integer>();
            Assert.assertTrue(primitiveMap.isEmpty());
            for (var i = 0; i < 100; i++) {
                var key = random.nextInt(50);
                var value = random.nextInt(50);
                assertKey(primitiveMap, javaMap, key);
                if (random.nextBoolean()) {
                    var primitiveValue = primitiveMap.put(key, value);
                    var javaValue = javaMap.put(key, value);
                    Assert.assertEquals(primitiveValue, javaValue);
                } else {
                    var primitiveValue = primitiveMap.remove(key);
                    var javaValue = javaMap.remove(key);
                    Assert.assertEquals(primitiveValue, javaValue);
                }
                assertKey(primitiveMap, javaMap, key);
            }
        }
    }

    @Test
    public void testPutGetRemoveContainsBigMaps() {
        var random = new Random(Integer.MAX_VALUE);
        for (var it = 0; it < 100; it++) {
            var primitiveMap = new HashMapIntInt(0);
            var javaMap = new HashMap<Integer, Integer>();
            Assert.assertTrue(primitiveMap.isEmpty());
            for (var i = 0; i < 10000; i++) {
                var key = random.nextInt(Integer.MAX_VALUE);
                var value = random.nextInt(Integer.MAX_VALUE);
                assertKey(primitiveMap, javaMap, key);
                if (random.nextBoolean()) {
                    var primitiveValue = primitiveMap.put(key, value);
                    var javaValue = javaMap.put(key, value);
                    Assert.assertEquals(primitiveValue, javaValue);
                } else {
                    var primitiveValue = primitiveMap.remove(key);
                    var javaValue = javaMap.remove(key);
                    Assert.assertEquals(primitiveValue, javaValue);
                }
                assertKey(primitiveMap, javaMap, key);
            }
        }
    }

    @Test
    public void testPut() {
        var map = new HashMapIntInt();
        var array = new int[10000];
        Arrays.fill(array, -1);
        var random = new Random(32232);
        int size = 0;
        for (var i = 0; i < 1000000; i++) {
            Assert.assertEquals(map.size(), size);
            var key = random.nextInt(10000);
            Integer oldValue = map.put(key, i);
            if (array[key] == -1) {
                Assert.assertNull(oldValue);
                size++;
            } else {
                Assert.assertNotNull(oldValue);
                Assert.assertEquals(oldValue.intValue(), array[key]);
            }
            array[key] = i;
            Assert.assertEquals(map.size(), size);
        }
    }

    @Test
    public void testGetContainsKey() {
        var map = new HashMapIntInt();
        var random = new Random(322322);
        int size = 100000;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt();
            var oldValue = map.put(i, array[i]);
            Assert.assertNull(oldValue);
        }
        Assert.assertEquals(map.size(), size);
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(map.get(i).intValue(), array[i]);
            Assert.assertTrue(map.containsKey(i));
            map.get(~i);
            Assert.assertFalse(map.containsKey(~i));
        }
    }

    @Test
    public void testRemove() {
        var map = new HashMapIntInt();
        var random = new Random(3223223);
        int size = 100000;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt();
            var oldValue = map.put(i, array[i]);
            Assert.assertNull(oldValue);
        }
        Assert.assertEquals(map.size(), size);
        for (int i = 0; i < size; i++) {
            Assert.assertTrue(map.containsKey(i));
            Assert.assertEquals(map.remove(i).intValue(), array[i]);
            Assert.assertFalse(map.containsKey(i));
            map.remove(i);
            Assert.assertFalse(map.containsKey(i));
        }
    }

    @Test
    public void testClear() {
        var map = new HashMapIntInt();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(map.size(), 0);
        int size = 0;
        for (int i = 1; i <= 1000000; i++) {
            map.put(i, -i);
            size++;
            Assert.assertFalse(map.isEmpty());
            Assert.assertEquals(map.size(), size);
            if ((i & (i - 1)) == 0) {
                map.clear();
                Assert.assertTrue(map.isEmpty());
                Assert.assertEquals(map.size(), 0);
                size = 0;
            }
        }
    }

    @Test
    public void testKeysValuesArrays() {
        var random = new Random(32232232);
        var n = 100000;
        int[] keys = new int[n];
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = (1 + i) * 10000 + random.nextInt(9000);
            values[i] = random.nextInt();
        }
        var map = new HashMapIntInt();
        for (int i = 0; i < n; i++) {
            var oldValue = map.put(keys[i], values[i]);
            Assert.assertNull(oldValue);
        }
        Assert.assertEquals(map.size(), n);
        var mapKeys = new ArrayList<>(map.keySet());
        var mapValues = new ArrayList<>(map.values());
        Collections.sort(mapKeys);
        Collections.sort(mapValues);
        Arrays.sort(keys);
        Arrays.sort(values);
        Assert.assertArrayEquals(ArrayUtils.intToArray(mapKeys), keys);
        Assert.assertArrayEquals(ArrayUtils.intToArray(mapValues), values);
    }

    @Test
    public void testConstructors() {
        var srcMap = new HashMapIntInt();
        var javaMap = new HashMap<Integer, Integer>();
        int n = 10000;
        for (int i = 0; i < n; i++) {
            srcMap.put(~i, i);
            javaMap.put(~i, i);
        }
        var map1 = new HashMapIntInt();
        map1.putAll(srcMap);
        var map2 = new HashMapIntInt();
        map2.putAll(javaMap);
        Assert.assertEquals(map1.size(), n);
        Assert.assertEquals(map2.size(), n);
        for (int i = 0; i < n; i++) {
            Assert.assertEquals(map1.get(~i).intValue(), i);
            Assert.assertEquals(map2.get(~i).intValue(), i);
        }
        Assert.assertEquals(map1.size(), srcMap.size());
        Assert.assertEquals(map2.size(), srcMap.size());
    }

    @Test
    public void testIterator() {
        var random = new Random(322322322);
        int n = 1000;
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = random.nextInt();
        }
        var map = new HashMapIntInt();
        for (int i = 0; i < n; i++) {
            var oldValue = map.put(i, array[i]);
            Assert.assertNull(oldValue);
        }
        boolean[] visited = new boolean[n];
        var it = map.entrySet().iterator();
        for (int i = 0; i < n; i++) {
            Assert.assertTrue(it.hasNext());
            var next = it.next();
            int key = next.getKey();
            int value = next.getValue();
            Assert.assertEquals(value, array[key]);
            Assert.assertFalse(visited[key]);
            visited[key] = true;
            for (int retries = 0; retries < 2; retries++) {
                Assert.assertEquals(next.getKey().intValue(), key);
                Assert.assertEquals(next.getValue().intValue(), value);
            }
        }
        for (int retries = 0; retries < 3; retries++) {
            Assert.assertFalse(it.hasNext());
            try {
                it.next().getKey();
            } catch (NoSuchElementException e) {
                // as expected
            }
            try {
                it.next().getValue();
            } catch (NoSuchElementException e) {
                // as expected
            }
        }

        it = map.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            it.remove();
            Assert.assertFalse(map.containsKey(entry.getKey()));
        }
        Assert.assertTrue(map.isEmpty());
    }


    @Test
    public void testCompressingAfterRemoving() {
        int n = 1000000;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }
        Random rnd = new Random(3223223223L);
        for (int i = 0; i < n; i++) {
            int j = i + rnd.nextInt(n - i);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        var map = new HashMapIntInt();
        for (int i = 0; i < n; i++) {
            map.put(a[i], i);
        }
        for (int i = 0; i < n - 1000; i++) {
            var oldValue = map.remove(a[i]);
            Assert.assertNotNull(oldValue);
        }
        Assert.assertEquals(map.size(), 1000);

        Assert.assertEquals(map.size(), 1000);
        // Length of the arrays in the map must be O(size). If it's not, there will be a timeout
        int dummy1 = 0, dummy2 = 0;
        for (int i = 0; i < 1000; i++) {
            var it = map.entrySet().iterator();
            while(it.hasNext()) {
                var next = it.next();
                dummy1 ^= next.getKey();
                dummy2 ^= next.getValue();
            }
        }
        Assert.assertEquals(dummy1, 0);
        Assert.assertEquals(dummy2, 0);
    }

}
