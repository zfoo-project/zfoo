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
 * 改进的hash算法虽然发布比较均匀，但是没有Java自带的hash算法速度快，所以需要知道自己需要什么
 *
 * @author godotg
 * @version 3.0
 */
public abstract class HashUtils {


    private static final int P = 16777619;
    private static final int INIT_HASH = (int) 2166136261L;

    /**
     * 改进的32位FNV算法1
     *
     * @param data 数组
     * @return hash结果
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
     * 改进的32位FNV算法1
     *
     * @param object 计算hash的对象，会调用toString方法
     * @return hash结果
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
