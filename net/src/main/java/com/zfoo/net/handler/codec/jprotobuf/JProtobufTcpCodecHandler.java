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
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.packet.model.EncodedPacketInfo;
import com.zfoo.net.packet.service.PacketService;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * header(4byte) + protocolId(2byte) + packet
 * header = body(bytes.length) + protocolId.length(2byte)
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class JProtobufTcpCodecHandler extends ByteToMessageCodec<EncodedPacketInfo> {

    private static final Logger logger = LoggerFactory.getLogger(JProtobufTcpCodecHandler.class);


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 不够读一个int
        if (in.readableBytes() <= PacketService.PACKET_HEAD_LENGTH) {
            return;
        }
        in.markReaderIndex();
        var length = in.readInt();

        // 如果长度非法，则抛出异常断开连接
        if (length < 0) {
            throw new IllegalArgumentException(StringUtils.format("[session:{}]的包头长度[length:{}]非法"
                    , SessionUtils.sessionInfo(ctx), length));
        }

        // ByteBuf里的数据太小
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        ByteBuf tmpByteBuf = null;
        try {
            tmpByteBuf = in.readRetainedSlice(length);
            DecodedPacketInfo packetInfo = read(tmpByteBuf);
            out.add(packetInfo);
        } catch (Exception e) {
            logger.error("[session:{}]解码exception异常", SessionUtils.sessionInfo(ctx), e);
        } catch (Throwable t) {
            logger.error("[session:{}]解码throwable错误", SessionUtils.sessionInfo(ctx), t);
        } finally {
            ReferenceCountUtil.release(tmpByteBuf);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EncodedPacketInfo packetInfo, ByteBuf out) {
        try {
            write(out, packetInfo.getPacket(), packetInfo.getAttachment());
        } catch (Exception e) {
            logger.error("[session:{}][{}]编码exception异常", SessionUtils.sessionInfo(ctx), packetInfo.getPacket().getClass().getSimpleName(), e);
        } catch (Throwable t) {
            logger.error("[session:{}][{}]编码throwable错误", SessionUtils.sessionInfo(ctx), packetInfo.getPacket().getClass().getSimpleName(), t);
        }
    }

    public static DecodedPacketInfo read(ByteBuf buffer) throws IOException {
        var protocolId = ByteBufUtils.readShort(buffer);
        var protocolRegistration = ProtocolManager.getProtocol(protocolId);
        var protocolClass = protocolRegistration.protocolConstructor().getDeclaringClass();

        var protobufCodec = ProtobufProxy.create(protocolClass);

        var bytes = ByteBufUtils.readAllBytes(buffer);
        var packet = protobufCodec.decode(bytes);

        return DecodedPacketInfo.valueOf((IPacket) packet, null);
    }

    public void write(ByteBuf buffer, IPacket packet, IAttachment attachment) throws IOException {
        if (packet == null) {
            logger.error("packet is null and can not be sent.");
            return;
        }

        // 写入protobuf协议
        var protobufCodec = (Codec<IPacket>) ProtobufProxy.create(packet.getClass());
        byte[] bytes = protobufCodec.encode(packet);
        // header(4byte) + protocolId(2byte)
        buffer.writeInt(bytes.length + 2);

        var protocolId = packet.protocolId();
        // 写入协议号
        ByteBufUtils.writeShort(buffer, protocolId);

        buffer.writeBytes(bytes);
    }
}
