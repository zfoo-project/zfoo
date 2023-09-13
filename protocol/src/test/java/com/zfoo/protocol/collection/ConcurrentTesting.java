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

import com.zfoo.protocol.collection.concurrent.ConcurrentHashMapLongObject;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

/**
 * @author godotg
 */
@Ignore
public class ConcurrentTesting {

    private static final int EXECUTOR_SIZE = Runtime.getRuntime().availableProcessors();

    @Test
    public void copyOnWriteTest() throws InterruptedException {
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

    public static final int num = 1_0000;
    public static final int maxCount = 10000;

    @Test
    public void concurrentPrimitiveTest() throws InterruptedException {
        var map = new ConcurrentHashMapLongObject<Integer>(128);
        var startTime = System.currentTimeMillis();

        for (int count = 0; count < maxCount; count++) {
            var countDownLatch = new CountDownLatch(EXECUTOR_SIZE);
            for (var i = 0; i < EXECUTOR_SIZE; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < num; j++) {
                            map.put(j, j);
                        }
                        map.forEachPrimitive(it -> it.value());
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
                            var value = (int) map.get((long) j);
                            Assert.assertEquals(value, j);
                        }
                        map.forEachPrimitive(it -> it.value());
                        countDownLatch2.countDown();
                    }
                }).start();
            }
            countDownLatch2.await();

            var countDownLatch3 = new CountDownLatch(EXECUTOR_SIZE);
            for (var i = 0; i < EXECUTOR_SIZE; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < num; j++) {
                            map.put(j, j);
                        }
                        map.forEachPrimitive(it -> it.value());
                        countDownLatch3.countDown();
                    }
                }).start();
            }
            countDownLatch3.await();

            var countDownLatch4 = new CountDownLatch(EXECUTOR_SIZE);
            for (var i = 0; i < EXECUTOR_SIZE; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < num; j++) {
                            map.remove((long) j);
                        }
                        map.forEachPrimitive(it -> it.value());
                        countDownLatch4.countDown();
                    }
                }).start();
            }
            countDownLatch4.await();
            Assert.assertTrue(map.isEmpty());
        }

        System.out.println(System.currentTimeMillis() - startTime);
    }

    @Test
    public void concurrentTest() throws InterruptedException {
        var map = new ConcurrentHashMap<Long, Integer>();
        var startTime = System.currentTimeMillis();

        for (int count = 0; count < maxCount; count++) {
            var countDownLatch = new CountDownLatch(EXECUTOR_SIZE);
            for (var i = 0; i < EXECUTOR_SIZE; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < num; j++) {
                            map.put((long) j, j);
                        }
                        map.forEach((key, value) -> value++);
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
                            var value = (int) map.get((long) j);
                            Assert.assertEquals(value, j);
                        }
                        map.forEach((key, value) -> value++);
                        countDownLatch2.countDown();
                    }
                }).start();
            }
            countDownLatch2.await();

            var countDownLatch3 = new CountDownLatch(EXECUTOR_SIZE);
            for (var i = 0; i < EXECUTOR_SIZE; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < num; j++) {
                            map.put((long) j, j);
                        }
                        map.forEach((key, value) -> value++);
                        countDownLatch3.countDown();
                    }
                }).start();
            }
            countDownLatch3.await();

            var countDownLatch4 = new CountDownLatch(EXECUTOR_SIZE);
            for (var i = 0; i < EXECUTOR_SIZE; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < num; j++) {
                            map.remove((long) j);
                        }
                        map.forEach((key, value) -> value++);
                        countDownLatch4.countDown();
                    }
                }).start();
            }
            countDownLatch4.await();
            Assert.assertTrue(map.isEmpty());
        }

        System.out.println(System.currentTimeMillis() - startTime);
    }
}
