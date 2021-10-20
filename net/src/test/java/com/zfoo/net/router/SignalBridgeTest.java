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
import com.zfoo.net.router.route.SignalBridge;
import com.zfoo.scheduler.util.TimeUtils;
import com.zfoo.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class SignalBridgeTest {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    private final int executorSize = EventBus.EXECUTORS_SIZE / 2;
    private final int count = 100_0000;
    private final int totalIndex = 10;

    @Test
    public void test() throws InterruptedException {
        // 预热
        arrayTest();
        mapTest();

        ThreadUtils.sleep(3000);

        arrayTest();
        mapTest();
        System.out.println(atomicInteger.get());
    }

    public void arrayTest() throws InterruptedException {
        var startTime = TimeUtils.currentTimeMillis();

        var countDownLatch = new CountDownLatch(executorSize);
        for (var i = 0; i < executorSize; i++) {
            EventBus.execute(i).execute(new Runnable() {
                @Override
                public void run() {
                    addAndRemoveArray();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        SignalBridge.status();
        System.out.println(TimeUtils.currentTimeMillis() - startTime);
    }

    public void mapTest() throws InterruptedException {
        var startTime = TimeUtils.currentTimeMillis();

        var countDownLatch = new CountDownLatch(executorSize);
        for (int i = 0; i < executorSize; i++) {
            EventBus.execute(i).execute(new Runnable() {
                @Override
                public void run() {
                    addAndRemoveMap();
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        PacketSignalMap.status();
        System.out.println(TimeUtils.currentTimeMillis() - startTime);
    }

    public void addAndRemoveArray() {
        var list = new ArrayList<Integer>(totalIndex);
        for (var i = 0; i < count; i++) {
            list.clear();
            for (var j = 0; j < totalIndex; j++) {
                var index = atomicInteger.incrementAndGet();
                list.add(index);
                PacketSignalArray.addSignalAttachment(index);
            }

            for (var index : list) {
                PacketSignalArray.removeSignalAttachment(index);
            }
        }
    }


    public void addAndRemoveMap() {
        var list = new ArrayList<Integer>(totalIndex);
        for (var i = 0; i < count; i++) {
            list.clear();
            for (var j = 0; j < totalIndex; j++) {
                var index = atomicInteger.incrementAndGet();
                list.add(index);
                PacketSignalMap.addSignalAttachment(index);
            }

            for (var index : list) {
                PacketSignalMap.removeSignalAttachment(index);
            }
        }
    }

}
