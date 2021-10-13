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
 */

package com.zfoo.net.core.tcp.server;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.tcp.*;
import com.zfoo.net.router.receiver.PacketReceiver;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Component
public class TcpServerPacketController {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerPacketController.class);

    @PacketReceiver
    public void atTcpHelloRequest(Session session, TcpHelloRequest request) {
        logger.info("receive [packet:{}] from client", JsonUtils.object2String(request));

        var response = new TcpHelloResponse();
        response.setMessage("Hello, this is the tcp server!");

        NetContext.getRouter().send(session, response);
    }

    @PacketReceiver
    public void atSyncMessAsk(Session session, SyncMessAsk ask) {
        logger.info("receive [packet:{}] from client", JsonUtils.object2String(ask));

        // 测试超时
        // ThreadUtils.sleep(Integer.MAX_VALUE);

        // 测试正常返回
        var answer = new SyncMessAnswer();
        answer.setMessage("Hello, this is sync server answer!");

        // 测试返回不是预期的消息
        // SM_Int sm = new SM_Int();

        // 测试错误返回
        // var sm = ErrorResponse.valueOf(1, 1, "this is error response");

        NetContext.getRouter().send(session, answer);
    }


    // client->server1->server0->client
    @PacketReceiver
    public void atAsyncMess0Ask(Session session, AsyncMess0Ask ask0) {
        var ask1 = new AsyncMess1Ask();
        ask1.setMessage("Hello, server0 -> server1");

        var client0 = NetContext.getSessionManager().getClientSession(0L);
        NetContext.getRouter().asyncAsk(client0, ask1, AsyncMess1Answer.class, null)
                .whenComplete(sm_asyncMess0 -> {

                    var answer = new AsyncMess0Answer();
                    answer.setMessage("Hello, server1 -> client!");

                    NetContext.getRouter().send(session, answer);
                });
    }

    @PacketReceiver
    public void atAsyncMess1Ask(Session session, AsyncMess1Ask ask) {
        // 测试超时
        // ThreadUtils.sleep(Integer.MAX_VALUE);

        // 测试正常返回
        var answer = new AsyncMess1Answer();
        answer.setMessage("Hello, server0 -> server1!");

        // 测试返回不是预期的消息
        // SM_Int sm = new SM_Int();

        // 测试错误返回
        // var sm = ErrorResponse.valueOf(1, 1, "this is error response");

        NetContext.getRouter().send(session, answer);
    }

}
