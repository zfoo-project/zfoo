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

package com.zfoo.protocol.util;

import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * @author godotg
 */
public abstract class ThreadUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    private static final int WAIT_TIME = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过线程号寻找对应的线程
     */
    public static Thread findThread(long threadId) {
        var group = Thread.currentThread().getThreadGroup();
        while (group != null) {
            var threads = new Thread[group.activeCount() * 2];
            var count = group.enumerate(threads, true);
            for (var i = 0; i < count; i++) {
                if (threadId == threads[i].getId()) {
                    return threads[i];
                }
            }
            group = group.getParent();
        }
        return null;
    }


    public static void shutdown(ExecutorService executor) {
        try {
            if (!executor.isTerminated()) {

                executor.shutdown();

                if (!executor.awaitTermination(WAIT_TIME, TIME_UNIT)) {
                    executor.shutdownNow();
                }

            }
        } catch (Exception e) {
            logger.error("[{}] is failed to shutdown! ", executor, e);
        }
    }

    public synchronized static void shutdownEventLoopGracefully(String executorGroupName, EventExecutorGroup executor) {
        if (executor == null) {
            return;
        }
        try {
            if (!executor.isTerminated()) {
                executor.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("[{}] is failed to shutdown! ", executorGroupName, e);
            return;
        }
        logger.info("[{}] shutdown gracefully.", executorGroupName);
    }

    public static void shutdownForkJoinPool() {
        try {
            ForkJoinPool.commonPool().shutdown();

            if (ForkJoinPool.commonPool().awaitTermination(WAIT_TIME, TimeUnit.SECONDS)) {
                ForkJoinPool.commonPool().shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用kill退出的方式，不能调用这个停止方法
     */
    public static void shutdownApplication() {
        new Thread(() -> {
            System.exit(0);
        }).start();
    }

    public static Runnable safeRunnable(Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.error("unknown exception", e);
                } catch (Throwable t) {
                    logger.error("unknown error", t);
                }
            }
        };
    }

}
