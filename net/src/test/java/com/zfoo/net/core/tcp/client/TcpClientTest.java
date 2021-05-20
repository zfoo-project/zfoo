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
import com.zfoo.net.packet.*;
import com.zfoo.net.session.SessionUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.ThreadUtils;
import com.zfoo.util.net.HostAndPort;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class TcpClientTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpClientTest.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Test
    public void startClientTest() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client_config.xml");
        SessionUtils.printSessionInfo();
        var client = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server0")));
        var session = client.start();

        new Thread(() -> {
            CM_Int cm = new CM_Int();
            cm.setFlag(false);
            cm.setA(Byte.MIN_VALUE);
            cm.setB(Short.MIN_VALUE);
            cm.setC(Integer.MIN_VALUE);
            cm.setD(Long.MIN_VALUE);
            cm.setE('e');
            cm.setF("Hello，this is the client!!!!!!!!!!!!!!!!!!!!!!!!!!!!");


            for (int i = 0; i < 1000; i++) {
                ThreadUtils.sleep(2000);
                NetContext.getDispatcher().send(session, cm);
            }
        }).start();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    @Test
    public void syncClientTest() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client_config.xml");

        SessionUtils.printSessionInfo();
        var client = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server1")));
        var session = client.start();

        for (int i = 0; i < 10000; i++) {
            int it = i;
            var thread = new Thread(() -> {
                try {
                    CM_SyncMess cm = new CM_SyncMess();
                    cm.setA("Hello, this is client!");
                    cm.setId(it);
                    SM_SyncMess sm = NetContext.getDispatcher().syncAsk(session, cm, SM_SyncMess.class, null).packet();
                    var info = StringUtils.MULTIPLE_HYPHENS + FileUtils.LS
                            + JsonUtils.object2String(cm) + FileUtils.LS
                            + JsonUtils.object2String(sm) + FileUtils.LS;
                    System.out.println(info);
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
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("client_config.xml");
        var client = new TcpClient(HostAndPort.valueOf(NetContext.getConfigManager().getLocalConfig().getHostConfig().getAddressMap().get("server0")));
        var session = client.start();

        for (int i = 0; i < 10000; i++) {
            Thread thread = new Thread(() -> {
                try {
                    CM_AsyncMess0 cm = new CM_AsyncMess0();
                    cm.setA("Hello, client0 -> server0!");

                    var asyncResponse = NetContext.getDispatcher().asyncAsk(session, cm, SM_AsyncMess0.class, null);
                    asyncResponse.whenComplete(sm -> {
                                var info = StringUtils.MULTIPLE_HYPHENS + FileUtils.LS
                                        + JsonUtils.object2String(cm) + FileUtils.LS
                                        + JsonUtils.object2String(sm) + FileUtils.LS;
                                System.out.println(info);
                            }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            executor.submit(thread);
        }
        SessionUtils.printSessionInfo();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
