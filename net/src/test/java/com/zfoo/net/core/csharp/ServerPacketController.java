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

package com.zfoo.net.core.csharp;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.csharp.CM_CSharpRequest;
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.JsonUtils;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 * @version 3.0
 */
@Component
public class ServerPacketController {

    @PacketReceiver
    public void atCM_CSharpRequest(Session session, CM_CSharpRequest cm) {
        System.out.println("receive packet from client:");
        System.out.println(JsonUtils.object2String(cm));

        NetContext.getRouter().send(session, cm);
    }

}
