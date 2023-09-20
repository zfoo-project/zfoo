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

package com.zfoo.net.core.tcpSync.server;

import com.zfoo.net.NetContext;
import com.zfoo.net.anno.Task;
import com.zfoo.net.packet.tcp.SyncMessAnswer;
import com.zfoo.net.packet.tcp.SyncMessAsk;
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 */
@Component
public class TcpSyncController {

    private static final Logger logger = LoggerFactory.getLogger(TcpSyncController.class);

    @PacketReceiver(Task.VirtualThread)
    public void atSyncMessAsk(Session session, SyncMessAsk ask, SignalAttachment attachment) {
        logger.info("receive [packet:{}] from client", JsonUtils.object2String(ask));

        var answer = new SyncMessAnswer();
        answer.setMessage("Hello, this is the tcp server!");

        NetContext.getRouter().send(session, answer, attachment);
    }

}
