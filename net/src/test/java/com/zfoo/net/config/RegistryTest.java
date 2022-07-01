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

package com.zfoo.net.config;

import com.zfoo.net.config.model.ConsumerConfig;
import com.zfoo.net.config.model.ProviderConfig;
import com.zfoo.net.consumer.registry.RegisterVO;
import com.zfoo.protocol.registration.ProtocolModule;
import com.zfoo.util.net.HostAndPort;
import io.netty.util.NetUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class RegistryTest {


    @Test
    public void registerVoTest() {
        var modules = List.of(new ProtocolModule((byte) 100, "aaa")
                , new ProtocolModule((byte) 120, "bbb"));
        var providerConfig = ProviderConfig.valueOf(HostAndPort.valueOf("127.0.0.1", 80).toHostAndPortStr(), modules);
        var consumerConfig = ConsumerConfig.valueOf(modules);

        var vo = RegisterVO.valueOf("test", providerConfig, consumerConfig);
        var voStr = vo.toString();
        System.out.println(voStr);
        var newVo = RegisterVO.parseString(voStr);
        Assert.assertEquals(vo, newVo);

        System.out.println(NetUtil.LOCALHOST);
        System.out.println(NetUtil.LOCALHOST4);
        System.out.println(NetUtil.LOCALHOST6);
        System.out.println(NetUtil.SOMAXCONN);
        System.out.println(NetUtil.LOOPBACK_IF);
    }

}
