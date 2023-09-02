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
 */
@Ignore
public class BenchmarkAsyncTest {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkAsyncTest.class);

    @Test
    public void benchmarkTest() throws Exception {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var client = new TcpClient(HostAndPort.valueOf("127.0.0.1:9000"));
        var session = client.start();

        // 异步请求消息是一起发送过去的，服务器排队处理消息容易导致超时，可以调高Router的DEFAULT_TIMEOUT的超时时间，现在默认是3秒超时
        var threadNums = Runtime.getRuntime().availableProcessors();
        var requestNums = 1000;
        for (int i = 0; i < threadNums; i++) {
            var thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < requestNums; i++) {
                        try {
                            var ask = new AsyncMessAsk();
                            ask.setMessage("Hello, this is async client!");
                            NetContext.getRouter().asyncAsk(session, ask, AsyncMessAnswer.class, null)
                                    .whenComplete(answer -> {
                                                logger.info("异步请求收到结果[{}]", JsonUtils.object2String(answer));
                                            }
                                    );
                        } catch (Exception e) {
                            logger.info("同步请求异常", e);
                        }
                    }
                }
            });
            thread.start();
        }
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
