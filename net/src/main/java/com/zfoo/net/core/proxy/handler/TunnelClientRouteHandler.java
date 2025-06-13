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
import com.zfoo.net.core.proxy.TunnelClient;
import com.zfoo.net.core.proxy.TunnelProtocolClient2Server;
import com.zfoo.net.core.proxy.TunnelProtocolServer2Client;
import com.zfoo.net.handler.ClientRouteHandler;
import com.zfoo.net.session.Session;
import com.zfoo.net.util.SessionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author jaysunxiao
 */
@ChannelHandler.Sharable
public class TunnelClientRouteHandler extends ClientRouteHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        TunnelClient.tunnels.add(ctx.channel());
        var session = SessionUtils.getSession(ctx);
        ctx.channel().writeAndFlush(new TunnelProtocolClient2Server.TunnelRegister(session.getSid()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        TunnelClient.tunnels.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        var tunnelPacketInfo = (TunnelProtocolServer2Client.TunnelPacketInfo) msg;
        var session = new Session(tunnelPacketInfo.sid, tunnelPacketInfo.uid, ctx.channel());
        NetContext.getRouter().receive(session, tunnelPacketInfo.packet, tunnelPacketInfo.attachment);
    }

}
