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

import com.zfoo.net.NetContext;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.core.tcp.TcpServer;
import com.zfoo.net.session.SessionUtils;
import com.zfoo.util.ThreadUtils;
import com.zfoo.util.net.HostAndPort;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.Executors;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class TcpServerTest {

    /**
     * 单机服务器教程，启动成功过后在com.zfoo.net.core.tcp.client.TcpClientTest中运行startClientTest
     * <p>
     * startClientTest连接服务器成功过后，会不断的发消息给服务器
     */
    @Test
    public void startServer0() {
        var context = new ClassPathXmlApplicationContext("config.xml");
        SessionUtils.printSessionInfo();

        var server0 = new TcpServer(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server0")));
        server0.start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startServer1() {
        var context = new ClassPathXmlApplicationContext("config.xml");

        SessionUtils.printSessionInfo();

        var server1 = new TcpServer(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server1")));
        server1.start();

        // 连接server0
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.execute(() -> {
            while (true) {
                try {
                    var client0 = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("client0")));
                    client0.start();
                    break;
                } catch (Exception e) {
                    System.out.println("连接失败，开始重新连接");
                    ThreadUtils.sleep(3000);
                    e.printStackTrace();
                }
            }
        });

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


}
