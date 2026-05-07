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

package com.zfoo.net.core.tcpAsync.server;

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

    @Test
    public void startServer() {
        // Startup sequence notes:
        // First, the XML configuration is loaded and the custom namespace tags are parsed
        // When a custom tag is encountered, NamespaceHandler and NetDefinitionParser handle the custom tag parsing
        // The parser then registers the required beans into the Spring container
        var context = new ClassPathXmlApplicationContext("config.xml");

        // Print the names of all beans currently managed by Spring
        // Conclusion: by parsing the custom XML tags, the following beans are self-registered:
        // Network-related beans: NetConfig, NetContext, ConfigManager, PacketService, Router, Consumer, SessionManager
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }

        // Start up a TCP server
        // Note: the SessionManager bean registered earlier manages all active connection sessions
        // This is the benefit of Spring: decoupled initialization - SessionManager lifecycle is managed by Spring
        var server = new TcpServer(HostAndPort.valueOf("127.0.0.1:9000"));
        server.start();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }
}

/*
serverPacketController
gatewayProviderController
providerController
tcpClientController
tcpServerController
tcpAsyncController
tcpSyncController
udpClientPacketController
udpServerPacketController
websocketClientPacketController
websocketServerPacketController
org.springframework.context.annotation.internalConfigurationAnnotationProcessor
org.springframework.context.annotation.internalAutowiredAnnotationProcessor
org.springframework.context.event.internalEventListenerProcessor
org.springframework.context.event.internalEventListenerFactory
com.zfoo.net.config.model.NetConfig
com.zfoo.net.NetContext
com.zfoo.net.config.ConfigManager
com.zfoo.net.packet.PacketService
com.zfoo.net.router.Router
com.zfoo.net.consumer.Consumer
com.zfoo.net.session.SessionManager
 */
