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
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.packet.gateway.GatewayToProviderRequest;
import com.zfoo.net.packet.gateway.GatewayToProviderResponse;
import com.zfoo.net.util.SessionUtilsTest;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test case: client connects to the gateway, which then forwards requests to the service provider
 *
 * @author godotg
 */
@Ignore
public class GatewayTest {

    private static final Logger logger = LoggerFactory.getLogger(GatewayTest.class);

    /**
     * Start ZooKeeper first, then run the test methods below in order:
     * 3. Run clientTest
     * <p>
     * Messages are forwarded through the gateway to the service provider.
    @Test
    public void startProvider0() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtilsTest.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startProvider1() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtilsTest.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startProvider2() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtilsTest.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * This is the gateway
     */
    @Test
    public void startGateway() {
        var context = new ClassPathXmlApplicationContext("gateway/gateway_config.xml");
        SessionUtilsTest.printSessionInfo();

        // Note: GatewayServer uses GatewayRouteHandler (not BaseRouteHandler); it forwards messages to the Provider via a ConsumerSession
        var gatewayServer = new GatewayServer(HostAndPort.valueOf("127.0.0.1:9000"), null);
        gatewayServer.start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * Client side: client first requests data from the gateway.
     */
    @Test
    public void clientSyncTest() {
        var context = new ClassPathXmlApplicationContext("gateway/gateway_client_config.xml");
        SessionUtilsTest.printSessionInfo();

        // The address here is the gateway address
        var client = new TcpClient(HostAndPort.valueOf("127.0.0.1:9000"));
        var session = client.start();

        var executorSize = Runtime.getRuntime().availableProcessors() * 2;
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        var request = new GatewayToProviderRequest();
        request.setMessage("Hello, this is the client!");
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < executorSize; i++) {
            var thread = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    try {
                        // Note: 2nd parameter must be xxxRequest (not xxxAsk); 3rd argument null => random provider.
                        var response = NetContext.getRouter().syncAsk(session, request, GatewayToProviderResponse.class, null).packet();
                        logger.info("client request[{}] received response[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(response));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            executor.execute(thread);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void clientAsyncTest() {
        var context = new ClassPathXmlApplicationContext("gateway/gateway_client_config.xml");
        SessionUtilsTest.printSessionInfo();

        // The address here is the gateway address
        var client = new TcpClient(HostAndPort.valueOf("127.0.0.1:9000"));
        var session = client.start();

        var executorSize = Runtime.getRuntime().availableProcessors() * 2;
        var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        var request = new GatewayToProviderRequest();
        request.setMessage("Hello, this is the client!");
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < executorSize; i++) {
            var thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    try {
                        // Note: 2nd parameter must be xxxRequest (not xxxAsk); 3rd argument null => random provider.
                        NetContext.getRouter().asyncAsk(session, request, GatewayToProviderResponse.class, null)
                                .whenComplete(response -> {
                                    logger.info("client request[{}] received response[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(response));
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            executor.execute(thread);
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }
}
