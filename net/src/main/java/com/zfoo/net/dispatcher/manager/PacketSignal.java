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

package com.zfoo.net.dispatcher.manager;

import com.zfoo.net.packet.model.SignalPacketAttachment;
import com.zfoo.protocol.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 同步或异步的调用控制器
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class PacketSignal {

    private static final Logger logger = LoggerFactory.getLogger(PacketSignal.class);

    // equal with 16383
    private static final int SIGNAL_MASK = 0B00000000_00000000_00111111_11111111;

    private static AtomicReferenceArray<SignalPacketAttachment> signalPacketArray = new AtomicReferenceArray<>(SIGNAL_MASK + 1);

    /**
     * Session控制同步或异步的附加包，key：packetId
     */
    private static Map<Integer, SignalPacketAttachment> signalPacketAttachmentMap = new ConcurrentHashMap<>(1000);

    public static void addSignalAttachment(SignalPacketAttachment packetAttachment) {
        var packetId = packetAttachment.getPacketId();
        var hash = packetId & SIGNAL_MASK;

        if (!signalPacketArray.compareAndSet(hash, null, packetAttachment)) {
            signalPacketAttachmentMap.put(packetId, packetAttachment);
        }
    }

    public static SignalPacketAttachment removeSignalAttachment(SignalPacketAttachment packetAttachment) {
        return removeSignalAttachment(packetAttachment.getPacketId());
    }

    public static SignalPacketAttachment removeSignalAttachment(int packetId) {
        var hash = packetId & SIGNAL_MASK;

        var attachment = signalPacketArray.get(hash);
        if (attachment != null && attachment.getPacketId() == packetId && signalPacketArray.compareAndSet(hash, attachment, null)) {
            return attachment;
        }
        return signalPacketAttachmentMap.remove(packetId);
    }

    public static void status() {
        var count = 0;
        for (int i = 0; i < SIGNAL_MASK + 1; i++) {
            var value = signalPacketArray.get(i);
            if (value != null) {
                logger.info("signalPacketArray has attachment [index:{}][count:{}][value:{}]", i, ++count, JsonUtils.object2String(value));
            }
        }

        signalPacketAttachmentMap.forEach((key, value) -> {
            logger.info("signalPacketAttachmentMap has attachment [key:{}][value:{}]", key, JsonUtils.object2String(value));
        });
    }

}
