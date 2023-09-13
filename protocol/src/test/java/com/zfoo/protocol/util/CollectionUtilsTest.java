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

package com.zfoo.protocol.util;

import com.zfoo.protocol.collection.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author godotg
 */
public class CollectionUtilsTest {

    @Test
    public void mapRemoveTest() {
        Map<String, String> map = new ConcurrentHashMap<>();
        map.put("a", "a");
        Assert.assertEquals(map.remove("a"), "a");
    }

    @Test
    public void collateTest() {
        var a = new ArrayList<>(List.of(1, 2, 3, 9));
        var b = new ArrayList<>(List.of(2, 4, 6, 7, 8));
        var c = CollectionUtils.collate(a, b);
        var d = List.of(1, 2, 2, 3, 4, 6, 7, 8, 9);
        Assert.assertArrayEquals(c.toArray(), d.toArray());

        a = new ArrayList<>(List.of(1, 2, 3));
        b = new ArrayList<>();
        c = CollectionUtils.collate(a, b);
        Assert.assertArrayEquals(c.toArray(), a.toArray());
    }

    @Test
    public void subListLastTest() {
        var list = List.of(1, 2, 3, 4, 5);
        Assert.assertArrayEquals(CollectionUtils.subListLast(list, 1).toArray(), List.of(5).toArray());
        Assert.assertArrayEquals(CollectionUtils.subListLast(list, 4).toArray(), List.of(2, 3, 4, 5).toArray());
        Assert.assertArrayEquals(CollectionUtils.subListLast(list, 5).toArray(), list.toArray());
        Assert.assertArrayEquals(CollectionUtils.subListLast(list, 6).toArray(), list.toArray());
        Assert.assertArrayEquals(CollectionUtils.subListLast(list, 100).toArray(), list.toArray());
    }

}
