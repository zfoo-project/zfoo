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
import com.zfoo.net.core.AbstractClient;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.handler.BaseRouteHandler;
import com.zfoo.net.handler.ClientRouteHandler;
import com.zfoo.net.handler.codec.udp.UdpCodecHandler;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.exception.ExceptionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author godotg
 */
public class UdpClient extends AbstractClient<Channel> {

    public UdpClient(HostAndPort host) {
        super(host);
    }

    @Override
    public synchronized Session start() {
        try {
            this.bootstrap = new Bootstrap();
            this.bootstrap.group(nioEventLoopGroup)
                    .channel(Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(this);

            // bind(0)随机选择一个端口
            var channelFuture = bootstrap.bind(0).sync();
            channelFuture.syncUninterruptibly();

            if (channelFuture.isSuccess()) {
                if (channelFuture.channel().isActive()) {
                    var channel = channelFuture.channel();
                    var session = BaseRouteHandler.initChannel(channel);
                    NetContext.getSessionManager().addClientSession(session);
                    logger.info("UdpClient started at [{}]", channel.localAddress());
                    return session;
                }
            } else if (channelFuture.cause() != null) {
                logger.error(ExceptionUtils.getMessage(channelFuture.cause()));
            } else {
                logger.error("[{}] started failed", this.getClass().getSimpleName());
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
        }
        return null;
    }

    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline().addLast(new UdpCodecHandler());
        channel.pipeline().addLast(new ClientRouteHandler());
    }
}
