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

package com.zfoo.net.core.tcp.server;

import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.tcp.TcpServer;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author godotg
 */
@Ignore
public class TcpServerTest {

    /**
     * 单机服务器教程，启动成功过后在com.zfoo.net.core.tcp.client.TcpClientTest中运行startClientTest
     * <p>
     * startClientTest连接服务器成功过后，会不断的发消息给服务器
     */
    @Test
    public void startServer() {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var server = new TcpServer(HostAndPort.valueOf("127.0.0.1:9000"));
        server.start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
