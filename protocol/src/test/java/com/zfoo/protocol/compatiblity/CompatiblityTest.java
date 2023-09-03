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

package com.zfoo.protocol.compatiblity;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.IOUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.zfoo.protocol.SpeedTest.complexObject;
import static com.zfoo.protocol.SpeedTest.normalObject;

/**
 * @author godotg
 */
public class CompatiblityTest {

    /**
     * 字节码增强的Map变量顺序会出现不一样，所以序列化的内容顺序会改变，关闭字节码增强就会发现序列化内容相同
     * @throws IOException
     */
    @Ignore
    @Test
    public void compatiblityTest() throws IOException {
        var bytes = IOUtils.toByteArray(ClassUtils.getFileFromClassPath("ComplexObject.bytes"));
        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        buffer.writeBytes(bytes);

        var packet = ProtocolManager.read(buffer);

        var newBuffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        ProtocolManager.write(newBuffer, packet);

        buffer.resetReaderIndex();
        newBuffer.resetReaderIndex();
        for (int i = 0; i < buffer.writerIndex(); i++) {
            var a = buffer.readByte();
            var b = newBuffer.readByte();
            if (a != b) {
                System.out.println("协议兼容错误" + i);
            }
        }
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
}
