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
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.packet.model.EncodedPacketInfo;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * header(4byte) + protocolId(2byte) + packet
 * header = body(bytes.length) + protocolId.length(2byte)
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class TcpPacketCodecHandler extends ByteToMessageCodec<EncodedPacketInfo> {

    private static final Logger logger = LoggerFactory.getLogger(TcpPacketCodecHandler.class);

    private int length;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // 不够读一个int
            if (in.readableBytes() <= ProtocolManager.PROTOCOL_HEAD_LENGTH) {
                return;
            }
            in.markReaderIndex();
            length = in.readInt();

            // 如果长度超过限制，则抛出异常断开连接
            if (length < 0) {
                throw new IllegalArgumentException(StringUtils
                        .format("[session:{}]的包头长度[length:{}]非法[maxLength:{}]"
                                , SessionUtils.sessionInfo(ctx), length));
            }

            // ByteBuf里的数据太小
            if (in.readableBytes() < length) {
                in.resetReaderIndex();
                return;
            }

            var tmpByteBuf = in.readRetainedSlice(length);
            DecodedPacketInfo packetInfo = NetContext.getPacketService().read(tmpByteBuf);
            ReferenceCountUtil.release(tmpByteBuf);
            out.add(packetInfo);
        } catch (Exception e) {
            logger.error("[session:{}]解码exception异常", SessionUtils.sessionInfo(ctx), e);
            throw e;
        } catch (Throwable t) {
            logger.error("[session:{}]解码throwable错误", SessionUtils.sessionInfo(ctx), t);
            throw t;
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EncodedPacketInfo packetInfo, ByteBuf out) {
        try {
            NetContext.getPacketService().write(out, packetInfo.getPacket(), packetInfo.getPacketAttachment());
        } catch (Exception e) {
            logger.error("[session:{}][{}]编码exception异常", SessionUtils.sessionInfo(ctx), packetInfo.getPacket().getClass().getSimpleName(), e);
            throw e;
        } catch (Throwable t) {
            logger.error("[session:{}][{}]编码throwable错误", SessionUtils.sessionInfo(ctx), packetInfo.getPacket().getClass().getSimpleName(), t);
            throw t;
        }
    }

}
