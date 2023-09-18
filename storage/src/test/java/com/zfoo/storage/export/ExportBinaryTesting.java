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
 */

package com.zfoo.storage.export;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.FieldUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.storage.anno.AliasFieldName;
import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Index;
import com.zfoo.storage.anno.Storage;
import com.zfoo.storage.config.StorageConfig;
import com.zfoo.storage.manager.StorageInt;
import com.zfoo.storage.manager.StorageManager;
import com.zfoo.storage.util.ExportUtils;
import com.zfoo.storage.util.LambdaUtils;
import com.zfoo.storage.util.function.Func1;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author godotg
 */


@Ignore
public class ExportBinaryTesting {

    public static class User {

        public String id;
        public String name;
        public String sex;
        public int age;

    }

    @Storage
    public record StudentResource(
            @Id
            int id,
            @Index(unique = true)
            String idCard,
            @Index
            String name,
            @Index
            @AliasFieldName("年龄")
            int age,
            float score,
            String[] courses,
            User[] users,
            User user
    ) {


    }


    public static class ResourceData {

        public Map<Integer, StudentResource> studentResources;

    }

    @Test
    public void test() throws Exception {
        var config = new StorageConfig();
        config.setScanPackage("com.zfoo.storage.export");
        config.setResourceLocation("classpath:/excel");
        config.setWriteable(true);
        config.setRecycle(false);
        var storageManager = new StorageManager();
        storageManager.setStorageConfig(config);
        storageManager.initBefore();
        storageManager.initAfter();

        // 生成协议
        var protocols = new HashSet<Class<?>>();
        protocols.add(ResourceData.class);
        protocols.addAll(storageManager.storageMap().keySet());
        var operation = new GenerateOperation();
        operation.setProtocolPath("D:\\github\\godot-bird\\test\\storage\\protocol");
        operation.getGenerateLanguages().add(CodeLanguage.GdScript);
        ProtocolManager.initProtocolAuto(protocols, operation);

        // 生成数据
        var resourceData = ExportUtils.autoWrapData(ResourceData.class, storageManager.storageMap());
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, Integer.MAX_VALUE);
        ProtocolManager.write(buffer, resourceData);
        var bytes = ByteBufUtils.readAllBytes(buffer);
        FileUtils.writeInputStreamToFile(new File("D:/github/godot-bird/binary_data.cfg"), new ByteArrayInputStream(bytes));

        Func1<StudentResource, Integer> ageFunc = StudentResource::age;
        String methodName = LambdaUtils.extract(ageFunc).getImplMethodName();
        String fieldName = FieldUtils.methodToProperty(methodName);
        System.out.println(methodName);
        System.out.println(fieldName);


        //获取storage对象
        var storage = storageManager.getStorage(StudentResource.class);
        //获取唯一索引的对象
        StudentResource uniqueIndex = storage.getUniqueIndex(StudentResource::idCard, "110101200007281903");
        System.out.println(JsonUtils.object2String(uniqueIndex));
        //获取年龄索引对象列表
        List<StudentResource> ageIndexList = storage.getIndexes(StudentResource::age, 10);
        for (StudentResource resource : ageIndexList) {
            System.out.println(JsonUtils.object2String(resource));
        }
        //获取名称索引对象列表
        List<StudentResource> nameIndexList = storage.getIndexes(StudentResource::name, "james1");
        for (StudentResource resource : nameIndexList) {
            System.out.println(JsonUtils.object2String(resource));
        }
        //获取所有列表对象
        List<StudentResource> list = storage.getList();
        for (StudentResource resource : list) {
            System.out.println(JsonUtils.object2String(resource));
        }
        //获取泛型的storage对象
        StorageInt<Integer, StudentResource> genericStorage = storageManager.getStorage(StudentResource.class);
        //获取配置表的所有数据
        List<StudentResource> all = storageManager.getList(StudentResource.class);
        for (StudentResource resource : all) {
            System.out.println(JsonUtils.object2String(resource));
        }
        //获取配置表索引的数据
        List<StudentResource> ageIndexes = storageManager.getIndexes(StudentResource.class, StudentResource::age, 10);
        for (StudentResource resource : ageIndexes) {
            System.out.println(JsonUtils.object2String(resource));
        }
        //获取配置表索引的数据
        List<StudentResource> nameIndexes = storageManager.getIndexes(StudentResource.class, StudentResource::name, "james1");
        for (StudentResource resource : nameIndexes) {
            System.out.println(JsonUtils.object2String(resource));
        }
        //获取主键ID的唯一数据
        StudentResource idResource = storageManager.get(StudentResource.class, 1001);
        System.out.println(JsonUtils.object2String(idResource));

        for (StudentResource resource : storage.getAll()) {
            System.out.println(JsonUtils.object2String(resource));
        }
    }

}
