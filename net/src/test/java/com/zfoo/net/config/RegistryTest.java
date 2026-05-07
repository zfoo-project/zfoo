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
import com.zfoo.net.consumer.registry.Register;
import com.zfoo.net.core.HostAndPort;
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
        // Define 2 protocol modules that can be used by both providers and consumers (module metadata only)
        var protocolModule1 = "aaa";
        var protocolModule2 ="bbb";

        // Provider module list and provider configuration
        // Define 2 service-provider modules
        var providerModules = List.of(new ProviderModule(protocolModule1, "a"), new ProviderModule(protocolModule2, "b"));
        // Provider config: IP address + provider modules
        var providerConfig = ProviderConfig.valueOf(HostAndPort.valueOf("127.0.0.1", 80).toHostAndPortStr(), providerModules);

        // Consumer module list and consumer config (consumer module has an extra load-balance attribute)
        var consumerModules = List.of(new ConsumerModule("random", "a"), new ConsumerModule("random", "b"));
        // Consumer config: no IP (consumers connect out, not listen)
        var consumerConfig = ConsumerConfig.valueOf(consumerModules);

        var register = Register.valueOf("test", providerConfig, consumerConfig);
        var voStr = register.toString();

        // test | 127.0.0.1:80 | provider:[100-aaa-a, 120-bbb-b] | consumer:[100-aaa-random-a, 120-bbb-random-b]
        System.out.println(voStr);

        var newRegister = Register.parseString(voStr);
        Assert.assertEquals(register, newRegister);

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