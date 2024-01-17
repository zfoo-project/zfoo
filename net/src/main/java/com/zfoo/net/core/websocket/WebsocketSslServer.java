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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLException;
import java.io.InputStream;

/**
 * @author godotg
 */
public class WebsocketSslServer extends AbstractServer<SocketChannel> {

    private SslContext sslContext;

    /**
     * 阿里云证书转换：
     * 进入阿里云证书下载页面，要下载两份，一个是nginx的，一个是tomcat的。
     * 1.下载的nginx格式的证书，里面包含pem和key两种格式的文件。其中pem格式我们可以直接用，key格式的Java无法直接使用。
     * <p>
     * 2.下载的tomcat格式的证书，我们使用OpenSSL将前面下载tomcat格式证书pfx文件转换一个可以使用的key文件出来:
     * openssl pkcs12 -in xxx.pfx -nocerts -nodes -out new_xxx.key
     * openssl会让输入密码，密码是tomcat下载文件里的pfx-password.txt
     * Linux系统一般自带openssl工具，把证书上传到一台Linux服务器去转换一下就可以了。
     * <p>
     * 3.第一步获得的pem证书和第二步获得的key证书就是下面这个构造方法要使用的
     */
    public WebsocketSslServer(HostAndPort host, InputStream pem, InputStream key) {
        super(host);
        try {
            this.sslContext = SslContextBuilder.forServer(pem, key).build();
        } catch (SSLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 180));
        channel.pipeline().addLast(new ServerIdleHandler());
        channel.pipeline().addLast(sslContext.newHandler(channel.alloc()));
        channel.pipeline().addLast(new HttpServerCodec(8 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_KB, 16 * IOUtils.BYTES_PER_KB));
        channel.pipeline().addLast(new HttpObjectAggregator(16 * IOUtils.BYTES_PER_MB));
        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
        channel.pipeline().addLast(new ChunkedWriteHandler());
        channel.pipeline().addLast(new WebSocketCodecHandler());
        channel.pipeline().addLast(new ServerRouteHandler());
    }

}
