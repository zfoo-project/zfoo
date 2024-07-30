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
import com.zfoo.orm.entity.MailEntity;
import com.zfoo.orm.entity.WrongEntity;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

@Ignore
public class IdAnnotationTest {

    @Test
    public void batchInsertTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        for (int i = 0; i < 100; i++) {
            insertMail(StringUtils.format("mail-{}", i));
        }
    }

    public void insertMail(String mailId) {
        OrmContext.getAccessor().delete(mailId, MailEntity.class);
        var mailEntity = MailEntity.valueOf(mailId, "userName-" + mailId, "content" + mailId, new Date());
        OrmContext.getAccessor().insert(mailEntity);
    }

    @Test
    public void wrongCase() {
        new ClassPathXmlApplicationContext("application.xml");
        var entity = new WrongEntity();
        Exception exception = null;
        try {
            OrmContext.getAccessor().insert(entity);
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }
}
