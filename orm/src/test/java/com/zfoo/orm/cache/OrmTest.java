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

package com.zfoo.orm.cache;

import com.zfoo.orm.OrmContext;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author godotg
 */
@Ignore
public class OrmTest {

    @Test
    public void test() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        // 通过注解自动注入的方式去拿到UserEntity的EntityCaches
        var userEntityCaches = context.getBean(UserManager.class).userEntityCaches;

        for (int i = 1; i <= 10; i++) {
            var entity = userEntityCaches.load((long) i);
            entity.setE("update" + i);
            entity.setC(i);
            userEntityCaches.update(entity);
        }

        OrmContext.getOrmManager().getAllEntityCaches().forEach(it -> System.out.println(it.recordStatus()));

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
