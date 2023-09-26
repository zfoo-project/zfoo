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

package com.zfoo.protocol.compatible;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.packet.*;
import com.zfoo.protocol.util.*;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.zfoo.protocol.BenchmarkTesting.complexObject;
import static com.zfoo.protocol.BenchmarkTesting.normalObject;

/**
 * @author godotg
 */
@Ignore
public class CompatibleTesting {

    /**
     * EN: The order of the bytecode-enhanced Map traversal order will be different, so the order of the serialized content will change.
     * You can see that the different bytes are not consecutive.
     * <p>
     * CN: 字节码增强的Map遍历顺序会出现不一样，所以序列化的内容顺序会改变，可以看到不相同的字节并不是连续的
     */
    @Test
    public void compatibleTest() throws IOException {
        ProtocolManager.initProtocolAuto(Set.of(ComplexObject.class, NormalObject.class, SimpleObject.class, EmptyObject.class, VeryBigObject.class), GenerateOperation.NO_OPERATION);

        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("ComplexObject.bytes"));
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        buffer.writeBytes(bytes);

        var packet = ProtocolManager.read(buffer);

        var newBuffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        ProtocolManager.write(newBuffer, packet);

        buffer.resetReaderIndex();
        newBuffer.resetReaderIndex();

        var equal = 0;
        var notEqual = 0;
        for (int i = 0; i < buffer.writerIndex(); i++) {
            var a = buffer.readByte();
            var b = newBuffer.readByte();
            if (a == b) {
                equal++;
            } else {
                notEqual++;
            }
        }
        System.out.println(StringUtils.format("equal [{}], not equal [{}]", equal, notEqual));
    }


    @Test
    public void enhanceNormalTest() {
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        // 简单对象序列化和反序列化测试
        // 序列化：把normalObject序列化一下写到buffer中
        ProtocolManager.write(buffer, normalObject);
        // 反序列化：从buffer中反序列化为协议包
        var packet = ProtocolManager.read(buffer);

        buffer.clear();

        // 复杂对象序列化和反序列化测试
        ProtocolManager.write(buffer, complexObject);
        packet = ProtocolManager.read(buffer);

        buffer.clear();
    }

    @Test
    public void normalTest() {
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        ProtocolManager.write(buffer, normalObject);
        var packet = ProtocolManager.read(buffer);


        var newBuffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        ProtocolManager.write(newBuffer, packet);

        buffer.resetReaderIndex();
        newBuffer.resetReaderIndex();

        var equal = 0;
        var notEqual = 0;
        for (int i = 0; i < buffer.writerIndex(); i++) {
            var a = buffer.readByte();
            var b = newBuffer.readByte();
            if (a == b) {
                equal++;
            } else {
                notEqual++;
            }
        }
        System.out.println(StringUtils.format("equal [{}], not equal [{}]", equal, notEqual));
    }

    @Test
    public void normalWriteTest() {
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
//        normalObject.outCompatibleValue = 88;
        ProtocolManager.write(buffer, normalObject);
        FileUtils.writeInputStreamToFile(new File("normal-no-compatible.bytes"), new ByteArrayInputStream(ByteBufUtils.readAllBytes(buffer)));
    }

    @Test
    public void normalReadTest() throws IOException {
        ProtocolManager.initProtocolAuto(Set.of(ComplexObject.class, NormalObject.class, SimpleObject.class, EmptyObject.class, VeryBigObject.class), GenerateOperation.NO_OPERATION);

//        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("compatible/normal-no-compatible.bytes"));
//        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("compatible/normal-out-compatible.bytes"));
//        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("compatible/normal-inner-compatible.bytes"));
//        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("compatible/normal-out-inner-compatible.bytes"));
        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("compatible/normal-out-inner-inner-compatible.bytes"));

        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        buffer.writeBytes(bytes);
        var packet = ProtocolManager.read(buffer);

        System.out.println(JsonUtils.object2String(packet));
    }
}
