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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Ignore
public class MapTest {
    private static final Logger log = LoggerFactory.getLogger(MapTest.class);

    @Test
    public void insertMapData() {

        var context = new ClassPathXmlApplicationContext("application.xml");
        MapEntity entity = OrmContext.getAccessor().load(1, MapEntity.class);
        if (entity == null) {
            entity = new MapEntity();
            entity.setId(1);
            entity.getRoleBag().computeIfAbsent("1", k -> new BagItem())
                    .getMapItem().computeIfAbsent("2", k -> new Item());
            OrmContext.getAccessor().insert(entity);
            log.info("数据插入成功 {}", entity);
        } else {
            log.info("entity已存在 {}", entity);
        }

    }
}