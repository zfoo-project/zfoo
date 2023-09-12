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

package com.zfoo.net.core.json;

import com.zfoo.net.core.AbstractClient;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.handler.ClientRouteHandler;
import com.zfoo.net.handler.codec.json.JsonWebSocketCodecHandler;
import com.zfoo.protocol.util.IOUtils;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author godotg
 */
public class JsonWebsocketClient extends AbstractClient<SocketChannel> {

    private WebSocketClientProtocolConfig webSocketClientProtocolConfig;

    public JsonWebsocketClient(HostAndPort host, WebSocketClientProtocolConfig webSocketClientProtocolConfig) {
        super(host);
        this.webSocketClientProtocolConfig = webSocketClientProtocolConfig;
    }

    @Override
    public void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(new HttpClientCodec(8 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_KB));
        channel.pipeline().addLast(new HttpObjectAggregator(16 * IOUtils.BYTES_PER_MB));
        channel.pipeline().addLast(new WebSocketClientProtocolHandler(webSocketClientProtocolConfig));
        channel.pipeline().addLast(new ChunkedWriteHandler());
        channel.pipeline().addLast(new JsonWebSocketCodecHandler());
        channel.pipeline().addLast(new ClientRouteHandler());
    }

}
