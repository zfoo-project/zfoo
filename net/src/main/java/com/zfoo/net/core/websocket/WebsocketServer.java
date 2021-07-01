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

package com.zfoo.net.core.websocket;

import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.handler.ServerDispatcherHandler;
import com.zfoo.net.handler.codec.websocket.WebSocketCodecHandler;
import com.zfoo.util.net.HostAndPort;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class WebsocketServer extends AbstractServer {

    public WebsocketServer(HostAndPort host) {
        super(host);
    }

    @Override
    public ChannelInitializer<SocketChannel> channelChannelInitializer() {
        return new ChannelHandlerInitializer();
    }


    public static class ChannelHandlerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel channel) {
            ChannelPipeline pipeline = channel.pipeline();
            // 编解码 http 请求
            pipeline.addLast(new HttpServerCodec());
            // 写文件内容，支持异步发送大的码流，一般用于发送文件流
            pipeline.addLast(new ChunkedWriteHandler());
            // 聚合解码 HttpRequest/HttpContent/LastHttpContent 到 FullHttpRequest
            // 保证接收的 Http 请求的完整性
            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
            // 处理其他的 WebSocketFrame
            pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));
            // 编解码WebSocketFrame二进制协议
            pipeline.addLast(new WebSocketCodecHandler());
            pipeline.addLast(new ServerDispatcherHandler());
        }
    }

}
