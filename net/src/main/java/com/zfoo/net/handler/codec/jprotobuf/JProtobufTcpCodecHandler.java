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

package com.zfoo.net.handler.codec.jprotobuf;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.zfoo.net.packet.DecodedPacketInfo;
import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.net.packet.PacketService;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.IOException;
import java.util.List;

/**
 * header(4byte) + protocolId(2byte) + packet
 * header = body(bytes.length) + protocolId.length(2byte)
 *
 * @author godotg
 */
public class JProtobufTcpCodecHandler extends ByteToMessageCodec<EncodedPacketInfo> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        // 不够读一个int
        if (in.readableBytes() <= PacketService.PACKET_HEAD_LENGTH) {
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

        var sliceByteBuf = in.readSlice(length);
        var packetInfo = read(sliceByteBuf);
        out.add(packetInfo);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EncodedPacketInfo packetInfo, ByteBuf out) throws IOException {
        write(out, packetInfo.getPacket(), packetInfo.getAttachment());
    }

    public static DecodedPacketInfo read(ByteBuf buffer) throws IOException {
        var protocolId = ByteBufUtils.readShort(buffer);
        var protocolRegistration = ProtocolManager.getProtocol(protocolId);
        var protocolClass = protocolRegistration.protocolConstructor().getDeclaringClass();

        var protobufCodec = ProtobufProxy.create(protocolClass);

        var bytes = ByteBufUtils.readAllBytes(buffer);
        var packet = protobufCodec.decode(bytes);

        return DecodedPacketInfo.valueOf(packet, null);
    }

    public void write(ByteBuf buffer, Object packet, Object attachment) throws IOException {
        // 写入protobuf协议
        @SuppressWarnings("unchecked")
        var protobufCodec = (Codec<Object>) ProtobufProxy.create(packet.getClass());
        byte[] bytes = protobufCodec.encode(packet);
        // header(4byte) + protocolId(2byte)
        buffer.writeInt(bytes.length + 2);

        var protocolId = ProtocolManager.protocolId(packet.getClass());
        // 写入协议号
        ByteBufUtils.writeShort(buffer, protocolId);

        buffer.writeBytes(bytes);
    }
}
