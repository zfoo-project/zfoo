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

package com.zfoo.protocol.collection.lpmap;

import com.zfoo.protocol.collection.lpmap.model.MyPacket;
import com.zfoo.protocol.ProtocolManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 */
@Ignore
public class ConcurrentHeapMapTesting {

    private static final int EXECUTOR_SIZE = Runtime.getRuntime().availableProcessors();

    @Test
    public void putIfAbsentTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));
        var myPacket = new MyPacket();
        myPacket.setA(1);

        var map = new ConcurrentHeapMap<MyPacket>();

        var previous1 = map.put(1, myPacket);
        var previous2 = map.put(2, myPacket);
        var previous3 = map.put(2, new MyPacket());

        Assert.assertNull(previous1);
        Assert.assertNull(previous2);
        Assert.assertEquals(previous3, myPacket);
    }

    @Test
    public void benchmarkTest() throws IOException, InterruptedException {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new ConcurrentHeapMap<MyPacket>();
        var atomicInt = new AtomicInteger(0);
        var count = 1000_0000;

        var countdown = new CountDownLatch(EXECUTOR_SIZE);
        for (int i = 0; i < EXECUTOR_SIZE; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    var key = atomicInt.getAndIncrement();
                    while (key < count) {
                        var myPacket = MyPacket.valueOf(key, String.valueOf(key));
                        map.put(key, myPacket);
                        key = atomicInt.getAndIncrement();
                    }
                    countdown.countDown();
                }
            }).start();
        }
        countdown.await();
        for (var i = 0; i < count; i++) {
            var myPacket = MyPacket.valueOf(i, String.valueOf(i));
            var packet = map.get(i);
            Assert.assertEquals(myPacket, packet);
        }
    }
}
