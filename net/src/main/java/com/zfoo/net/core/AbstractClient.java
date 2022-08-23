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

package com.zfoo.net.core;

import com.zfoo.net.NetContext;
import com.zfoo.net.handler.BaseRouteHandler;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.util.ThreadUtils;
import com.zfoo.util.net.HostAndPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class AbstractClient implements IClient {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);

    protected static final EventLoopGroup nioEventLoopGroup = Epoll.isAvailable()
            ? new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, new DefaultThreadFactory("netty-client", true))
            : new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, new DefaultThreadFactory("netty-client", true));

    protected String hostAddress;
    protected int port;

    protected Bootstrap bootstrap;

    public AbstractClient(HostAndPort host) {
        this.hostAddress = host.getHost();
        this.port = host.getPort();
    }

    public abstract ChannelInitializer<? extends Channel> channelChannelInitializer();

    @Override
    public synchronized Session start() {
        return doStart(channelChannelInitializer());
    }

    private synchronized Session doStart(ChannelInitializer<? extends Channel> channelChannelInitializer) {
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(nioEventLoopGroup)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(16 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_MB))
                .handler(channelChannelInitializer());
        var channelFuture = bootstrap.connect(hostAddress, port);
        channelFuture.syncUninterruptibly();

        if (channelFuture.isSuccess()) {
            if (channelFuture.channel().isActive()) {
                var channel = channelFuture.channel();
                var session = BaseRouteHandler.initChannel(channel);
                NetContext.getSessionManager().addClientSession(session);
                logger.info("TcpClient started at [{}]", channel.localAddress());
                return session;
            }
        } else if (channelFuture.cause() != null) {
            logger.error(ExceptionUtils.getMessage(channelFuture.cause()));
        } else {
            logger.error("启动客户端[client:{}]未知错误", this);
        }
        return null;
    }


    public synchronized static void shutdown() {
        ThreadUtils.shutdownEventLoopGracefully("netty-client", nioEventLoopGroup);
    }

}
