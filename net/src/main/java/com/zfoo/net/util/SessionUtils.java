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

import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import static com.zfoo.net.handler.BaseRouteHandler.SESSION_KEY;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class SessionUtils {

    private static final String CHANNEL_INFO_TEMPLATE = "[ip:{}][sid:{}][uid:{}]";


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
            return StringUtils.format(CHANNEL_INFO_TEMPLATE, ctx.channel());
        }
        return sessionInfo(session);
    }

    public static String sessionInfo(Session session) {
        if (session == null) {
            return CHANNEL_INFO_TEMPLATE;
        }
        var remoteAddress = session.getAttribute(AttributeType.CHANNEL_REMOTE_ADDRESS);
        return StringUtils.format(CHANNEL_INFO_TEMPLATE, remoteAddress, session.getSid(), session.getAttribute(AttributeType.UID));
    }

}
