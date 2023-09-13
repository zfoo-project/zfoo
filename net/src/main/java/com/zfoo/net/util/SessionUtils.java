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

package com.zfoo.net.util;

import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import static com.zfoo.net.handler.BaseRouteHandler.SESSION_KEY;

/**
 * @author godotg
 */
public abstract class SessionUtils {

    private static final String CHANNEL_INFO_TEMPLATE = "[ip:{}][sid:{}][uid:{}]";

    private static final String CHANNEL_SIMPLE_INFO_TEMPLATE = "[sid:{}][uid:{}]";

    private static final String CHANNEL_TEMPLATE = "[channel:{}]";

    public static boolean isActive(Session session) {
        return session != null && session.getChannel().isActive();
    }

    public static boolean isActive(Channel session) {
        return session != null && session.isActive();
    }

    public static Session getSession(ChannelHandlerContext ctx) {
        var sessionAttr = ctx.channel().attr(SESSION_KEY);
        return sessionAttr.get();
    }

    public static String sessionInfo(ChannelHandlerContext ctx) {
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return StringUtils.format(CHANNEL_TEMPLATE, ctx.channel());
        }
        return sessionInfo(session);
    }

    public static String sessionInfo(Session session) {
        if (session == null) {
            return CHANNEL_INFO_TEMPLATE;
        }
        var remoteAddress = StringUtils.EMPTY;
        try {
            remoteAddress = StringUtils.substringAfterFirst(session.getChannel().remoteAddress().toString(), StringUtils.SLASH);
        } catch (Throwable t) {
            // do nothing
            // to avoid: io.netty.channel.unix.Errors$NativeIoException: readAddress(..) failed: Connection reset by peer
            // 有些情况当建立连接过后迅速关闭，这个时候取remoteAddress会有异常
        }
        return StringUtils.format(CHANNEL_INFO_TEMPLATE, remoteAddress, session.getSid(), session.getUid());
    }

    public static String sessionSimpleInfo(ChannelHandlerContext ctx) {
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return StringUtils.format(CHANNEL_TEMPLATE, ctx.channel());
        }
        return sessionSimpleInfo(session);
    }

    public static String sessionSimpleInfo(Session session) {
        if (session == null) {
            return CHANNEL_SIMPLE_INFO_TEMPLATE;
        }
        return StringUtils.format(CHANNEL_SIMPLE_INFO_TEMPLATE, session.getSid(), session.getUid());
    }

}
