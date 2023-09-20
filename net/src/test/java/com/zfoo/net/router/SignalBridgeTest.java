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

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.scheduler.util.TimeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * @author godotg
 */
@Ignore
public class SignalBridgeTest {

    private final int executorSize = EventBus.EXECUTORS_SIZE;
    private final int count = 100_0000;
    private final int totalIndex = 10;

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            arrayTest();
        }
        SignalBridge.status();
        System.out.println(SignalAttachment.ATOMIC_ID.get());
    }

    public void arrayTest() throws InterruptedException {
        var startTime = TimeUtils.currentTimeMillis();

        var countDownLatch = new CountDownLatch(executorSize);
        for (var i = 0; i < executorSize; i++) {
            EventBus.execute(i, new Runnable() {
                @Override
                public void run() {
                    addAndRemoveArray();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println(TimeUtils.currentTimeMillis() - startTime);
    }


    public void addAndRemoveArray() {
        var signalList = new ArrayList<Integer>(totalIndex);
        for (var i = 0; i < count; i++) {
            signalList.clear();
            for (var j = 0; j < totalIndex; j++) {
                var signalAttachment = new SignalAttachment();
                SignalBridge.addSignalAttachment(signalAttachment);
                signalList.add(signalAttachment.getSignalId());
            }

            for (var signalId : signalList) {
                SignalBridge.removeSignalAttachment(signalId);
            }
        }
    }

}
