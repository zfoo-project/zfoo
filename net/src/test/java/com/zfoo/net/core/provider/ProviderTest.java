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

package com.zfoo.net.core.provider;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.provider.CM_Provider;
import com.zfoo.net.packet.provider.SM_Provider;
import com.zfoo.net.session.SessionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class ProviderTest {

    /**
     * RPC教程：
     * 1.首先必须保证启动zookeeper
     * 2.启动服务提供者，startProvider0，startProvider1，startProvider2
     * 3.启动服务消费者，startSyncRandomConsumer，startAsyncRandomConsumer，startConsistentSessionConsumer，startShortestTimeConsumer
     * 4.每个消费者都是通过不同的策略消费，注意区别
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

    /**
     * 随机消费，同步请求的方式
     */
    @Test
    public void startSyncRandomConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_random_config.xml");
        SessionUtils.printSessionInfo();

        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(3000);
            var cm = new CM_Provider();
            cm.setA(NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString());
            try {
                System.out.println("客户端发送消息：" + JsonUtils.object2String(cm));
                var sm = NetContext.getConsumer().syncAsk(cm, SM_Provider.class, null).packet();
                System.out.println("客户端收到消息：" + JsonUtils.object2String(sm));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * 随机消费，异步请求的方式
     */
    @Test
    public void startAsyncRandomConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_random_config.xml");
        SessionUtils.printSessionInfo();

        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(3000);
            var cm = new CM_Provider();
            cm.setA(NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString());
            System.out.println("客户端发送消息：" + JsonUtils.object2String(cm));
            NetContext.getConsumer().asyncAsk(cm, SM_Provider.class, null).whenComplete(sm -> {
                System.out.println("客户端收到消息：" + JsonUtils.object2String(sm));
            });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * 一致性hash算法消费方式
     */
    @Test
    public void startConsistentSessionConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_consistent_session_config.xml");
        SessionUtils.printSessionInfo();
        ThreadUtils.sleep(3000);
        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(3000);
            var cm = new CM_Provider();
            cm.setA(NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString());
            System.out.println("客户端发送消息：" + JsonUtils.object2String(cm));
            NetContext.getConsumer().asyncAsk(cm, SM_Provider.class, 100).whenComplete(sm -> {
                System.out.println("客户端收到消息：" + JsonUtils.object2String(sm));
            });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * 最短时间的消费方式
     */
    @Test
    public void startShortestTimeConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_shortest_time_config.xml");
        SessionUtils.printSessionInfo();
        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(3000);
            var cm = new CM_Provider();
            cm.setA(NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString());
            System.out.println("客户端发送消息：" + JsonUtils.object2String(cm));
            NetContext.getConsumer().asyncAsk(cm, SM_Provider.class, null).whenComplete(sm -> {
                System.out.println("客户端收到消息：" + JsonUtils.object2String(sm));
            });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


}
