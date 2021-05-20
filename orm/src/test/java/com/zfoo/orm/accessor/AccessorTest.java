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
import com.zfoo.orm.TestUnit;
import com.zfoo.orm.entity.UserEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class AccessorTest {

    @Test
    public void testBatchInsert() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);
        List<UserEntity> listUser = new ArrayList<>();
        for (int i = 0; i <= TestUnit.NUM; i++) {
            UserEntity userEntity = new UserEntity(i, (byte) 1, (short) i, i, true, "helloOrm" + i, "helloOrm" + i);
            listUser.add(userEntity);
        }
        OrmContext.getAccessor().batchInsert(listUser);
    }

    @Test
    public void testInsert() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);
        UserEntity userEntity = new UserEntity(1, (byte) 2, (short) 3, 5, true, "helloORM", "helloOrm");
        OrmContext.getAccessor().insert(userEntity);
    }

    @Test
    public void testUpdate() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);
        UserEntity userEntity = new UserEntity(1, (byte) 2, (short) 3, 5, true, "helloUpdate", "helloOrm");
        OrmContext.getAccessor().update(userEntity);
    }

    @Test
    public void testBatchUpdate() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);
        UserEntity userEntity = new UserEntity(1, (byte) 2, (short) 3, 5, true, "helloBatchUpdate", "helloOrm");
        userEntity.setC(222);
        OrmContext.getAccessor().batchUpdate(Collections.singletonList(userEntity));
    }

    @Test
    public void testLoad() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);
        UserEntity ent = (UserEntity) OrmContext.getAccessor().load(1L, UserEntity.class);
        System.out.println(ent);
    }

    @Test
    public void testDelete() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(TestUnit.CONFIG_LOCATION);
        OrmContext.getAccessor().delete(1L, UserEntity.class);
    }

}
