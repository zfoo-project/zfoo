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

package com.zfoo.orm.text;

import com.mongodb.client.model.Filters;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.UserEntity;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class TextSearchTest {

    @Test
    public void insertTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        var listUser = new ArrayList<UserEntity>();
        var userEntity = new UserEntity(1, (byte) 1, (short) 1, 1, true, "one", null);
        listUser.add(userEntity);
        userEntity = new UserEntity(2, (byte) 2, (short) 2, 2, true, "one two", null);
        listUser.add(userEntity);
        userEntity = new UserEntity(3, (byte) 3, (short) 3, 3, true, "one two three", null);
        listUser.add(userEntity);
        OrmContext.getAccessor().batchInsert(listUser);
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void queryTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        var collection = OrmContext.getOrmManager().getCollection(UserEntity.class);
        collection.find(Filters.text("\"one\" -\"three\"")).forEach(new Consumer<UserEntity>() {
            @Override
            public void accept(UserEntity userEntity) {
                System.out.println(userEntity);
            }
        });
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
