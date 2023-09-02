/*
 * Copyright (C) 2020 The zfoo Authors
 *
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

import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class ConsistentHashTest {

    //待添加入Hash环的服务器列表
    private static final List<Pair<String, String>> servers = List.of(new Pair<>("192.168.0.0:111", "192.168.0.0:111")
            , new Pair<>("192.168.0.1:111", "192.168.0.1:111"), new Pair<>("192.168.0.2:111", "192.168.0.2:111"));

    private static final List<Pair<String, String>> nums = List.of(new Pair<>("1", "1"), new Pair<>("2", "2"), new Pair<>("3", "3"));

    private static final List<Pair<String, String>> chars = List.of(new Pair<>("a", "a"), new Pair<>("b", "b"), new Pair<>("c", "c"));


    @Test
    public void consistentHashTest() {
        test(servers);
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
        test(nums);
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
        test(chars);
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
    }

    public void test(List<Pair<String, String>> list) {
        var realNodeMap = new HashMap<String, Integer>();
        var hitNodeMap = new HashMap<Integer, String>();

        list.forEach(it -> realNodeMap.put(it.getKey(), 0));

        var consistentHash = new ConsistentHash<>(list, 300);
        for (int i = 0; i < 100000; i++) {
            var key = String.valueOf(i);
            var realNode = consistentHash.getRealNode(key).getKey();

            int nums = realNodeMap.get(realNode);
            realNodeMap.put(realNode, ++nums);
            hitNodeMap.put(i, realNode);
        }
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 100000; j++) {
                var key = String.valueOf(j);
                var realNode = consistentHash.getRealNode(key).getKey();
                Assert.assertEquals(realNode, hitNodeMap.get(j));
            }
        }

        System.out.println(JsonUtils.object2String(realNodeMap));
    }


    @Test
    public void testTreeMap() {
        var treeMap = new TreeMap<Integer, String>();
        treeMap.put(1, "a");
        treeMap.put(100, "b");
        treeMap.put(200, "c");

        System.out.println(treeMap.ceilingEntry(8));
        System.out.println(treeMap.ceilingEntry(450));

    }

}
