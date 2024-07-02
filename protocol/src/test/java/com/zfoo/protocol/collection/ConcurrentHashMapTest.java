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

import com.zfoo.protocol.collection.concurrent.ConcurrentHashMapLongObject;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMap;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import io.netty.util.collection.LongObjectHashMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author godotg
 */
public class ConcurrentHashMapTest {

    @Test
    public void equalsMapTest() {
        var map = Map.of(1L, "a", 2L, "b", 3L, "c", 4L, "d", 5L, "e", Long.MAX_VALUE, "max", Long.MIN_VALUE, "min");

        var concurrentHashMapLongObject = new ConcurrentHashMapLongObject<String>();
        concurrentHashMapLongObject.putAll(map);
        Assert.assertEquals(map, concurrentHashMapLongObject);

        var copyOnWriteHashMap = new CopyOnWriteHashMap<Long, String>();
        copyOnWriteHashMap.putAll(map);
        Assert.assertEquals(map, copyOnWriteHashMap);

        var copyOnWriteHashMapLongObject = new CopyOnWriteHashMapLongObject<String>();
        copyOnWriteHashMapLongObject.putAll(map);
        Assert.assertEquals(map, copyOnWriteHashMapLongObject);
    }

    @Test
    public void primitiveMapTest() {
        var map = new LongObjectHashMap<Integer>();

        var num = 10;

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < num; j++) {
                map.put(j, j);
            }
            for (var entry : map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
            }
            for (int j = 0; j < num; j++) {
                var value = (int) map.get((long) j);
                Assert.assertEquals(value, j);
            }
            for (int j = 0; j < num; j++) {
                map.remove((long) j);
            }
        }

        Assert.assertTrue(map.isEmpty());
    }

}
