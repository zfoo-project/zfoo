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

package com.zfoo.protocol.buffer;

import com.zfoo.protocol.collection.CollectionUtils;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * 自定义私有协议格式，可以针对性的对存在性能瓶颈的数据结构做特定优化
 *
 * @author godotg
 */
public abstract class CustomByteBuf {

    // -------------------------------------------------------------------------------------------------------------
    // 针对于int数组提高性能的简单方式
    public static void writeIntArraySimple(ByteBuf byteBuf, int[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        var length = array.length;
        ByteBufUtils.writeInt(byteBuf, length);
        var writeIndex = byteBuf.writerIndex();
        byteBuf.ensureWritable(length * 4);
        for (var value : array) {
            byteBuf.setInt(writeIndex, value);
            writeIndex += 4;
        }
        byteBuf.writerIndex(writeIndex);
    }

    public static int[] readIntArraySimple(ByteBuf byteBuf) {
        var length = ByteBufUtils.readInt(byteBuf);
        var ints = new int[CollectionUtils.comfortableLength(length)];
        var readIndex = byteBuf.readerIndex();
        for (var i = 0; i < length; i++) {
            ints[i] = byteBuf.getInt(readIndex);
            readIndex += 4;
        }
        byteBuf.readerIndex(readIndex);
        return ints;
    }

    // 针对于int数组提高性能的复杂方式
    public static void writeIntArrayComplex(ByteBuf byteBuf, int[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        var length = array.length;
        ByteBufUtils.writeInt(byteBuf, length);

        var byteBuffer = ByteBuffer.allocate(length * 4);
        var intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(array);
        byteBuf.writeBytes(byteBuffer);
    }

    public static int[] readIntArrayComplex(ByteBuf byteBuf) {
        var length = ByteBufUtils.readInt(byteBuf);
        var byteBuffer = ByteBuffer.allocate(length * 4);
        byteBuf.readBytes(byteBuffer);
        byteBuffer.rewind();
        var intBuffer = byteBuffer.asIntBuffer();
        int[] ints = new int[length];
        intBuffer.get(ints);
        return ints;
    }

}
