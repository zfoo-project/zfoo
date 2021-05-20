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

package com.zfoo.net.core.provider.controller;

import com.zfoo.net.NetContext;
import com.zfoo.net.dispatcher.model.anno.PacketReceiver;
import com.zfoo.net.packet.provider.CM_Provider;
import com.zfoo.net.packet.provider.SM_Provider;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.util.JsonUtils;
import org.springframework.stereotype.Component;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Component
public class ProviderController {

    @PacketReceiver
    public void atCM_Provider(Session session, CM_Provider cm) {
        System.out.println("服务器收到消息：" + JsonUtils.object2String(cm));

        var sm = new SM_Provider();
        sm.setA(NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString());
        sm.setB(cm.getB());

        System.out.println("服务器返回消息：" + JsonUtils.object2String(sm));
        NetContext.getDispatcher().send(session, sm);
    }
}
