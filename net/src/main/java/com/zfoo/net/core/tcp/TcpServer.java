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

package com.zfoo.net.core.tcp;

import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.handler.ServerRouteHandler;
import com.zfoo.net.handler.codec.tcp.TcpCodecHandler;
import com.zfoo.net.handler.idle.ServerIdleHandler;
import com.zfoo.util.net.HostAndPort;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * @author godotg
 * @version 3.0
 */
public class TcpServer extends AbstractServer<SocketChannel> {

    private final ServerRouteHandler routeHandler;

    public TcpServer(HostAndPort host) {
        this(host, null);
    }

    public TcpServer(HostAndPort host, @Nullable ServerRouteHandler serverRouteHandler) {
        super(host);
        this.routeHandler = Objects.requireNonNullElse(serverRouteHandler, new ServerRouteHandler());
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 180));
        channel.pipeline().addLast(new ServerIdleHandler());
        channel.pipeline().addLast(new TcpCodecHandler());
        channel.pipeline().addLast(routeHandler);
    }
}
