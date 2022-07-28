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

package com.zfoo.orm.cache.guava;

import com.google.common.cache.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.zfoo.util.ThreadUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author godotg
 * @version 1.0
 * @since 2019-08-13 11:15
 */
@Ignore
public class GuavaCacheTest {

    @Test
    public void sizeRemovedTest() {
        RemovalListener<String, String> listener = new RemovalListener<String, String>() {
            public void onRemoval(RemovalNotification<String, String> notification) {
                System.out.println("[" + notification.getKey() + ":" + notification.getValue() + "] is removed!");

                // 采用LRU算法，所以最后会移除key1
                Assert.assertEquals(notification.getKey(), "key1");
                Assert.assertEquals(notification.getValue(), "value1");
            }
        };
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(3)
                .removalListener(listener)
                .build();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4");
    }

    @Test
    public void timeRemovedTest() {
        // 基于过期时间的策略不会回调这个remove方法
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .refreshAfterWrite(5, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    public void onRemoval(RemovalNotification<String, String> notification) {
                        System.out.println("[" + notification.getKey() + ":" + notification.getValue() + "] is removed!");
                    }
                })
                .build(new CacheLoader<String, String>() {
                    public String load(String key) {
                        System.out.println("load by " + Thread.currentThread().getName());
                        return "newValue";
                    }

                    @Override
                    public ListenableFuture<String> reload(String key, String oldValue) {
                        System.out.println("reload by " + Thread.currentThread().getName());
                        return Futures.immediateFuture("newValue");
                    }
                });

        cache.put("key1", "value1");

        Assert.assertEquals(cache.getIfPresent("key1"), "value1");
        ThreadUtils.sleep(5000);
        Assert.assertEquals(cache.getIfPresent("key1"), "newValue");
    }


}
