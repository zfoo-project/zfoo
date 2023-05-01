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

import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;

/**
 * @author godotg
 */
public class FastTreeMapIntLongTest {

    @Test
    public void test() {
        var treeMap = new TreeMap<Integer, Long>();
        for (var i = 0; i < 100; i = i + 2) {
            treeMap.put(i, (long) i);
        }

        var fastTreeMap = new FastTreeMapIntLong(treeMap);
        Assert.assertEquals(fastTreeMap.get(0), 0);
        Assert.assertEquals(fastTreeMap.get(8), 8);
        Assert.assertEquals(fastTreeMap.get(98), 98);
        Assert.assertFalse(fastTreeMap.contains(-1));
        Assert.assertFalse(fastTreeMap.contains(1));
        Assert.assertFalse(fastTreeMap.contains(100));
        Assert.assertTrue(fastTreeMap.contains(44));
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(-1), 0);
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(-100), 0);
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(0), 0);
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(1), 2);
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(45), 46);
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(97), 98);
        Assert.assertEquals(fastTreeMap.getValueByCeilingKey(100), 0);
        Assert.assertEquals(fastTreeMap.indexOfNearestCeilingKey(-1), 0);
        Assert.assertEquals(fastTreeMap.indexOfNearestCeilingKey(0), 0);
        Assert.assertEquals(fastTreeMap.indexOfNearestCeilingKey(1), 1);
    }

}
