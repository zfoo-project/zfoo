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

import com.zfoo.net.dispatcher.model.anno.PacketReceiver;
import com.zfoo.net.packet.SM_Int;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.util.JsonUtils;
import org.springframework.stereotype.Component;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Component
public class TcpClientPacketController {


    @PacketReceiver
    public void atSM_Int(Session session, SM_Int sm) {
        System.out.println("receive packet from server:");
        System.out.println(JsonUtils.object2String(sm));
    }

}
