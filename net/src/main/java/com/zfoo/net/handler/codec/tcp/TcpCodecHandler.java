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

package com.zfoo.net.handler.codec.tcp;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.net.packet.PacketService;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * header(4byte) + protocolId(2byte) + packet
 * header = body(bytes.length) + protocolId.length(2byte)
 *
 * @author godotg
 */
public class TcpCodecHandler extends ByteToMessageCodec<EncodedPacketInfo> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Not enough bytes to read the length header (4 bytes)
        if (in.readableBytes() <= PacketService.PACKET_HEAD_LENGTH) {
            return;
        }
        in.markReaderIndex();
        var length = in.readInt();

        // Reject illegal packet lengths to prevent oversized packets from consuming bandwidth
        if (length < 0 || length > IOUtils.BYTES_PER_MB) {
            throw new IllegalArgumentException(StringUtils.format("illegal packet [length:{}]", length));
        }

        // Not enough data in ByteBuf yet
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // readSlice avoids array copying, reducing GC pressure compared to byte[]
        var sliceByteBuf = in.readSlice(length);
        var packetInfo = NetContext.getPacketService().read(sliceByteBuf);
        out.add(packetInfo);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EncodedPacketInfo packetInfo, ByteBuf out) {
        NetContext.getPacketService().writeHeaderAndBody(out, packetInfo.getPacket(), packetInfo.getAttachment());
    }

}
