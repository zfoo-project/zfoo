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

package com.zfoo.net.router;

import com.zfoo.net.router.attachment.SignalAttachment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Synchronous or asynchronous call controller, acting as a signal communication bridge for sync and async calls
 *
 * @author godotg
 */
public class SignalBridge {

    // equal with 32767
    private static final int SIGNAL_MASK = 0B00000000_00000000_01111111_11111111;

    /**
     * key: signalId
     */
    private static final Map<Integer, SignalAttachment> signalAttachmentMap = new ConcurrentHashMap<>(1000);
    /**
     * Ring buffer data structure, which is cache-friendly, reduces cache contention and improves memory access efficiency
     */
    private static final AtomicReferenceArray<SignalAttachment> signalAttachmentArray = new AtomicReferenceArray<>(SIGNAL_MASK + 1);

    public static void addSignalAttachment(SignalAttachment signalAttachment) {
        var signalId = signalAttachment.getSignalId();
        var hash = signalId & SIGNAL_MASK;

        // Using an AtomicReferenceArray is just to improve performance; using only a ConcurrentHashMap would also work correctly
        if (signalAttachmentArray.compareAndSet(hash, null, signalAttachment)) {
            return;
        }

        signalAttachmentMap.put(signalId, signalAttachment);
    }

    public static SignalAttachment removeSignalAttachment(SignalAttachment signalAttachment) {
        return removeSignalAttachment(signalAttachment.getSignalId());
    }

    public static SignalAttachment removeSignalAttachment(int signalId) {
        var hash = signalId & SIGNAL_MASK;

        var attachment = signalAttachmentArray.get(hash);
        if (attachment != null && attachment.getSignalId() == signalId) {
            // Use the higher-performance lazySet() when the modification doesn't need to be immediately visible to other threads
            signalAttachmentArray.lazySet(hash, null);
            return attachment;
        }
        return signalAttachmentMap.remove(signalId);
    }

    public static int signalSize() {
        var size = 0;
        for (int i = 0; i < SIGNAL_MASK + 1; i++) {
            var value = signalAttachmentArray.get(i);
            if (value != null) {
                size++;
            }
        }
        size += signalAttachmentMap.size();
        return size;
    }

}
