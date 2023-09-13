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
 *
 */
package com.zfoo.protocol.jprotobuf;

import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.buffer.ByteBufUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author godotg
 */
@Ignore
public class ProtobufTagTesting {

    @Test
    public void test() throws IOException {
        var buffer = new byte[1024 * 8];
        var output = CodedOutputStream.newInstance(buffer);
        output.writeSInt32NoTag(Integer.MAX_VALUE);

        var length = output.getTotalBytesWritten();
        for (int i = 0; i < length; i++) {
            System.out.println(buffer[i]);
        }
        System.out.println("----------------------------------");

        var byteBuf = new UnpooledUnsafeHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        ByteBufUtils.writeInt(byteBuf, Integer.MAX_VALUE);
        var bytes = ByteBufUtils.readAllBytes(byteBuf);
        for (int i = 0; i < bytes.length; i++) {
            System.out.println(bytes[i]);
        }
    }

}
