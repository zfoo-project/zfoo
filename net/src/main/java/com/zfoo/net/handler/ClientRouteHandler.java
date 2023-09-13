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
import com.zfoo.net.core.event.ClientSessionActiveEvent;
import com.zfoo.net.core.event.ClientSessionInactiveEvent;
import com.zfoo.net.util.SessionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godotg
 */
@ChannelHandler.Sharable
public class ClientRouteHandler extends BaseRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientRouteHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 客户端的session初始化在启动的时候已经做了，这边直接获取session
        var session = SessionUtils.getSession(ctx);
        EventBus.post(ClientSessionActiveEvent.valueOf(session));
        logger.info("client channel is active {}", SessionUtils.sessionInfo(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        var session = SessionUtils.getSession(ctx);

        if (session == null) {
            return;
        }

        NetContext.getSessionManager().removeClientSession(session);
        EventBus.post(ClientSessionInactiveEvent.valueOf(session));

        // 如果是消费者inactive，还需要触发客户端消费者检查事件，以便重新连接
        if (session.getConsumerAttribute() != null) {
            NetContext.getConfigManager().getRegistry().checkConsumer();
        }

        logger.warn("client channel is inactive {}", SessionUtils.sessionSimpleInfo(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("client session exception caught {}", SessionUtils.sessionSimpleInfo(ctx), cause);
    }

}
