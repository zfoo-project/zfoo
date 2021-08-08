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

package com.zfoo.net.dispatcher;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.dispatcher.manager.PacketSignal;
import com.zfoo.net.packet.model.SignalPacketAttachment;
import com.zfoo.scheduler.util.TimeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class PacketSignalTest {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Test
    public void test() throws InterruptedException {
        // 预热
        addAndRemove();

        var startTime = TimeUtils.currentTimeMillis();

        var countDownLatch = new CountDownLatch(EventBus.EXECUTORS_SIZE);
        for (int i = 0; i < EventBus.EXECUTORS_SIZE; i++) {
            EventBus.asyncExecute(i).execute(new Runnable() {
                @Override
                public void run() {
                    addAndRemove();
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        System.out.println(TimeUtils.currentTimeMillis() - startTime);
        PacketSignal.status();
    }

    public void addAndRemove() {
        for (int count = 0; count < 1_0000; count++) {
            var startIndex = atomicInteger.incrementAndGet();
            var endIndex = 0;
            for (int i = 0; i < 1000; i++) {
                endIndex = atomicInteger.incrementAndGet();
            }
            for (int i = startIndex; i < endIndex; i++) {
                var attachment = new SignalPacketAttachment();
                attachment.setPacketId(i);
                PacketSignal.addSignalAttachment(attachment);
            }
            for (int i = startIndex; i < endIndex; i++) {
                PacketSignal.removeSignalAttachment(i);
            }
        }
    }


}
