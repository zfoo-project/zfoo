/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.util;

import com.zfoo.event.manager.EventBus;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 单值缓存，会隔一段时间在后台刷新一下缓存
 *
 * @author godotg
 * @version 3.0
 */
public class SingleCache<V> {

    private long refreshDuration;

    private Supplier<V> supplier;


    private volatile V cache;
    private volatile long refreshTime;
    private final Lock lock = new ReentrantLock();


    /**
     * @param refreshDuration 刷新实际那，毫秒
     * @param supplier        缓存提供者
     * @return 简单的缓存
     */
    public static <V> SingleCache<V> build(long refreshDuration, Supplier<V> supplier) {
        var cache = new SingleCache<V>();
        cache.refreshDuration = refreshDuration;
        cache.supplier = supplier;
        cache.cache = supplier.get();
        cache.refreshTime = TimeUtils.now() + refreshDuration;
        return cache;
    }


    public V get() {
        var now = TimeUtils.now();
        // 使用双重检测锁的方式
        if (now > refreshTime) {
            lock.lock();
            try {
                if (now > refreshTime) {
                    refreshTime = now + refreshDuration;
                    cache = supplier.get();
                }
            } finally {
                lock.unlock();
            }
        }
        return cache;
    }

    public void set(V value) {
        cache = value;
    }

}
