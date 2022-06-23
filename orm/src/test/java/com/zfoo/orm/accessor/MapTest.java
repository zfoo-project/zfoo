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
    public void insertMapData() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        OrmContext.getAccessor().delete(1, MapEntity.class);

        var entity = new MapEntity();
        entity.setId(1);

        var bagMap = new HashMap<String, BagItem>();
        entity.setBagMap(bagMap);

        var itemMap = new HashMap<String, Item>();
        itemMap.put("1", new Item(1, "item1"));
        itemMap.put("2", new Item(2, "item1"));
        itemMap.put("3", new Item(3, "item1"));

        var bagItem1 = new BagItem(1, "desc1", itemMap);
        var bagItem2 = new BagItem(2, "desc2", itemMap);
        var bagItem3 = new BagItem(3, "desc3", itemMap);

        bagMap.put("bag1", bagItem1);
        bagMap.put("bag2", bagItem2);
        bagMap.put("bag3", bagItem3);

        var map = new HashMap<String, Map<String, String>>();
        map.put("a", Map.of("b", "b"));
        entity.setBaseMap(map);

        OrmContext.getAccessor().insert(entity);

        var myEntity = OrmContext.getAccessor().load(1, MapEntity.class);
        Assert.assertEquals(entity, myEntity);
    }
}
