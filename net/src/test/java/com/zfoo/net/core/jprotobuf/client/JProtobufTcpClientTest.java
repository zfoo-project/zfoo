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

package com.zfoo.net.core.jprotobuf.client;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.jprotobuf.JProtobufTcpClient;
import com.zfoo.net.packet.jprotobuf.JProtobufHelloRequest;
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
public class JProtobufTcpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(JProtobufTcpClientTest.class);

    @Test
    public void startClient() throws Exception {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var client = new JProtobufTcpClient(HostAndPort.valueOf("127.0.0.1:9000"));
        var session = client.start();

        for (int i = 0; i < 1000; i++) {
            var ask = new JProtobufHelloRequest();
            ask.setMessage("Hello, this is jprotobuf client!");
            NetContext.getRouter().send(session, ask);
            ThreadUtils.sleep(1000);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
