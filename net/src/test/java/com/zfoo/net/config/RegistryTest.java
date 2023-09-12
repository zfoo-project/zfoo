/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.zfoo.net.config;

import com.zfoo.net.config.model.ConsumerConfig;
import com.zfoo.net.config.model.ConsumerModule;
import com.zfoo.net.config.model.ProviderConfig;
import com.zfoo.net.config.model.ProviderModule;
import com.zfoo.net.consumer.registry.RegisterVO;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.protocol.registration.ProtocolModule;
import io.netty.util.NetUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author godotg
 */
public class RegistryTest {

    @Test
    public void registerVoTest() {
        // 定义2个模块：可以为服务提供者用，也可以为服务消费者用，这个仅仅是模块信息
        var protocolModule1 = new ProtocolModule((byte) 100, "aaa");
        var protocolModule2 = new ProtocolModule((byte) 120, "bbb");

        // 服务提供者模块列表和服务提供者配置
        // 定义2个服务提供者模块
        var providerModules = List.of(new ProviderModule(protocolModule1, "a"), new ProviderModule(protocolModule2, "b"));
        // 服务器提供者配置：服务提供者的ip + 服务提供者模块
        var providerConfig = ProviderConfig.valueOf(HostAndPort.valueOf("127.0.0.1", 80).toHostAndPortStr(), providerModules);

        // 服务消费者模块和服务消费者配置(服务消费者模块多一个负载均衡属性)
        var consumerModules = List.of(new ConsumerModule(protocolModule1, "random", "a"), new ConsumerModule(protocolModule2, "random", "b"));
        // 服务消费者配置：这个是没Ip的
        var consumerConfig = ConsumerConfig.valueOf(consumerModules);

        var vo = RegisterVO.valueOf("test", providerConfig, consumerConfig);
        var voStr = vo.toString();

        // test | 127.0.0.1:80 | provider:[100-aaa-a, 120-bbb-b] | consumer:[100-aaa-random-a, 120-bbb-random-b]
        System.out.println(voStr);

        var newVo = RegisterVO.parseString(voStr);
        Assert.assertEquals(vo, newVo);

        // /127.0.0.1
        System.out.println(NetUtil.LOCALHOST);

        // localhost/127.0.0.1
        System.out.println(NetUtil.LOCALHOST4);

        //localhost/0:0:0:0:0:0:0:1
        System.out.println(NetUtil.LOCALHOST6);

        // 200
        System.out.println(NetUtil.SOMAXCONN);

        // name:lo (Software Loopback Interface 1)
        System.out.println(NetUtil.LOOPBACK_IF);
    }

}

/*
test | 127.0.0.1:80 | provider:[100-aaa-a, 120-bbb-b] | consumer:[100-aaa-random-a, 120-bbb-random-b]
/127.0.0.1
localhost/127.0.0.1
localhost/0:0:0:0:0:0:0:1
200
name:lo (Software Loopback Interface 1)
 */