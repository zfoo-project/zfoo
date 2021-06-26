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
import com.zfoo.net.handler.ClientDispatcherHandler;
import com.zfoo.net.handler.codec.tcp.TcpPacketCodecHandler;
import com.zfoo.net.handler.idle.ClientIdleHandler;
import com.zfoo.util.net.HostAndPort;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class TcpClient extends AbstractClient {

    public TcpClient(HostAndPort host) {
        super(host);
    }

    @Override
    public ChannelInitializer<? extends Channel> channelChannelInitializer() {
        return new TcpChannelInitHandler();
    }


    private static class TcpChannelInitHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) {
            channel.pipeline().addLast(new IdleStateHandler(0, 0, 60));
            channel.pipeline().addLast(new ClientIdleHandler());
            channel.pipeline().addLast(new TcpPacketCodecHandler());
            channel.pipeline().addLast(new ClientDispatcherHandler());
        }
    }


}
