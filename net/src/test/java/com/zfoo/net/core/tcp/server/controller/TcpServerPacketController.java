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

package com.zfoo.net.core.tcp.server.controller;

import com.zfoo.net.NetContext;
import com.zfoo.net.dispatcher.model.anno.PacketReceiver;
import com.zfoo.net.packet.*;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Component
public class TcpServerPacketController {

    @PacketReceiver
    public void atCM_Int(Session session, CM_Int cm) {
        System.out.println("receive packet from client:");
        System.out.println(JsonUtils.object2String(cm));

        SM_Int sm = new SM_Int();
        sm.setFlag(false);
        sm.setA(Byte.MIN_VALUE);
        sm.setB(Short.MIN_VALUE);
        sm.setC(Integer.MIN_VALUE);
        sm.setD(Long.MIN_VALUE);
        sm.setE('e');
        sm.setF("Hello，this is the Server!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        NetContext.getDispatcher().send(session, sm);
    }

    @PacketReceiver
    public void atCM_SyncMess(Session session, CM_SyncMess cm) {
        // 测试超时
        // ThreadUtils.sleep(Integer.MAX_VALUE);

        // 测试正常返回
        SM_SyncMess sm = new SM_SyncMess();
        sm.setA("Hello, this is server!");
        sm.setId(cm.getId());

        // 测试返回不是预期的消息
        // SM_Int sm = new SM_Int();

        // 测试错误返回
        // var sm = ErrorResponse.valueOf(1, 1, "this is error response");


        NetContext.getDispatcher().send(session, sm);

        var info = StringUtils.MULTIPLE_HYPHENS + FileUtils.LS
                + JsonUtils.object2String(cm) + FileUtils.LS
                + JsonUtils.object2String(sm) + FileUtils.LS;

        System.out.println(info);
    }


    // client0->server0->server1->server0->client0
    @PacketReceiver
    public void atCM_AsyncMess0(Session session, CM_AsyncMess0 cm0) {
        CM_AsyncMess1 cm1 = new CM_AsyncMess1();
        cm1.setA("Hello, server0 -> server1");

        var server1 = NetContext.getSessionManager().getClientSession(0L);
        NetContext.getDispatcher().asyncAsk(server1, cm1, SM_AsyncMess1.class, null)
                .whenComplete(sm_asyncMess0 -> {

                    SM_AsyncMess0 sm = new SM_AsyncMess0();
                    sm.setA("Hello, server0 -> client0!");

                    NetContext.getDispatcher().send(session, sm);
                    var info = StringUtils.MULTIPLE_HYPHENS + FileUtils.LS
                            + JsonUtils.object2String(cm0) + FileUtils.LS
                            + JsonUtils.object2String(sm) + FileUtils.LS;

                    System.out.println(info);

                });
    }

    @PacketReceiver
    public void atCM_AsyncMess1(Session session, CM_AsyncMess1 cm) {
        // 测试超时
        // ThreadUtils.sleep(Integer.MAX_VALUE);

        // 测试正常返回
        SM_AsyncMess1 sm = new SM_AsyncMess1();
        sm.setA("Hello, server1 -> server0!");

        // 测试返回不是预期的消息
        // SM_Int sm = new SM_Int();

        // 测试错误返回
        // var sm = ErrorResponse.valueOf(1, 1, "this is error response");

        NetContext.getDispatcher().send(session, sm);

        var info = StringUtils.MULTIPLE_HYPHENS + FileUtils.LS
                + JsonUtils.object2String(cm) + FileUtils.LS
                + JsonUtils.object2String(sm) + FileUtils.LS;

        System.out.println(info);
    }

}
