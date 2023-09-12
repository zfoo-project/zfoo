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

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.net.core.event.ServerSessionActiveEvent;
import com.zfoo.net.core.event.ServerSessionInactiveEvent;
import com.zfoo.net.util.SessionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godotg
 */
@ChannelHandler.Sharable
public class ServerRouteHandler extends BaseRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerRouteHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        var session = initChannel(ctx.channel());
        NetContext.getSessionManager().addServerSession(session);
        logger.info("server channel is active {}", SessionUtils.sessionInfo(ctx));
        EventBus.post(ServerSessionActiveEvent.valueOf(session));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return;
        }
        NetContext.getSessionManager().removeServerSession(session);
        logger.warn("server channel is inactive {}", SessionUtils.sessionSimpleInfo(ctx));
        EventBus.post(ServerSessionInactiveEvent.valueOf(session));
    }
}
