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

import com.zfoo.net.core.AbstractClient;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.handler.ClientRouteHandler;
import com.zfoo.net.handler.codec.tcp.TcpCodecHandler;
import com.zfoo.net.handler.idle.ClientIdleHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author godotg
 */
public class TcpClient extends AbstractClient<SocketChannel> {
    public TcpClient(HostAndPort host) {
        super(host);
    }

    @Override
    protected void initChannel(SocketChannel channel) {
                // The client's idle detection timeout is 60 s (shorter), allowing it to send heartbeats.
                // The server's idle detection timeout is 180 s (longer); once idle is detected, the client is evicted.
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 60));
        channel.pipeline().addLast(new ClientIdleHandler());
        channel.pipeline().addLast(new TcpCodecHandler());
        channel.pipeline().addLast(new ClientRouteHandler());
    }
}
