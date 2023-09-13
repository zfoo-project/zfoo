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

package com.zfoo.orm.query;

import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.UserEntity;
import com.zfoo.orm.util.MongoIdUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author godotg
 */
@Ignore
public class QueryTest {

    @Test
    public void queryAllTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        var id = MongoIdUtils.getIncrementIdFromMongoDefault(UserEntity.class);
        var entity = new UserEntity();
        entity.setId(id);
        entity.setC((int)id);
        entity.setE("User_"+id);
        OrmContext.getAccessor().insert(entity);
        var list = OrmContext.getQuery(UserEntity.class).eq("e", "User_1").eq("f", null).queryAll();
        System.out.println(list);
    }

    @Test
    public void queryByFieldTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        var list = OrmContext.getQuery(UserEntity.class).eq("a", 1).queryAll();
        System.out.println(list);
    }


}
