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
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.handler.ServerRouteHandler;
import com.zfoo.net.handler.codec.websocket.WebSocketCodecHandler;
import com.zfoo.net.handler.idle.ServerIdleHandler;
import com.zfoo.protocol.util.IOUtils;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author godotg
 */
public class WebsocketServer extends AbstractServer<SocketChannel> {

    public WebsocketServer(HostAndPort host) {
        super(host);
    }

    @Override
    public void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 180));
        channel.pipeline().addLast(new ServerIdleHandler());
        // Encode/decode HTTP requests
        channel.pipeline().addLast(new HttpServerCodec(8 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_KB));
        // Aggregate HttpRequest/HttpContent/LastHttpContent into FullHttpRequest to ensure request completeness
        channel.pipeline().addLast(new HttpObjectAggregator(16 * IOUtils.BYTES_PER_MB));
        // Handle WebSocketFrame messages
        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/websocket"));
        // Write file content; supports async sending of large byte streams (e.g., file transfers)
        channel.pipeline().addLast(new ChunkedWriteHandler());
        // Encode/decode WebSocketFrame binary protocol
        channel.pipeline().addLast(new WebSocketCodecHandler());
        channel.pipeline().addLast(new ServerRouteHandler());
    }

}
