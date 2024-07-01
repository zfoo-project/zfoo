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
 *
 */

package com.zfoo.orm.cache;

import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.wrapper.*;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author godotg
 */
@Ignore
public class WrapperTest {

    @Test
    public void wrapperTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        // 每次运行前先删除数据库
        OrmContext.getOrmManager().getCollection(IntEntity.class).drop();
        OrmContext.getOrmManager().getCollection(LongEntity.class).drop();
        OrmContext.getOrmManager().getCollection(FloatEntity.class).drop();
        OrmContext.getOrmManager().getCollection(DoubleEntity.class).drop();
        OrmContext.getOrmManager().getCollection(StringEntity.class).drop();
        OrmContext.getOrmManager().getCollection(ObjectIdEntity.class).drop();

        var userManager = OrmContext.getApplicationContext().getBean(UserManager.class);
        var intEntityCaches = userManager.intEntityCaches;
        var longEntityCaches = userManager.longEntityCaches;
        var floatEntityCaches = userManager.floatEntityCaches;
        var doubleEntityCaches = userManager.doubleEntityCaches;
        var stringEntityCaches = userManager.stringEntityCaches;
        var objectIdEntityCaches = userManager.objectIdEntityCaches;

        for (var i = 1; i < 10; i++) {
            for (var j = 1; j < 10; j++) {
                var intEntity = intEntityCaches.loadOrCreate(j);
                intEntity.setMessage(StringUtils.format("update-{}-{}", i, j));
                intEntityCaches.update(intEntity);

                var longEntity = longEntityCaches.loadOrCreate((long) j);
                longEntity.setMessage(StringUtils.format("update-{}-{}", i, j));
                longEntityCaches.update(longEntity);

                var floatEntity = floatEntityCaches.loadOrCreate(Float.valueOf(i + "." + j));
                floatEntity.setMessage(StringUtils.format("update-{}-{}", i, j));
                floatEntityCaches.update(floatEntity);

                var doubleEntity = doubleEntityCaches.loadOrCreate(Double.valueOf(i + "." + j));
                doubleEntity.setMessage(StringUtils.format("update-{}-{}", i, j));
                doubleEntityCaches.update(doubleEntity);

                var stringEntity = stringEntityCaches.loadOrCreate(String.valueOf(j));
                stringEntity.setMessage(StringUtils.format("update-{}-{}", i, j));
                stringEntityCaches.update(stringEntity);

                var objectIdEntity = objectIdEntityCaches.loadOrCreate(new ObjectId(String.valueOf(j).repeat(24)));
                objectIdEntity.setMessage(StringUtils.format("update-{}-{}", i, j));
                objectIdEntityCaches.update(objectIdEntity);
            }

            ThreadUtils.sleep(60 * TimeUtils.MILLIS_PER_SECOND);
        }

        ThreadUtils.sleep(60 * TimeUtils.MILLIS_PER_SECOND);
    }

}
