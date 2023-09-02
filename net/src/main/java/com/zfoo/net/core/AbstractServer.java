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

import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class AbstractServer<C extends Channel> extends ChannelInitializer<C> implements IServer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);

    // 所有的服务器都可以在这个列表中取到
    protected static final List<AbstractServer<? extends Channel>> allServers = new ArrayList<>(1);

    protected String hostAddress;
    protected int port;


    // 配置服务端nio线程组，服务端接受客户端连接
    private EventLoopGroup bossGroup;

    // SocketChannel的网络读写
    protected EventLoopGroup workerGroup;

    protected ChannelFuture channelFuture;

    public AbstractServer(HostAndPort host) {
        this.hostAddress = host.getHost();
        this.port = host.getPort();
    }

    @Override
    public void start() {
        doStart();
    }

    protected synchronized void doStart() {
        var cpuNum = Runtime.getRuntime().availableProcessors();
        // 一条线程持有一个端口对应的selector，如果我们启动不仅仅是一个服务器端口的话，为了更好的性能需要修改对应的bossGroup数量
        bossGroup = Epoll.isAvailable()
                ? new EpollEventLoopGroup(Math.max(1, cpuNum / 8), new DefaultThreadFactory("netty-boss", true))
                : new NioEventLoopGroup(Math.max(1, cpuNum / 8), new DefaultThreadFactory("netty-boss", true));

        workerGroup = Epoll.isAvailable()
                ? new EpollEventLoopGroup(cpuNum * 2, new DefaultThreadFactory("netty-worker", true))
                : new NioEventLoopGroup(cpuNum * 2, new DefaultThreadFactory("netty-worker", true));

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(16 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_MB))
                .childHandler(this);
        // 绑定端口，同步等待成功
        // channelFuture = bootstrap.bind(hostAddress, port).sync();
        // 等待服务端监听端口关闭
        // channelFuture.channel().closeFuture().sync();


        // 异步
        channelFuture = bootstrap.bind(hostAddress, port);
        channelFuture.syncUninterruptibly();

        allServers.add(this);

        logger.info("{} started at [{}:{}]", this.getClass().getSimpleName(), hostAddress, port);
    }


    @Override
    public synchronized void shutdown() {
        ThreadUtils.shutdownEventLoopGracefully("netty-boss", bossGroup);
        ThreadUtils.shutdownEventLoopGracefully("netty-worker", workerGroup);

        if (channelFuture != null) {
            try {
                channelFuture.channel().close().syncUninterruptibly();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    public synchronized static void shutdownAllServers() {
        allServers.forEach(it -> it.shutdown());
    }

}
