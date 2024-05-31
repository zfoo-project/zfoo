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
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.storage.anno.AliasFieldName;
import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Storage;
import com.zfoo.storage.config.StorageConfig;
import com.zfoo.storage.manager.StorageManager;
import com.zfoo.storage.util.ExportUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
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

    // 对应的excel表格，变量对应excel中的列，只有定义过的变量才能导出，没有定义的变量不会导出
    @Storage
    public record StudentResource(
            @Id
            int id,
            String idCard,
            String name,
            @AliasFieldName("年龄")
            int age,
            float score,
            String[] courses,
            User[] users,
            User user
    ) {
    }

    // 需要定义一个类将所有的excel对象包裹起来
    public static class ResourceData {

        public Map<Integer, StudentResource> studentResources;

    }

    // excel对应的java文件路径，比如ResourceData，StudentResource
    public static final String excelClassPath = "com.zfoo.storage.export";
    // excel文件路径，如果是类路径以classpath开头，如果是目录文件路径则以file开头
    public static final String excelFilePath = "classpath:/excel";
    // excel输出的gdscript文件路径，会将excel转为gdscript对象
    public static final String gdscriptOutputPath = "D:\\github\\godot-bird\\excel";
    // excel导出的二进制文件路径
    public static final String binaryOutputFilePath = "D:/github/godot-bird/excel/binary_data.cfg";


    @Test
    public void test() throws Exception {
        var config = new StorageConfig();
        config.setScanPackage(excelClassPath);
        config.setResourceLocation(excelFilePath);
        config.setWriteable(true);
        config.setRecycle(false);
        var storageManager = new StorageManager();
        storageManager.setStorageConfig(config);
        storageManager.initBefore();
        storageManager.initAfter();

        // 生成协议
        var protocols = new ArrayList<Class<?>>();
        protocols.add(ResourceData.class);
        protocols.addAll(storageManager.storageMap().keySet());
        var operation = new GenerateOperation();
        operation.setProtocolPath(gdscriptOutputPath);
        operation.getGenerateLanguages().add(CodeLanguage.GdScript);
        ProtocolManager.initProtocolAuto(protocols, operation);

        // 生成数据
        var resourceData = ExportUtils.autoWrapData(ResourceData.class, storageManager.storageMap());
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, Integer.MAX_VALUE);
        ProtocolManager.write(buffer, resourceData);
        var bytes = ByteBufUtils.readAllBytes(buffer);
        FileUtils.writeInputStreamToFile(new File(binaryOutputFilePath), new ByteArrayInputStream(bytes));
    }

}
