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

import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.storage.manager.StorageManager;
import com.zfoo.storage.model.anno.Id;
import com.zfoo.storage.model.config.StorageConfig;
import com.zfoo.storage.model.vo.Storage;
import com.zfoo.storage.util.ExportUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Map;

/**
 * @author godotg
 * @version 3.0
 */


@Ignore
public class ExportBinaryTest {

    public static class User implements IPacket {

        public String id;
        public String name;
        public String sex;
        public int age;

    }


    public static class StudentResource implements IPacket {

        @Id
        public int id;

        public String name;
        public int age;
        public float score;
        public String[] courses;
        public User[] users;
        public User user;

    }


    public static class ResourceData implements IPacket {

        public Map<Integer, StudentResource> studentResources;

    }

    @Test
    public void test() throws Exception {
        var config = new StorageConfig();
        config.setScanPackage(new String[]{"com.zfoo.storage.export"});

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

        var storage = (Storage<Integer, StudentResource>) storageManager.getStorage(StudentResource.class);
        for (StudentResource resource : storage.getAll()) {
            System.out.println(JsonUtils.object2String(resource));
        }
    }

}
