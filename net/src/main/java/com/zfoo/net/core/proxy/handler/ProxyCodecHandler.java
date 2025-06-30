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

package com.zfoo.net.core.proxy.handler;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.proxy.TunnelProtocolServer2Client;
import com.zfoo.net.core.proxy.TunnelServer;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author jaysunxiao
 */
public class ProxyCodecHandler extends ByteToMessageCodec<TunnelProtocolServer2Client> {

    private static final Logger logger = LoggerFactory.getLogger(ProxyCodecHandler.class);


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
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

        if (CollectionUtils.isEmpty(TunnelServer.tunnels)) {
            in.readSlice(length);
            logger.warn("Tunnel server has no tunnels");
            return;
        }

        var tunnel = RandomUtils.randomEle(TunnelServer.tunnels);
        if (!SessionUtils.isActive(tunnel)) {
            in.readSlice(length);
            logger.warn("Tunnel server has no active tunnels");
            return;
        }

        if (!tunnel.isWritable()) {
            in.readSlice(length);
            logger.warn("Tunnel server has no writable tunnels");
            return;
        }

        var retainedByteBuf = in.readRetainedSlice(length);
        try {
            var session = SessionUtils.getSession(ctx);
            tunnel.writeAndFlush(TunnelProtocolServer2Client.valueOf(session.getSid(), session.getUid(), retainedByteBuf));
        } catch (Throwable t) {
            ReferenceCountUtil.release(retainedByteBuf);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, TunnelProtocolServer2Client tunnelProtocol, ByteBuf out) {
        try {
            out.ensureWritable(7);
            out.writerIndex(PacketService.PACKET_HEAD_LENGTH);
            out.writeBytes(tunnelProtocol.getRetainedByteBuf());
            NetContext.getPacketService().writeHeaderBefore(out);
        } finally {
            ReferenceCountUtil.release(tunnelProtocol.getRetainedByteBuf());
        }
    }

}
