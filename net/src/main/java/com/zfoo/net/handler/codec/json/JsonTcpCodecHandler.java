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

package com.zfoo.net.handler.codec.json;

import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

import static com.zfoo.net.packet.PacketService.PACKET_HEAD_LENGTH;

/**
 * header(4byte) + json string
 * header = json body(json.length)
 *
 * @author godotg
 */
public class JsonTcpCodecHandler extends ByteToMessageCodec<EncodedPacketInfo> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 不够读一个int
        if (in.readableBytes() <= PACKET_HEAD_LENGTH) {
            return;
        }
        in.markReaderIndex();
        var length = in.readInt();

        // 如果长度非法，则抛出异常断开连接，按照自己的使用场景指定合适的长度，防止客户端发送超大包占用带宽
        if (length < 0 || length > IOUtils.BYTES_PER_MB) {
            throw new IllegalArgumentException(StringUtils.format("illegal packet [length:{}]", length));
        }

        // ByteBuf里的数据太小
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // readSlice和byte[]数组相比，readSlice减少了垃圾回收
        var sliceByteBuf = in.readSlice(length);
        var packetInfo = JsonPacket.readDecodedPacketInfo(sliceByteBuf);
        out.add(packetInfo);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EncodedPacketInfo packetInfo, ByteBuf out) {
        out.writeInt(PACKET_HEAD_LENGTH);
        JsonPacket.writeEncodedPacketInfo(out, packetInfo);
        int length = out.readableBytes();
        int packetLength = length - PACKET_HEAD_LENGTH;
        out.writerIndex(0);
        out.writeInt(packetLength);
        out.writerIndex(length);
    }

}
