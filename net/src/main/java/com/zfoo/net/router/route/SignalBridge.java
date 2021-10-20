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

package com.zfoo.net.router.route;

import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 同步或异步的调用控制器，同步和异步调用的信号沟通桥梁
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class SignalBridge {

    private static final Logger logger = LoggerFactory.getLogger(SignalBridge.class);

    // equal with 32767
    private static final int SIGNAL_MASK = 0B00000000_00000000_01111111_11111111;

    private static final AtomicReferenceArray<SignalAttachment> signalAttachmentArray = new AtomicReferenceArray<>(SIGNAL_MASK + 1);

    /**
     * Session控制同步或异步的附加包，key：packetId
     */
    private static final Map<Integer, SignalAttachment> signalAttachmentMap = new ConcurrentHashMap<>(1000);

    public static void addSignalAttachment(SignalAttachment signalAttachment) {
        var packetId = signalAttachment.getSignalId();
        var hash = packetId & SIGNAL_MASK;

        if (signalAttachmentArray.compareAndSet(hash, null, signalAttachment)) {
            return;
        }

        signalAttachmentMap.put(packetId, signalAttachment);
    }

    public static SignalAttachment removeSignalAttachment(SignalAttachment signalAttachment) {
        return removeSignalAttachment(signalAttachment.getSignalId());
    }

    public static SignalAttachment removeSignalAttachment(int packetId) {
        var hash = packetId & SIGNAL_MASK;

        var attachment = signalAttachmentArray.get(hash);
        if (attachment != null && attachment.getSignalId() == packetId && signalAttachmentArray.compareAndSet(hash, attachment, null)) {
            return attachment;
        }
        return signalAttachmentMap.remove(packetId);
    }

    public static void status() {
        var count = 0;
        for (int i = 0; i < SIGNAL_MASK + 1; i++) {
            var value = signalAttachmentArray.get(i);
            if (value != null) {
                logger.info("signalPacketArray has attachment [index:{}][count:{}][value:{}]", i, ++count, JsonUtils.object2String(value));
            }
        }

        signalAttachmentMap.forEach((key, value) -> {
            logger.info("signalAttachmentMap has attachment [key:{}][value:{}]", key, JsonUtils.object2String(value));
        });
    }

}
