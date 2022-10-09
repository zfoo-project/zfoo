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

package com.zfoo.protocol.collection;

import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class ConcurrentTest {

    private static final int EXECUTOR_SIZE = Runtime.getRuntime().availableProcessors();

    @Test
    public void test() throws InterruptedException {
        var map = new CopyOnWriteHashMapLongObject<Integer>();
        var num = 1_0000;
        var countDownLatch = new CountDownLatch(EXECUTOR_SIZE);
        for (var i = 0; i < EXECUTOR_SIZE; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < num; j++) {
                        map.put((long) j, j);
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();
        Assert.assertEquals(map.size(), num);

        var countDownLatch2 = new CountDownLatch(EXECUTOR_SIZE);
        for (var i = 0; i < EXECUTOR_SIZE; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < num; j++) {
                        map.remove((long) j);
                    }
                    countDownLatch2.countDown();
                }
            }).start();
        }
        countDownLatch2.await();
        Assert.assertTrue(map.isEmpty());
    }
}
