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

import com.mongodb.client.model.Filters;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.UserEntity;
import com.zfoo.orm.model.cache.IEntityCaches;
import com.zfoo.util.ThreadUtils;
import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class UpdateTest {


    @Test
    public void test() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");

        IEntityCaches<Long, UserEntity> userEntityCaches = (IEntityCaches<Long, UserEntity>) OrmContext.getOrmManager().getEntityCaches(UserEntity.class);

        for (int i = 0; i < 10; i++) {
            var userEnt = userEntityCaches.load((long) i);
            userEnt.setE("update" + i);
            userEnt.setC(i);
            userEntityCaches.update(userEnt);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    @Test
    public void collectionTest() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application.xml");

        var collection = OrmContext.getOrmManager().getCollection(UserEntity.class);
        var result = collection.updateOne(Filters.eq("_id", 1), new Document("$inc", new Document("c", 1L)));
        System.out.println(result);
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
