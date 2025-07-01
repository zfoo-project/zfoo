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

package com.zfoo.net.core.proxy.server;

import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.proxy.ProxyTcpServer;
import com.zfoo.net.core.proxy.TunnelServer;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author jaysunxiao
 */
@Ignore
public class ProxyServerTest {

    /**
     * ReverseProxyServerTest reverse proxy TargetServerTest
     *
     * ProxyServer 代理了 TargetServer
     */
    @Test
    public void startServer() {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var proxyServer = new ProxyTcpServer(HostAndPort.valueOf("0.0.0.0:9000"));
        proxyServer.start();

        var tunnelServer = new TunnelServer(HostAndPort.valueOf("0.0.0.0:9001"));
        tunnelServer.start();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
