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

package com.zfoo.net.core.json.client;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.json.JsonWebsocketClient;
import com.zfoo.net.packet.json.JsonHelloRequest;
import com.zfoo.net.packet.json.JsonHelloResponse;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.function.Consumer;

/**
 * @author godotg
 */
@Ignore
public class JsonWebsocketClientTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonWebsocketClientTest.class);

    @Test
    public void startClient() throws Exception {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var webSocketClientProtocolConfig = WebSocketClientProtocolConfig.newBuilder()
                .webSocketUri("http://127.0.0.1:9000/websocket")
                .build();

        var client = new JsonWebsocketClient(HostAndPort.valueOf("127.0.0.1:9000"), webSocketClientProtocolConfig);
        var session = client.start();

        // Websocket在建立tcp连接过后不能立刻通讯，还需要使用Http协议去握手升级成Websocket协议，这里等待一秒让websocket内部协议初始化完毕
        // WebSocket握手是在客户端和服务器之间建立WebSocket连接的过程。它是通过HTTP/HTTPS协议完成的，后续将升级为WebSocket协议。
        ThreadUtils.sleep(1000);

        var request = new JsonHelloRequest();
        request.setMessage("Hello, this is the json client!");

        for (int i = 0; i < 1000; i++) {
            NetContext.getRouter().send(session, request);

            var response = NetContext.getRouter().syncAsk(session, request, JsonHelloResponse.class, null).packet();
            logger.info("sync json client receive [packet:{}] from server", JsonUtils.object2String(response));

            NetContext.getRouter().asyncAsk(session, request, JsonHelloResponse.class, null)
                    .whenComplete(new Consumer<JsonHelloResponse>() {
                        @Override
                        public void accept(JsonHelloResponse jsonHelloResponse) {
                            logger.info("async json client receive [packet:{}] from server", JsonUtils.object2String(jsonHelloResponse));
                        }
                    });
            ThreadUtils.sleep(1000);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


}
