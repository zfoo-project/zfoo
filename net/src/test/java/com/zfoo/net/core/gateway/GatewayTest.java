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

package com.zfoo.net.core.gateway;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.packet.gateway.CM_GatewayProvider;
import com.zfoo.net.packet.gateway.SM_GatewayProvider;
import com.zfoo.net.session.SessionUtils;
import com.zfoo.protocol.util.JsonUtils;
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
public class GatewayTest {

    /**
     * 启动zookeeper，依次运行下面的测试方法启动服务提供者，网关，然后运行clientTest，消息会通过网关转发到服务提供者
     */
    @Test
    public void startProvider0() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtils.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startProvider1() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtils.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startProvider2() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtils.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startGateway() {
        var context = new ClassPathXmlApplicationContext("gateway/gateway_consistent_session_config.xml");
        SessionUtils.printSessionInfo();
        var gatewayServer = new GatewayServer(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server0")), null);
        gatewayServer.start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void clientTest() {
        var context = new ClassPathXmlApplicationContext("gateway/gateway_client_config.xml");
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        SessionUtils.printSessionInfo();
        var client = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server0")));
        var session = client.start();

        for (int i = 0; i <= 100; i++) {
            int finalI = i;
            var thread = new Thread(() -> {
                var cm = new CM_GatewayProvider();
                cm.setA(NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString());
                cm.setB(finalI);
                try {
                    System.out.println("客户端发送消息：" + JsonUtils.object2String(cm));
                    var sm = NetContext.getDispatcher().syncAsk(session, cm, SM_GatewayProvider.class, null).packet();
                    System.out.println("客户端收到消息：" + JsonUtils.object2String(sm));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executor.execute(thread);
        }
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
