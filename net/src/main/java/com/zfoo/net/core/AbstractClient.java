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

import com.zfoo.net.session.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.util.TimeUtils;
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

import java.sql.SQLOutput;

/**
 * @author godotg
 */
public abstract class AbstractClient<C extends Channel> extends ChannelInitializer<C> implements IClient {

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

    @Override
    public synchronized Session start() {
        return doStart();
    }

    private synchronized Session doStart() {
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(nioEventLoopGroup)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(16 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_MB))
                .handler(this);
        var channelFuture = bootstrap.connect(hostAddress, port);
        channelFuture.syncUninterruptibly();

        if (channelFuture.cause() != null) {
            throw new RuntimeException(channelFuture.cause());
        }

        if (!channelFuture.isSuccess() || !channelFuture.channel().isActive()) {
            throw new RunException("[{}] started failed", this.getClass().getSimpleName());
        }

        var channel = channelFuture.channel();
        var session = SessionUtils.getSession(channel);
        var loop = 128;
        var sleepMillisSeconds = 100;
        for (int i = 0; i < loop; i++) {
            ThreadUtils.sleep(sleepMillisSeconds);
            session = SessionUtils.getSession(channel);
            if (session != null) {
                break;
            }
        }
        if (session == null) {
            channel.close();
            throw new RunException("[{}] start client timeout after [{}] seconds", this.getClass().getSimpleName(), loop * sleepMillisSeconds / TimeUtils.MILLIS_PER_SECOND);
        }
        logger.info("{} started at [{}]", this.getClass().getSimpleName(), channel.localAddress());
        return session;
    }


    public synchronized static void shutdown() {
        ThreadUtils.shutdownEventLoopGracefully("netty-client", nioEventLoopGroup);
    }

}
