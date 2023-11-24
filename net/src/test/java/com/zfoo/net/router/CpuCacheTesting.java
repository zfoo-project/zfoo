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
 *
 */
package com.zfoo.net.router;

import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author godotg
 */
@Ignore
public class CpuCacheTesting {

    public static final int threadNum = Runtime.getRuntime().availableProcessors() / 2;
    public static final ExecutorService[] executors = new ExecutorService[threadNum];
    public static final UserPO[] users = new UserPO[threadNum];


    public static class UserPO {
        public int a;
        public int b;
        public int c;
        public int d;
        public int e;
        public int f;
        public int g;
        public int h;
        public String s = "cpu cache test";
        public List<Integer> l = List.of(100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009,
                100000, 100001, 100002, 100003, 100004, 100005, 100006, 100007, 100008, 100009
        );
    }

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        for (int i = 0; i < threadNum; i++) {
            users[i] = new UserPO();
        }

        var hashTaskCount = 0;
        var competeTaskCount = 0;
        for (int i = 0; i < 100; i++) {
            var a = hashTaskTest();
            var b = competeTaskTest();
            if (a < b) {
                hashTaskCount++;
            } else {
                competeTaskCount++;
            }
        }
        System.out.println(StringUtils.format("cpu缓存友好写法胜出{} 不友好写法胜出{}", hashTaskCount, competeTaskCount));
    }


    public long hashTaskTest() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            int index = i;
            executors[i].execute(() -> {
                for (int j = 0; j < 100_0000; j++) {
                    var randomInt = RandomUtils.randomInt(0, Integer.MAX_VALUE);
                    randomInt = index;
                    var user = users[index % threadNum];
                    user.a = index;
                    user.b = index;
                    user.c = index;
                    user.d = index;
                    user.e = index;
                    user.f = index;
                    user.g = index;
                    user.h = index;
                }
                countdown.countDown();
            });
        }
        countdown.await();
        long endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        System.out.println(StringUtils.format("cpu缓存友好的任务调度 -> {}", cost));
        return cost;
    }

    public long competeTaskTest() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            int index = i;
            executors[i].execute(() -> {
                for (int j = 0; j < 100_0000; j++) {
                    var randomInt = RandomUtils.randomInt(0, Integer.MAX_VALUE);
                    var user = users[randomInt % threadNum];
                    user.a = index;
                    user.b = index;
                    user.c = index;
                    user.d = index;
                    user.e = index;
                    user.f = index;
                    user.g = index;
                    user.h = index;
                }
                countdown.countDown();
            });
        }
        countdown.await();
        long endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        System.out.println(StringUtils.format("cpu缓存不友好的任务调度 -> {}", cost));
        return cost;
    }
}
