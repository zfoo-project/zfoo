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

package com.zfoo.net.handler.codec.websocket;

import com.zfoo.net.NetContext;
import com.zfoo.net.handler.codec.tcp.TcpPacketCodecHandler;
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.packet.model.EncodedPacketInfo;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
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
public class WebSocketCodecHandler extends MessageToMessageCodec<WebSocketFrame, EncodedPacketInfo> {

    private static final Logger logger = LoggerFactory.getLogger(TcpPacketCodecHandler.class);

    // 数据包的最大长度限制，防止恶意的攻击
    private static final int MAX_LENGTH = 100 * IOUtils.BYTES_PER_KB;

    private int length;
    private boolean remain = false;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame, List<Object> list) {
        try {
            ByteBuf in = webSocketFrame.content();

            if (!remain) {
                // 不够读一个int
                if (in.readableBytes() <= ProtocolManager.PROTOCOL_HEAD_LENGTH) {
                    return;
                }
                length = in.readInt();
                remain = true;
            }

            // 如果长度超过限制，则抛出异常断开连接
            if (length > MAX_LENGTH) {
                throw new IllegalArgumentException(StringUtils
                        .format("[session:{}]的包头长度[length:{}]超过最大长度[maxLength:{}]限制"
                                , SessionUtils.sessionInfo(channelHandlerContext), length, MAX_LENGTH));
            }

            // ByteBuf里的数据太小
            if (in.readableBytes() < length) {
                return;
            }

            remain = false;

            DecodedPacketInfo packetInfo = NetContext.getPacketService().read(in);

            list.add(packetInfo);
        } catch (Exception e) {
            logger.error("exception异常", e);
            throw e;
        } catch (Throwable t) {
            logger.error("throwable错误", t);
            throw t;
        }
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, EncodedPacketInfo out, List<Object> list) {
        try {
            ByteBuf byteBuf = Unpooled.directBuffer();
            byteBuf.clear();

            NetContext.getPacketService().write(byteBuf, out.getPacket(), out.getPacketAttachment());
            list.add(new BinaryWebSocketFrame(byteBuf));
        } catch (Exception e) {
            logger.error("[{}]编码exception异常", JsonUtils.object2String(out), e);
            throw e;
        } catch (Throwable t) {
            logger.error("[{}]编码throwable错误", JsonUtils.object2String(out), t);
            throw t;
        }
    }


}
