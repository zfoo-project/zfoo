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

package com.zfoo.net.core.tcpAsync.client;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.packet.tcp.AsyncMessAnswer;
import com.zfoo.net.packet.tcp.AsyncMessAsk;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class TcpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TcpClientTest.class);

    @Test
    public void startClient() throws Exception {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var client = new TcpClient(HostAndPort.valueOf("127.0.0.1:9000"));

        // 得到连接到服务器的session
        var session = client.start();

        for (int i = 0; i < 1000; i++) {
            var ask = new AsyncMessAsk();
            ask.setMessage("Hello, this is async client!");

            // Router只是一个接口，它的赋值也是在容器初始化好之后从Spring容器获取到的Router对象
            // 要发送网络数据，要通过方法asyncAsk,第一个参数就是要发送消息的session
            // 注意：第3个argument参数是当收到消息后，是要要到哪个线程处理,这是CompletableFuture保证的
            // 其实这个依然是通过session，然后调用send发送过去的消息
            // 最终依然是通过BaseRouteHandler得到响应，只不过attachment不为空，会触发CompletableFuture的completable方法，从而得到回调结果
            NetContext.getRouter().asyncAsk(session, ask, AsyncMessAnswer.class, null)
                    .whenComplete(answer -> {
                                logger.info("异步请求收到结果[{}]", JsonUtils.object2String(answer));
                            }
                    );

            ThreadUtils.sleep(1000);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
