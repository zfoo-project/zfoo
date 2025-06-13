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

package com.zfoo.net.core.proxy.client;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.packet.proxy.ProxyHelloRequest;
import com.zfoo.net.packet.proxy.ProxyHelloResponse;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.function.Consumer;

/**
 * @author jaysunxiao
 */
@Ignore
public class ProxyClientTest {

    private static final Logger logger = LoggerFactory.getLogger(ProxyClientTest.class);

    @Test
    public void startClient() throws Exception {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var client = new TcpClient(HostAndPort.valueOf("127.0.0.1:9000"));
        var session = client.start();

        var request = ProxyHelloRequest.valueOf("Hello, this is the tcp client!");

        for (int i = 0; i < 1000; i++) {
            NetContext.getRouter().send(session, request);

            var response = NetContext.getRouter().syncAsk(session, request, ProxyHelloResponse.class, null).packet();
            logger.info("sync client receive [packet:{}] from server", JsonUtils.object2String(response));

            NetContext.getRouter().asyncAsk(session, request, ProxyHelloResponse.class, null)
                    .whenComplete(new Consumer<ProxyHelloResponse>() {
                        @Override
                        public void accept(ProxyHelloResponse jsonHelloResponse) {
                            logger.info("async client receive [packet:{}] from server", JsonUtils.object2String(jsonHelloResponse));
                        }
                    });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
