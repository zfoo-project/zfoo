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

package com.zfoo.net.core.tcp.client;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.packet.tcp.*;
import com.zfoo.net.session.SessionUtils;
import com.zfoo.net.task.model.SafeRunnable;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.util.ThreadUtils;
import com.zfoo.util.net.HostAndPort;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class TcpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TcpClientTest.class);

    @Test
    public void startClient0() {
        var context = new ClassPathXmlApplicationContext("config.xml");
        SessionUtils.printSessionInfo();
        var client = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHost().getAddress().get("server0")));
        var session = client.start();

        var request = new TcpHelloRequest();
        request.setMessage("Hello, this is the tcp client!");


        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(2000);
            NetContext.getRouter().send(session, request);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    @Test
    public void syncClientTest() {
        var context = new ClassPathXmlApplicationContext("config.xml");
        SessionUtils.printSessionInfo();
        var client = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHost().getAddress().get("server1")));
        var session = client.start();

        var executorSize = Runtime.getRuntime().availableProcessors() * 2;
        var executor = Executors.newFixedThreadPool(executorSize);
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < executorSize; i++) {
            var thread = new Thread(() -> {
                try {
                    for (int j = 0; j < 10000; j++) {
                        var ask = new SyncMessAsk();
                        ask.setMessage("Hello, this is sync client!");
                        var answer = NetContext.getRouter().syncAsk(session, ask, SyncMessAnswer.class, null).packet();
                        logger.info("同步请求[{}]收到结果[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(answer));
                    }
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getMessage(e));
                }
            });
            executor.execute(thread);
        }
        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    @Test
    public void asyncClientTest() {
        var context = new ClassPathXmlApplicationContext("config.xml");
        var client1 = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHost().getAddress().get("server1")));
        var session1 = client1.start();

        var executorSize = Runtime.getRuntime().availableProcessors() * 2;
        var executor = Executors.newFixedThreadPool(executorSize);
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < executorSize; i++) {
            var thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    var ask = new AsyncMess0Ask();
                    ask.setMessage("Hello, client0 -> server0!");

                    NetContext.getRouter().asyncAsk(null, ask, AsyncMess0Answer.class, null)
                            .notComplete(new SafeRunnable() {
                                @Override
                                public void doRun() {
                                    logger.info("异步请求没有完成");
                                }
                            })
                            .whenComplete(answer -> {
                                        logger.info("异步请求[{}]收到结果[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(answer));
                                    }
                            );
                }
            });
            executor.execute(thread);
        }

        SessionUtils.printSessionInfo();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
