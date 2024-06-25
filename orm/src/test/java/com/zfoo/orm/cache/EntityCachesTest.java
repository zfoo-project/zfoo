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

import com.mongodb.client.model.Filters;
import com.zfoo.event.manager.EventBus;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.MailEntity;
import com.zfoo.orm.entity.UserEntity;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;


/**
 * @author godotg
 */
@Ignore
public class EntityCachesTest {


    @Test
    public void test() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        // 每次运行前先删除数据库
        var collection = OrmContext.getOrmManager().getCollection(UserEntity.class);
        collection.drop();

        // 再插入
        batchInsert();

        // 动态去拿到UserEntity的EntityCaches
        @SuppressWarnings("unchecked")
        var userEntityCaches = (IEntityCache<Long, UserEntity>) OrmContext.getOrmManager().getEntityCaches(UserEntity.class);

        for (var i = 1; i <= 10; i++) {
            var entity = userEntityCaches.load((long) i);
            entity.setE("update" + i);
            entity.setC(i);
            EventBus.asyncExecute(() -> userEntityCaches.update(entity));
        }

        ThreadUtils.sleep(60 * TimeUtils.MILLIS_PER_SECOND);

        userEntityCaches.load(1L);
    }

    @Test
    public void testLoadOrInsert() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        // 动态去拿到UserEntity的EntityCaches
        @SuppressWarnings("unchecked")
        var mailEntityCaches = (IEntityCache<String, MailEntity>) OrmContext.getOrmManager().getEntityCaches(MailEntity.class);

        for (var i = 1; i <= 10; i++) {
            var entity = mailEntityCaches.loadOrCreate(String.valueOf(i));
            entity.setContent("msg:" + i);
            mailEntityCaches.update(entity);
        }

        ThreadUtils.sleep(60 * TimeUtils.MILLIS_PER_SECOND);

        mailEntityCaches.load("1");
    }

    @Test
    public void collectionTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        var collection = OrmContext.getOrmManager().getCollection(UserEntity.class);
        var result = collection.updateOne(Filters.eq("_id", 1), new Document("$inc", new Document("c", 1L)));
        System.out.println(result);
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void batchInsert() {
        var listUser = new ArrayList<UserEntity>();
        for (var i = 1; i <= 10; i++) {
            var userEntity = new UserEntity(i, (byte) 1, (short) i, i, true, "helloOrm" + i, "helloOrm" + i);
            listUser.add(userEntity);
        }
        OrmContext.getAccessor().batchInsert(listUser);
    }

}
