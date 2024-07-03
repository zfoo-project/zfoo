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
import com.zfoo.orm.entity.UserEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;

/**
 * @author godotg
 */
@Ignore
public class AccessorTest {

    @Test
    public void oneTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        delete();
        insert();
        delete();
        insert();
        update();
        load();
    }

    @Test
    public void batchTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        deleteAll();
        batchInsert();
        batchUpdate();
        queryAll();
        deleteAll();
        queryAll();
    }

    public void insert() {
        var userEntity = new UserEntity(1, (byte) 2, (short) 3, 5, true, "orm", "orm");
        OrmContext.getAccessor().insert(userEntity);
    }


    public void delete() {
        OrmContext.getAccessor().delete(1L, UserEntity.class);
    }

    public void update() {
        var userEntity = new UserEntity(1, (byte) 2, (short) 3, 5, true, "update", "update");
        OrmContext.getAccessor().update(userEntity);
    }

    public void load() {
        var ent = (UserEntity) OrmContext.getAccessor().load(1L, UserEntity.class);
        System.out.println(ent);
    }


    // 批量插入
    public void batchInsert() {
        var list = new ArrayList<UserEntity>();
        for (var i = 1; i <= 10; i++) {
            var userEntity = new UserEntity(i, (byte) 1, (short) i, i, true, "helloOrm" + i, "helloOrm" + i);
            list.add(userEntity);
        }
        OrmContext.getAccessor().batchInsert(list);
    }

    public void batchUpdate() {
        var list = new ArrayList<UserEntity>();
        for (var i = 1; i <= 10; i++) {
            var userEntity = new UserEntity(i, (byte) 1, (short) i, i, true, "update-" + i, "helloOrm" + i);
            list.add(userEntity);
        }
        OrmContext.getAccessor().batchUpdate(list);
    }

    public void queryAll() {
        var list = OrmContext.getQuery(UserEntity.class).queryAll();
        list.forEach(it -> System.out.println(it));
    }

    public void deleteAll() {
        var list = OrmContext.getQuery(UserEntity.class).queryAll();
        OrmContext.getAccessor().batchDelete(list);
    }

}
