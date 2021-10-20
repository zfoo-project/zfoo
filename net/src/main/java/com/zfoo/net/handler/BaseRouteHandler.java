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

package com.zfoo.net.handler;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.session.model.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@ChannelHandler.Sharable
public class BaseRouteHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BaseRouteHandler.class);

    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");

    public static Session initChannel(Channel channel) {
        var sessionAttr = channel.attr(SESSION_KEY);
        var session = new Session(channel);
        var setSuccessful = sessionAttr.compareAndSet(null, session);
        if (!setSuccessful) {
            channel.close();
            throw new RuntimeException(StringUtils.format("无法设置[channel:{}]的session", channel));
        }

        try {
            session.putAttribute(AttributeType.CHANNEL_REMOTE_ADDRESS, StringUtils.substringAfterFirst(channel.remoteAddress().toString(), StringUtils.SLASH));
        } catch (Throwable t) {
            // do nothing
            // to avoid: io.netty.channel.unix.Errors$NativeIoException: readAddress(..) failed: Connection reset by peer
            // 有些情况当建立连接过后迅速关闭，这个时候取remoteAddress会有异常
        }

        return session;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return;
        }
        DecodedPacketInfo decodedPacketInfo = (DecodedPacketInfo) msg;
        NetContext.getRouter().receive(session, decodedPacketInfo.getPacket(), decodedPacketInfo.getAttachment());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            logger.error("[session{}]未知异常", SessionUtils.sessionInfo(ctx), cause);
        } finally {
            ctx.close();
        }
    }

}
