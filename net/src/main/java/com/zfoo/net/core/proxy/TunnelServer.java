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

package com.zfoo.net.core.proxy;

import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.proxy.handler.TunnelServerCodecHandler;
import com.zfoo.net.core.proxy.handler.TunnelServerRouteHandler;
import com.zfoo.net.handler.idle.ServerIdleHandler;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jaysunxiao
 */
public class TunnelServer extends AbstractServer<SocketChannel> {

    public static final CopyOnWriteArrayList<Channel> tunnels = new CopyOnWriteArrayList<>();

    public TunnelServer(HostAndPort host) {
        super(host);
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 32));
        channel.pipeline().addLast(new ServerIdleHandler());
        channel.pipeline().addLast(new TunnelServerCodecHandler());
        channel.pipeline().addLast(new TunnelServerRouteHandler());
    }
}
