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
 */
public abstract class UuidUtils {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    /**
     * Get a locally unique int ID; wraps around when reaching max; thread-safe
     */
    public static int getLocalIntId() {
        return ATOMIC_INTEGER.incrementAndGet();
    }


    /**
     * Get a unique ID in a distributed environment
     *
     * @return String UUID
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        // Strip the “-”characters
        return uuid.replaceAll("-", "");
    }


    /**
     * Smaller ID first, larger ID second
     *
     * @param a first number
     * @param b second number
     * @return generated ID
     */
    public static String generateStringId(long a, long b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

}
