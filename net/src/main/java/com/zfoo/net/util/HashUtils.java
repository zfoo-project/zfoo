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

package com.zfoo.net.util;

/**
 * The improved hash algorithm distributes values more uniformly but is slower than Java's built-in hash.
 * Choose based on your requirements.
 *
 * @author godotg
 */
public abstract class HashUtils {


    private static final int P = 16777619;
    private static final int INIT_HASH = (int) 2166136261L;

    /**
     * Improved 32-bit FNV-1 hash algorithm.
     *
     * @param data byte array to hash
     * @return hash result
     */
    public static int fnvHash(byte[] data) {
        var hash = INIT_HASH;
        for (byte b : data) {
            hash = (hash ^ b) * P;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return Math.abs(hash);
    }

    /**
     * Improved 32-bit FNV-1 hash algorithm.
     *
     * @param object the object to hash; its toString() method will be called
     * @return hash result
     */
    public static int fnvHash(Object object) {
        var hash = object.toString().chars().reduce(INIT_HASH, (left, right) -> (left ^ right) * P);
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return Math.abs(hash);
    }

}
