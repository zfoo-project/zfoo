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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class UuidUtils {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    /**
     * 获取本地int的唯一id，如果达到最大值则重新从最小值重新计算，线程安全
     */
    public static int getLocalIntId() {
        return ATOMIC_INTEGER.incrementAndGet();
    }


    /**
     * 获得分布式环境下唯一id
     *
     * @return String UUID
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        //去掉“-”符号
        return uuid.replaceAll("-", "");
    }


    /**
     * 小的id在前 - 大的id在后
     *
     * @param a 第一个数字
     * @param b 第二个数字
     * @return 生成的id
     */
    public static String generateStringId(long a, long b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

}
