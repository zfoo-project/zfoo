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

package com.zfoo.orm.accessor;

import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.bag.BagItem;
import com.zfoo.orm.entity.bag.Item;
import com.zfoo.orm.entity.bag.MapEntity;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Ignore
public class MapTest {

    @Test
    public void insertTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        insert(1);
    }

    @Test
    public void batchInsertTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        for (int i = 0; i < 100; i++) {
            insert(i);
        }
    }

    public void insert(long id) {
        OrmContext.getAccessor().delete(id, MapEntity.class);

        var entity = new MapEntity();
        entity.setId(id);

        entity.setList(List.of(1, 2, 3));

        CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        copyOnWriteArrayList.add(1);
        copyOnWriteArrayList.add(2);
        entity.setCopyOnWriteArrayList(copyOnWriteArrayList);

        ConcurrentHashMap<Long, Integer> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put(1L, 1);
        concurrentHashMap.put(2L, 2);
        entity.setConcurrentHashMap(concurrentHashMap);

        CopyOnWriteHashMap<Long, CopyOnWriteHashMap<Integer, Integer>> copyOnWriteHashMap = new CopyOnWriteHashMap<>();
        CopyOnWriteHashMap<Integer, Integer> copMap = new CopyOnWriteHashMap<>();
        copMap.put(1, 1);
        copMap.put(2, 2);
        copMap.put(3, 3);
        copyOnWriteHashMap.putIfAbsent(1L, copMap);
        copyOnWriteHashMap.putIfAbsent(2L, copMap);
        entity.setCopyOnWriteHashMap(copyOnWriteHashMap);
        var bagMap = new HashMap<String, BagItem>();
        entity.setBagMap(bagMap);

        var itemMap = new HashMap<String, Item>();
        itemMap.put("1", new Item(1, "item1"));
        itemMap.put("2", new Item(2, "item2"));
        itemMap.put("3", new Item(3, "item3"));

        var bagItem1 = new BagItem(1, "desc1", itemMap);
        var bagItem2 = new BagItem(2, "desc2", itemMap);
        var bagItem3 = new BagItem(3, "desc3", itemMap);

        bagMap.put("bag1", bagItem1);
        bagMap.put("bag2", bagItem2);
        bagMap.put("bag3", bagItem3);
        bagMap.put("bag4", null);

        var map = new HashMap<String, Map<String, String>>();
        map.put("a", Map.of("b", "b"));
        entity.setBaseMap(map);

        var longStringHashMap = new HashMap<Long, String>();
        longStringHashMap.put(100L, "hello map1");
        longStringHashMap.put(101L, "hello map2");
        longStringHashMap.put(103L, "hello map3");
        longStringHashMap.put(104L, null);
        entity.setLongStringMap(longStringHashMap);

        var intStringHashMap = new HashMap<Integer, String>();
        intStringHashMap.put(100, "hello map1");
        intStringHashMap.put(102, "hello map2");
        intStringHashMap.put(103, "hello map3");
        intStringHashMap.put(104, null);
        entity.setIntStringMap(intStringHashMap);

        var intBaseMap = new HashMap<Integer, Map<Integer, String>>();
        intBaseMap.put(1, Map.of(1, "1"));
        intBaseMap.put(2, Map.of(2, "2"));
        intBaseMap.put(3, Map.of(3, "3"));
        intBaseMap.put(4, null);
        entity.setIntBaseMap(intBaseMap);

        var charBagMap = new HashMap<Character, BagItem>();
        entity.setCharBagMap(charBagMap);
        charBagMap.put('a', bagItem1);
        charBagMap.put('b', bagItem2);
        charBagMap.put('d', null);

        var boolBagMap = new HashMap<Boolean, BagItem>();
        entity.setBoolBagMap(boolBagMap);
        boolBagMap.put(true, bagItem1);
        boolBagMap.put(false, null);

        var byteBagMap = new HashMap<Byte, BagItem>();
        entity.setByteBagMap(byteBagMap);
        byteBagMap.put((byte) 1, bagItem1);
        byteBagMap.put((byte) 2, bagItem2);
        byteBagMap.put((byte) 3, null);

        var shortBagMap = new HashMap<Short, BagItem>();
        entity.setShortBagMap(shortBagMap);
        shortBagMap.put((short) 1, bagItem1);
        shortBagMap.put((short) 2, bagItem2);
        shortBagMap.put((short) 3, null);

        var intBagMap = new HashMap<Integer, BagItem>();
        entity.setIntBagMap(intBagMap);
        intBagMap.put(1, bagItem1);
        intBagMap.put(2, bagItem2);
        intBagMap.put(3, null);

        var longBagMap = new HashMap<Long, BagItem>();
        entity.setLongBagMap(longBagMap);
        longBagMap.put(1L, bagItem1);
        longBagMap.put(2L, bagItem2);
        longBagMap.put(3L, null);

        var floatBagMap = new HashMap<Float, BagItem>();
        entity.setFloatBagMap(floatBagMap);
        floatBagMap.put(1F, bagItem1);
        floatBagMap.put(2F, bagItem2);
        floatBagMap.put(3F, null);

        var doubleBagMap = new HashMap<Double, BagItem>();
        entity.setDoubleBagMap(doubleBagMap);
        doubleBagMap.put(1D, bagItem1);
        doubleBagMap.put(2D, bagItem2);
        doubleBagMap.put(3D, null);

        OrmContext.getAccessor().insert(entity);
        var myEntity = OrmContext.getAccessor().load(id, MapEntity.class);
        Assert.assertEquals(entity.getBagMap(), myEntity.getBagMap());
        Assert.assertEquals(entity.getBaseMap(), myEntity.getBaseMap());
        Assert.assertEquals(entity.getIntStringMap(), myEntity.getIntStringMap());
        Assert.assertEquals(entity.getIntBagMap(), myEntity.getIntBagMap());
        Assert.assertEquals(entity, myEntity);
    }
}
