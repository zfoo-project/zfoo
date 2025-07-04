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

package com.zfoo.net.core.udp;

import com.zfoo.net.NetContext;
import com.zfoo.net.handler.BaseRouteHandler;
import com.zfoo.net.packet.DecodedPacketInfo;
import com.zfoo.net.util.SessionUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author godotg
 */
@ChannelHandler.Sharable
public class UdpRouteHandler extends BaseRouteHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            session = initChannel(ctx.channel());
        }
        DecodedPacketInfo decodedPacketInfo = (DecodedPacketInfo) msg;
        NetContext.getRouter().receive(session, decodedPacketInfo.getPacket(), decodedPacketInfo.getAttachment());
    }
}
