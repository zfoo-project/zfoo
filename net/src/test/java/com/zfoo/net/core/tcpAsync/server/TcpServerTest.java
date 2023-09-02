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
 * @version 3.0
 */
@Ignore
public class TcpServerTest {

    @Test
    public void startServer() {
        // 启动流程笔记：
        // 首先，这里是xml配置，这样子就会解析xml配置中的标签
        // 当遇到自定义标签后，就会触发NamespaceHandler 和 NetDefinitionParser 的自定义标签解析流程
        // 接着就会往Spring容器注册相关的各种bean
        var context = new ClassPathXmlApplicationContext("config.xml");

        // 打印下当前spring管理的bean信息
        // 可以得出结论：通过解析自定义标签，往spring容器中自己主动注册的几个
        // 和网络有关的全类名的bean(NetConfig、NetContext、ConfigManager、PacketService、Router、Consumer、SessionManager)
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }

        // 启动起来一个Tcp服务器
        // 注意：由于之前往Spring容器中注册的SessionManager，从而可以使用这个bean变量管理所有的连接信息
        // 这也是spring的好处：很多东西是解耦的，让SessionManager的初始化交给的是spring，而不是自己从main函数开始一个个组件去初始化
        var server = new TcpServer(HostAndPort.valueOf("127.0.0.1:9000"));
        server.start();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }
}

/*
serverPacketController
gatewayProviderController
JProtobufTcpClientController
JProtobufTcpController
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
