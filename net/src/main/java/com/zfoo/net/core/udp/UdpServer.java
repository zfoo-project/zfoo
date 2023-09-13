/*
 * Copyright (C) 2020 The zfoo Authors
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

import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.handler.ServerRouteHandler;
import com.zfoo.net.handler.codec.udp.UdpCodecHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godotg
 */
public class UdpServer extends AbstractServer<Channel> {
    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    public UdpServer(HostAndPort host) {
        super(host);
    }

    @Override
    public void start() {
        var cpuNum = Runtime.getRuntime().availableProcessors();

        // 配置服务端nio线程组
        workerGroup = Epoll.isAvailable()
                ? new EpollEventLoopGroup(cpuNum * 2, new DefaultThreadFactory("netty-worker", true))
                : new NioEventLoopGroup(cpuNum * 2, new DefaultThreadFactory("netty-worker", true));

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(this);

        // 异步
        channelFuture = bootstrap.bind(hostAddress, port);
        channelFuture.syncUninterruptibly();

        allServers.add(this);

        logger.info("{} started at [{}:{}]", this.getClass().getSimpleName(), hostAddress, port);
    }

    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline().addLast(new UdpCodecHandler());
        channel.pipeline().addLast(new ServerRouteHandler());
    }
}
