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

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.model.BigPacket;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

/**
 * @author godotg
 */
@Ignore
public class ByteBufUtilsTesting {

    @Ignore
    @Test
    public void readLongSpeedTest() {
        var startTime = System.currentTimeMillis();
        long[] values = new long[]{Long.MIN_VALUE, -9999999999999999L, -9999999999999999L, -99999999, -9999, -100, -2, -1
                , 0, 1, 2, 100, 9999, 99999999, 9999999999999999L, Long.MAX_VALUE};
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            buffer.clear();
            for (long value : values) {
                ByteBufUtils.writeLong(buffer, value);
                ByteBufUtils.readLong(buffer);
            }
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }


    /**
     * 测试所有int值，运行时间太长，放弃测试
     */
    @Ignore
    @Test
    public void readIntTest() {
        ByteBuf byteBuf = Unpooled.buffer();
        for (int value = Integer.MIN_VALUE; value < Integer.MAX_VALUE; value++) {
            byteBuf.clear();
            ByteBufUtils.writeInt(byteBuf, value);
            int result = ByteBufUtils.readInt(byteBuf);
            Assert.assertEquals(result, value);
        }
    }


    @Ignore
    @Test
    public void readLongTest() {
        ByteBuf byteBuf = Unpooled.buffer();
        for (long value = Long.MIN_VALUE; value < Long.MAX_VALUE; value++) {
            byteBuf.clear();
            ByteBufUtils.writeLong(byteBuf, value);
            Assert.assertEquals(ByteBufUtils.readLong(byteBuf), value);
        }
    }

    @Ignore
    @Test
    public void bigDataTest() {
        ProtocolManager.initProtocol(Set.of(BigPacket.class));

        var bigPact = new BigPacket();
        Arrays.fill(bigPact.a, 99);

        var buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 100_0000);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10_0000; i++) {
            buffer.clear();
            ProtocolManager.write(buffer, bigPact);
            var newPacket = ProtocolManager.read(buffer);
        }

        System.out.println(StringUtils.format("[zfoo][size:{}] [time:{}]", buffer.writerIndex(), System.currentTimeMillis() - startTime));
    }

}
