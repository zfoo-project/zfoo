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

import java.util.HashSet;

/**
 * @author godotg
 */
public class HashSetTest {

    @Test
    public void byteTest() {
        var set = new HashSet<Byte>();
        var mySet = new HashSetByte();
        for (var i = 0; i < 100; i++) {
            set.add((byte) i);
            mySet.add((byte) i);
        }
        set.add(Byte.MAX_VALUE);
        set.add(Byte.MIN_VALUE);
        mySet.add(Byte.MAX_VALUE);
        mySet.add(Byte.MIN_VALUE);
        Assert.assertEquals(set, mySet);
        Assert.assertEquals(set.size(), mySet.size());
        Assert.assertFalse(mySet.isEmpty());
        for (var ele : set) {
            Assert.assertTrue(mySet.contains(ele));
        }
        for (var ele : mySet) {
            Assert.assertTrue(set.contains(ele));
        }
        for (var ele : set) {
            mySet.remove(ele);
        }
        Assert.assertTrue(mySet.isEmpty());
    }

    @Test
    public void shortTest() {
        var set = new HashSet<Short>();
        var mySet = new HashSetShort();
        for (var i = 0; i < 1000; i++) {
            set.add((short) i);
            mySet.add((short) i);
        }
        set.add(Short.MAX_VALUE);
        set.add(Short.MIN_VALUE);
        mySet.add(Short.MAX_VALUE);
        mySet.add(Short.MIN_VALUE);
        Assert.assertEquals(set, mySet);
        Assert.assertEquals(set.size(), mySet.size());
        Assert.assertFalse(mySet.isEmpty());
        for (var ele : set) {
            Assert.assertTrue(mySet.contains(ele));
        }
        for (var ele : mySet) {
            Assert.assertTrue(set.contains(ele));
        }
        for (var ele : set) {
            mySet.remove(ele);
        }
        Assert.assertTrue(mySet.isEmpty());
    }

    @Test
    public void intTest() {
        var set = new HashSet<Integer>();
        var mySet = new HashSetInt();
        for (var i = 0; i < 1000; i++) {
            set.add(i);
            mySet.add(i);
        }
        set.add(Integer.MAX_VALUE);
        set.add(Integer.MIN_VALUE);
        mySet.add(Integer.MAX_VALUE);
        mySet.add(Integer.MIN_VALUE);
        Assert.assertEquals(set, mySet);
        Assert.assertEquals(set.size(), mySet.size());
        Assert.assertFalse(mySet.isEmpty());
        for (var ele : set) {
            Assert.assertTrue(mySet.contains(ele));
        }
        for (var ele : mySet) {
            Assert.assertTrue(set.contains(ele));
        }
        for (var ele : set) {
            mySet.remove(ele);
        }
        Assert.assertTrue(mySet.isEmpty());
    }

    @Test
    public void longTest() {
        var set = new HashSet<Long>();
        var mySet = new HashSetLong();
        for (var i = 0; i < 1000; i++) {
            set.add((long) i);
            mySet.add(i);
        }
        set.add(Long.MAX_VALUE);
        set.add(Long.MIN_VALUE);
        mySet.add(Long.MAX_VALUE);
        mySet.add(Long.MIN_VALUE);
        Assert.assertEquals(set, mySet);
        Assert.assertEquals(set.size(), mySet.size());
        Assert.assertFalse(mySet.isEmpty());
        for (var ele : set) {
            Assert.assertTrue(mySet.contains(ele));
        }
        for (var ele : mySet) {
            Assert.assertTrue(set.contains(ele));
        }
        for (var ele : set) {
            mySet.remove(ele);
        }
        Assert.assertTrue(mySet.isEmpty());
    }
}
