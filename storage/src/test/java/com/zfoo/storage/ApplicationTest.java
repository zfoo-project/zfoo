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

import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.manager.StorageManager;
import com.zfoo.storage.model.IStorage;
import com.zfoo.storage.resource.StudentCsvResource;
import com.zfoo.storage.resource.StudentResource;
import com.zfoo.storage.resource.TeacherResource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * @author godotg
 */

public class ApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);


    // Storage tutorial
    @Test
    public void startStorageTest() {
        // Load config file (must import the storage module)
        // 'scan': package containing Excel-mapped classes; Excel files auto-discovered in subdirs
        // 'resource': path where Excel files are located
        var context = new ClassPathXmlApplicationContext("application.xml");

        // Add @ResInjection to a Spring bean method to auto-inject Excel-mapped objects
        // See StudentManager for standard usage

        IStorage<Integer, StudentResource> storage1 = StorageContext.getStorageManager().getStorage(StudentResource.class);

        //Get data by primary key
        StudentResource studentResource = StorageContext.get(StudentResource.class, 1001);
        TeacherResource teacherResource = StorageContext.get(TeacherResource.class, 1001);
        //Get name index list
        List<StudentResource> nameIndexList = StorageContext.getIndexes(StudentResource.class, StudentResource::getName, "james1");
        // Get age index list
        List<StudentResource> ageIndexList = StorageContext.getIndexes(StudentResource.class, StudentResource::getAge, 10);

        //Get Storage instance
        IStorage<Integer, StudentResource> storage = context.getBean(StorageManager.class).getStorage(StudentResource.class);

        var studentManager = context.getBean(StudentManager.class);
        var studentResources = studentManager.studentResources;
        var studentCsvResources = studentManager.studentCsvResources;
        // Without @Resource alias, class and Excel names must match; without @ExcelFieldName, fields map to same-named columns
        for (StudentResource resource : studentResources.getAll()) {
            logger.info(JsonUtils.object2String(resource));
        }
        System.out.println(StringUtils.MULTIPLE_HYPHENS);

        // Find row by id
        var id = 1002;
        var valueById = studentResources.get(id);
        logger.info(JsonUtils.object2String(valueById));
        System.out.println(StringUtils.MULTIPLE_HYPHENS);

        // Find row by index
        var valuesByIndex = studentResources.getIndexes(StudentResource::getName, "james0");
        logger.info(JsonUtils.object2String(valuesByIndex));

        // Find row by index
        var csvValuesByIndex = studentCsvResources.getIndexes(StudentCsvResource::getName, "james0");
        logger.info(JsonUtils.object2String(csvValuesByIndex));

        // Add @ResInjection to a Spring bean method to auto-inject Excel-mapped objects
        var testManager = context.getBean(TestManager.class);
        var testResources = testManager.testResources;
        for (com.zfoo.storage.resource.TestResource resource : testResources.getAll()) {
            Map<Integer, String> map = resource.getType9();
            AssertionUtils.notNull(map.get(1));
            logger.info(JsonUtils.object2String(resource));
        }
        // Find row by id
        id = 2;
        var resource = testResources.get(id);
        logger.info(JsonUtils.object2String(resource));
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
    }

}
