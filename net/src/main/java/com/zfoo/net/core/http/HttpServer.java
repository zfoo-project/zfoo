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
 *
 */

package com.zfoo.net.core.http;

import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.handler.ServerDispatcherHandler;
import com.zfoo.net.handler.codec.http.HttpCodecHandler;
import com.zfoo.protocol.IPacket;
import com.zfoo.util.net.HostAndPort;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.function.Function;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class HttpServer extends AbstractServer {

    /**
     * http的地址解析器
     */
    private Function<String, IPacket> uriResolver;

    public HttpServer(HostAndPort host, Function<String, IPacket> uriResolver) {
        super(host);
        this.uriResolver = uriResolver;
    }

    @Override
    public ChannelInitializer<SocketChannel> channelChannelInitializer() {
        return new ChannelHandlerInitializer();
    }


    private class ChannelHandlerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) {
            channel.pipeline().addLast(new HttpServerCodec());
            channel.pipeline().addLast(new ChunkedWriteHandler());
            channel.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
            channel.pipeline().addLast(new HttpCodecHandler(uriResolver));
            channel.pipeline().addLast(new ServerDispatcherHandler());
        }
    }
}
