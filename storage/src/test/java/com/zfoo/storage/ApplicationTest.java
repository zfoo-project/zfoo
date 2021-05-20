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

package com.zfoo.storage;

import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.resource.StudentResource;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author jaysunxiao
 * @version 3.0
 */

@Ignore
public class ApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

    // storage教程
    @Test
    public void startStorageTest() {
        // 加载配置文件，配置文件中必须引入storage
        // 配置文件中scan，需要映射Excel的类所在位置
        // 配置文件中resource，需要映射Excel的文件所在位置
        var context = new ClassPathXmlApplicationContext("application.xml");

        // Excel的映射内容需要在被Spring管理的bean的方法上加上@ResInjection注解，即可自动注入Excel对应的对象
        // 参考StudentManager中的标准用法

        var studentManager = context.getBean(StudentManager.class);
        var studentResources = studentManager.studentResources;
        // 类名称和Excel名称必须完全一致，Excel的列名称必须对应对象的属性名称
        for (StudentResource resource : studentResources.getAll()) {
            logger.info(JsonUtils.object2String(resource));
        }
        System.out.println(StringUtils.MULTIPLE_HYPHENS);

        // 通过id找到对应的行
        var id = 1000;
        var valueById = studentResources.get(id);
        logger.info(JsonUtils.object2String(valueById));
        System.out.println(StringUtils.MULTIPLE_HYPHENS);

        // 通过索引找对应的行
        var valuesByIndex = studentResources.getIndex("name", "james0");
        logger.info(JsonUtils.object2String(valuesByIndex));
    }
}
