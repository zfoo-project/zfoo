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

package com.zfoo.orm.util;

import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * MongoDB分布式唯一Id测试
 *
 * @author godotg
 * @version 3.0
 */
@Ignore
public class MongoUuidUtilsTest {

    @Test
    public void startApplication0() {
        incrementMongoIdTest();
    }

    @Test
    public void startApplication1() {
        incrementMongoIdTest();
    }

    @Test
    public void startApplication2() {
        incrementMongoIdTest();
    }

    @Test
    public void startApplication3() {
        incrementMongoIdTest();
    }

    private void incrementMongoIdTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        ThreadUtils.sleep(8000);

        var count = 0L;
        for (int i = 0; i < 100_0000; i++) {
            count = MongoIdUtils.getIncrementIdFromMongoDefault("myDocument");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                var count = 0L;
                for (int i = 0; i < 100_0000; i++) {
                    count = MongoIdUtils.getIncrementIdFromMongoDefault("myDocument");
                }
                System.out.println(count);
            }
        }).start();
        System.out.println(count);

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void reset() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        var id = MongoIdUtils.getIncrementIdFromMongoDefault("myDocument");
        System.out.println(id);
        MongoIdUtils.resetIncrementIdFromMongoDefault("myDocument");
    }

    @Test
    public void set() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        MongoIdUtils.setIncrementIdFromMongo("myDocument", 100);
    }
}
