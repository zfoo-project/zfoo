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

import com.zfoo.net.core.proxy.TunnelServer;
import com.zfoo.net.handler.BaseRouteHandler;
import com.zfoo.net.handler.ServerRouteHandler;
import com.zfoo.net.util.SessionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaysunxiao
 */
@ChannelHandler.Sharable
public class TunnelServerRouteHandler extends BaseRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(TunnelServerRouteHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        logger.info("tunnel server channel is active {}", SessionUtils.sessionInfo(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        TunnelServer.tunnels.remove(ctx.channel());
        logger.warn("tunnel server channel is inactive {}", SessionUtils.sessionSimpleInfo(ctx));
    }
}
