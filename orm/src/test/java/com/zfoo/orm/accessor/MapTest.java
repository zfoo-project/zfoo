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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

@Ignore
public class MapTest {
    private static final Logger log = LoggerFactory.getLogger(MapTest.class);

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

        var intBagMap = new HashMap<Integer, BagItem>();
        entity.setIntBagMap(intBagMap);
        intBagMap.put(1, bagItem1);
        intBagMap.put(2, bagItem2);
        intBagMap.put(3, bagItem3);

        var intBaseMap = new HashMap<Integer, Map<Integer, String>>();
        intBaseMap.put(1, Map.of(1, "1"));
        intBaseMap.put(2, Map.of(2, "2"));
        intBaseMap.put(3, Map.of(3, "3"));
        intBaseMap.put(4, null);
        entity.setIntBaseMap(intBaseMap);

        OrmContext.getAccessor().insert(entity);
        var myEntity = OrmContext.getAccessor().load(id, MapEntity.class);
        Assert.assertEquals(entity.getBagMap(), myEntity.getBagMap());
        Assert.assertEquals(entity.getBaseMap(), myEntity.getBaseMap());
        Assert.assertEquals(entity.getIntStringMap(), myEntity.getIntStringMap());
        Assert.assertEquals(entity.getIntBagMap(), myEntity.getIntBagMap());
        Assert.assertEquals(entity, myEntity);
    }
}
