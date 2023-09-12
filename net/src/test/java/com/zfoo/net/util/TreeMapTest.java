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

import com.zfoo.net.packet.common.PairIntLong;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author godotg
 */
public class TreeMapTest {

    private void testPermutation(int[] array) {
        int n = array.length;
        TreeMapIntLong map = new TreeMapIntLong(0);
        for (int it = 0; it < 3; it++) {
            Assert.assertEquals(map.size(), 0);
            Assert.assertTrue(map.isEmpty());
            for (int i = 0; i < n; i++) {
                map.put(array[i], 2 * array[i]);
                Assert.assertTrue(map.returnedNull());
                Assert.assertEquals(map.size(), i + 1);
                Assert.assertFalse(map.isEmpty());
            }
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < n; i++) {
                Assert.assertEquals(map.put(array[i], 3 * array[i]), 2 * array[i]);
                Assert.assertFalse(map.returnedNull());
                Assert.assertEquals(map.size(), n);
                Assert.assertFalse(map.isEmpty());
            }
            for (int i = -1; i <= n; i++) {
                if (0 <= i && i < n) {
                    Assert.assertTrue(map.containsKey(array[i]));
                    Assert.assertEquals(map.get(array[i]), 3 * array[i]);
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertFalse(map.containsKey(i));
                    map.get(i);
                    Assert.assertTrue(map.returnedNull());
                }
            }
            Assert.assertEquals(map.getFirstKey(), 0);
            Assert.assertFalse(map.returnedNull());
            Assert.assertEquals(map.getFirstEntry(), PairIntLong.valueOf(0, 0L));
            Assert.assertFalse(map.returnedNull());
            Assert.assertEquals(map.getLastKey(), n - 1);
            Assert.assertFalse(map.returnedNull());
            Assert.assertEquals(map.getLastEntry(), PairIntLong.valueOf(n - 1,  (3 * (n - 1))));
            Assert.assertFalse(map.returnedNull());
            for (int i = -2; i <= n + 1; i++) {
                int lowerKey = map.lowerKey(i);
                if (i > 0) {
                    Assert.assertEquals(lowerKey, Math.min(i - 1, n - 1));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertTrue(map.returnedNull());
                }
                int higherKey = map.higherKey(i);
                if (i < n - 1) {
                    Assert.assertEquals(higherKey, Math.max(i + 1, 0));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertTrue(map.returnedNull());
                }
                int floorKey = map.floorKey(i);
                if (i >= 0) {
                    Assert.assertEquals(floorKey, Math.min(i, n - 1));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertTrue(map.returnedNull());
                }
                int ceilingKey = map.ceilingKey(i);
                if (i < n) {
                    Assert.assertEquals(ceilingKey, Math.max(i, 0));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertTrue(map.returnedNull());
                }
                PairIntLong lowerEntry = map.lowerEntry(i);
                if (i > 0) {
                    Assert.assertEquals(lowerEntry, PairIntLong.valueOf(lowerKey,  (3 * lowerKey)));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertNull(lowerEntry);
                    Assert.assertTrue(map.returnedNull());
                }
                PairIntLong higherEntry = map.higherEntry(i);
                if (i < n - 1) {
                    Assert.assertEquals(higherEntry, PairIntLong.valueOf(higherKey,  (3 * higherKey)));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertNull(higherEntry);
                    Assert.assertTrue(map.returnedNull());
                }
                PairIntLong floorEntry = map.floorEntry(i);
                if (i >= 0) {
                    Assert.assertEquals(floorEntry, PairIntLong.valueOf(floorKey,  (3 * floorKey)));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertNull(floorEntry);
                    Assert.assertTrue(map.returnedNull());
                }
                PairIntLong ceilingEntry = map.ceilingEntry(i);
                if (i < n) {
                    Assert.assertEquals(ceilingEntry, PairIntLong.valueOf(ceilingKey,  (3 * ceilingKey)));
                    Assert.assertFalse(map.returnedNull());
                } else {
                    Assert.assertNull(ceilingEntry);
                    Assert.assertTrue(map.returnedNull());
                }
            }
            int removed = 0;
            for (int i = -1; i <= n; i++) {
                if (0 <= i && i < n) {
                    Assert.assertEquals(map.remove(array[i]), 3 * array[i]);
                    Assert.assertFalse(map.returnedNull());
                    removed++;
                } else {
                    map.remove(i);
                    Assert.assertTrue(map.returnedNull());
                }
                Assert.assertEquals(map.size(), n - removed);
            }
            map.clear();
        }
    }

    private void genPermutations(int[] permutation, int left, int length) {
        if (left >= length - 1) {
            testPermutation(permutation);
            return;
        }
        for (int i = left; i < length; i++) {
            swap(permutation, i, left);
            genPermutations(permutation, left + 1, length);
            swap(permutation, i, left);
        }
    }

    private void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private void shuffle(int[] a, Random rnd) {
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int j = i + rnd.nextInt(n - i);
            swap(a, i, j);
        }
    }

    @Test
    public void testAllSmallPermutations() {
        for (int length = 1; length <= 8; length++) {
            int[] a = new int[length];
            for (int i = 0; i < length; i++) {
                a[i] = i;
            }
            genPermutations(a, 0, length);
        }
    }

    @Test
    public void testManyPermutationsLength100() {
        Random rnd = new Random(-228);
        for (int it = 0; it < 7000; it++) {
            int length = 9 + rnd.nextInt(92);
            int[] a = new int[length];
            for (int i = 0; i < length; i++) {
                a[i] = i;
            }
            shuffle(a, rnd);
            testPermutation(a);
        }
    }

    @Test
    public void testFewPermutationsLength10000() {
        Random rnd = new Random(-322);
        for (int it = 0; it < 35; it++) {
            int length = 9 + rnd.nextInt(9992);
            int[] a = new int[length];
            for (int i = 0; i < length; i++) {
                a[i] = i;
            }
            shuffle(a, rnd);
            testPermutation(a);
        }
    }

    @Test
    public void testOperationsOnEmptyMap() {
        TreeMapIntLong map = new TreeMapIntLong(0);
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(map.size(), 0);
        for (int key = -200; key <= 200; key++) {
            Assert.assertFalse(map.containsKey(key));
            map.get(key);
            Assert.assertTrue(map.returnedNull());
            map.remove(key);
            Assert.assertTrue(map.returnedNull());
            map.getFirstKey();
            Assert.assertTrue(map.returnedNull());
            map.getLastKey();
            Assert.assertTrue(map.returnedNull());
            map.higherKey(key);
            Assert.assertTrue(map.returnedNull());
            map.lowerKey(key);
            Assert.assertTrue(map.returnedNull());
            map.ceilingKey(key);
            Assert.assertTrue(map.returnedNull());
            map.floorKey(key);
            Assert.assertTrue(map.returnedNull());
            Assert.assertEquals(map.higherEntry(key), null);
            Assert.assertTrue(map.returnedNull());
            Assert.assertEquals(map.lowerEntry(key), null);
            Assert.assertTrue(map.returnedNull());
            Assert.assertEquals(map.ceilingEntry(key), null);
            Assert.assertTrue(map.returnedNull());
            Assert.assertEquals(map.floorEntry(key), null);
            Assert.assertTrue(map.returnedNull());
            Assert.assertEquals(map.getFirstEntry(), null);
            Assert.assertTrue(map.returnedNull());
            Assert.assertEquals(map.getLastEntry(), null);
            Assert.assertTrue(map.returnedNull());
        }
        Assert.assertArrayEquals(map.keys(), new int[0]);
        Assert.assertArrayEquals(map.values(), new long[0]);
    }

    private void assertSortedMapMethods(TreeMapIntLong map, TreeMap<Integer, Long> javaMap, int key) {
        {
            Map.Entry<Integer, Long> javaEntry = javaMap.floorEntry(key);
            int ezResultKey = map.floorKey(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
            } else {
                Assert.assertEquals(ezResultKey, (int) javaEntry.getKey());
            }
            PairIntLong ezResultEntry = map.floorEntry(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
                Assert.assertNull(ezResultEntry);
            } else {
                Assert.assertEquals(ezResultEntry, PairIntLong.valueOf(javaEntry.getKey(),  javaEntry.getValue()));
            }
        }
        {
            Map.Entry<Integer, Long> javaEntry = javaMap.ceilingEntry(key);
            int ezResultKey = map.ceilingKey(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
            } else {
                Assert.assertEquals(ezResultKey, (int) javaEntry.getKey());
            }
            PairIntLong ezResultEntry = map.ceilingEntry(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
                Assert.assertNull(ezResultEntry);
            } else {
                Assert.assertEquals(ezResultEntry, PairIntLong.valueOf(javaEntry.getKey(),  javaEntry.getValue()));
            }
        }
        {
            Map.Entry<Integer, Long> javaEntry = javaMap.lowerEntry(key);
            int ezResultKey = map.lowerKey(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
            } else {
                Assert.assertEquals(ezResultKey, (int) javaEntry.getKey());
            }
            PairIntLong ezResultEntry = map.lowerEntry(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
                Assert.assertNull(ezResultEntry);
            } else {
                Assert.assertEquals(ezResultEntry, PairIntLong.valueOf(javaEntry.getKey(),  javaEntry.getValue()));
            }
        }
        {
            Map.Entry<Integer, Long> javaEntry = javaMap.higherEntry(key);
            int ezResultKey = map.higherKey(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
            } else {
                Assert.assertEquals(ezResultKey, (int) javaEntry.getKey());
            }
            PairIntLong ezResultEntry = map.higherEntry(key);
            if (javaEntry == null) {
                Assert.assertTrue(map.returnedNull());
                Assert.assertNull(ezResultEntry);
            } else {
                Assert.assertEquals(ezResultEntry, PairIntLong.valueOf(javaEntry.getKey(),  javaEntry.getValue()));
            }
        }
    }

    @Test
    public void testSignificantMethodsOnRandomData() {
        TreeMapIntLong map = new TreeMapIntLong();
        TreeMap<Integer, Long> javaMap = new TreeMap<Integer, Long>();
        Random rnd = new Random(~42);
        for (int i = 0; i < 1000000; i++) {
            Assert.assertEquals(map.size(), javaMap.size());
            double prob = rnd.nextDouble();
            if (prob < 0.301) {
                int key = rnd.nextInt(50);
                long newValue = rnd.nextInt();
                Long oldValue = javaMap.put(key, newValue);
                if (oldValue == null) {
                    map.put(key, newValue);
                    Assert.assertTrue(map.returnedNull());
                } else {
                    long actualOldValue = map.put(key, newValue);
                    Assert.assertFalse(map.returnedNull());
                    Assert.assertEquals(actualOldValue,  oldValue.longValue());
                }
            } else if (prob < 0.6) {
                int key = rnd.nextInt(50);
                Long oldValue = javaMap.remove(key);
                if (oldValue == null) {
                    map.remove(key);
                    Assert.assertTrue(map.returnedNull());
                } else {
                    long actualOldValue = map.remove(key);
                    Assert.assertFalse(map.returnedNull());
                    Assert.assertEquals(actualOldValue,  oldValue.longValue());
                }
            } else {
                int key = rnd.nextInt(20);
                assertSortedMapMethods(map, javaMap, key);
            }
        }
    }


    @Test
    public void testKeysValuesArrays() {
        int[] keys = new int[100];
        long[] values = new long[100];
        for (int i = 0; i < 100; i++) {
            keys[i] = i * 322 + 228;
            values[i] = i * 228 + 322;
        }
        for (int len = 0; len <= 100; len++) {
            TreeMapIntLong map = new TreeMapIntLong();
            for (int i = 0; i < len; i++) {
                map.put(keys[i], values[i]);
                Assert.assertTrue(map.returnedNull());
            }
            Assert.assertArrayEquals(map.keys(), Arrays.copyOfRange(keys, 0, len));
            Assert.assertArrayEquals(map.values(), Arrays.copyOfRange(values, 0, len));
        }
    }

    @Test
    public void testEqualsHashcode() {
        for (int mask1 = 0; mask1 < 32; mask1++) {
            for (int mask2 = 0; mask2 < 32; mask2++) {
                TreeMapIntLong map1 = new TreeMapIntLong(1);
                TreeMapIntLong map2 = new TreeMapIntLong(1);
                for (int i = 0; i < 5; i++) {
                    if ((mask1 & (1 << i)) != 0) {
                        map1.put(1000 * i, i);
                    }
                    if ((mask2 & (1 << i)) != 0) {
                        map2.put(1000 * i, i);
                    }
                }
                int hashCode1 = map1.hashCode();
                int hashCode2 = map2.hashCode();
                if (mask1 == mask2) {
                    Assert.assertTrue(map1.equals(map2));
                    Assert.assertTrue(map2.equals(map1));
                    Assert.assertTrue(hashCode1 == hashCode2);
                } else {
                    Assert.assertFalse(map1.equals(map2));
                    Assert.assertFalse(map2.equals(map1));
                }
            }
        }
    }

    @Test
    public void testCelling() {
        var start = 10000;
        var end = 20000;
        TreeMapIntLong treeMap = new TreeMapIntLong(200);
        for (int i = start; i < end; i++) {
            if (i % 2 == 0) {
                treeMap.put(i, i);
            }
        }
        for (int i = start; i < end; i++) {
            if (i % 2 != 0 && i != (end - 1)) {
                Assert.assertEquals(treeMap.ceilingEntry(i).getValue(), i + 1);
            }
            if (i % 2 != 0) {
                Assert.assertEquals(treeMap.floorEntry(i).getValue(), i - 1);
            }
        }
    }

}
