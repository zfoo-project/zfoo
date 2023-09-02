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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class RandomUtils {


    /**
     * 用于随机选的字符和数字
     */
    public static final String BASE_CHAR_NUMBER = StringUtils.ENGLISH_CHAR + StringUtils.ARAB_NUMBER;

    /**
     * 使用ThreadLocalRandom产生随机数，能够解决多个线程发生的竞争争夺。
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 获取{@link SecureRandom}，类提供加密的强随机数生成器 (RNG)
     */
    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取随机数产生器
     *
     * @param isSecure 是否为强随机数生成器 (RNG)
     * @return {@link Random}
     */
    public static Random getRandom(boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }

    /**
     * 获得随机Boolean值
     *
     * @return true or false
     */
    public static boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     */
    public static int randomInt(int min, int max) {
        return getRandom().nextInt(min, max);
    }

    /**
     * 获得随机数[-2^32, 2^32)
     *
     * @return 随机数
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     */
    public static int randomInt(int limit) {
        return getRandom().nextInt(limit);
    }

    /**
     * 获得指定范围内的随机数[min, max)
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     */
    public static long randomLong(long min, long max) {
        return getRandom().nextLong(min, max);
    }

    /**
     * 获得随机数
     *
     * @return 随机数
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     */
    public static long randomLong(long limit) {
        return getRandom().nextLong(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     */
    public static double randomDouble(double min, double max) {
        return getRandom().nextDouble(min, max);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min          最小数（包含）
     * @param max          最大数（不包含）
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(double min, double max, int scale, RoundingMode roundingMode) {
        return NumberUtils.round(randomDouble(min, max), scale, roundingMode).doubleValue();
    }

    /**
     * 获得随机数[0, 1)
     *
     * @return 随机数
     */
    public static double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(int scale, RoundingMode roundingMode) {
        return NumberUtils.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     */
    public static double randomDouble(double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param limit        限制随机数的范围，不包括这个数
     * @param scale        保留小数位数
     * @param roundingMode 保留小数的模式 {@link RoundingMode}
     * @return 随机数
     */
    public static double randomDouble(double limit, int scale, RoundingMode roundingMode) {
        return NumberUtils.round(randomDouble(limit), scale, roundingMode).doubleValue();
    }

    /**
     * 获得指定范围内的随机数[0, 1)
     *
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal() {
        return NumberUtils.toBigDecimal(getRandom().nextDouble());
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 最大数（不包含）
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal(BigDecimal limit) {
        return NumberUtils.toBigDecimal(getRandom().nextDouble(limit.doubleValue()));
    }

    /**
     * 获得指定范围内的随机数
     *
     * @param min 最小数（包含）
     * @param max 最大数（不包含）
     * @return 随机数
     */
    public static BigDecimal randomBigDecimal(BigDecimal min, BigDecimal max) {
        return NumberUtils.toBigDecimal(getRandom().nextDouble(min.doubleValue(), max.doubleValue()));
    }

    /**
     * 随机bytes
     *
     * @param length 长度
     * @return bytes
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        getRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>  元素类型
     * @param list 列表
     * @return 随机元素
     */
    public static <T> T randomEle(List<T> list) {
        return randomEle(list, list.size());
    }

    /**
     * 随机获得列表中的元素
     *
     * @param <T>   元素类型
     * @param list  列表
     * @param limit 限制列表的前N项
     * @return 随机元素
     */
    public static <T> T randomEle(List<T> list, int limit) {
        return list.get(randomInt(limit));
    }

    /**
     * 随机获得数组中的元素
     *
     * @param <T>   元素类型
     * @param array 列表
     * @return 随机元素
     */
    public static <T> T randomEle(T[] array) {
        return randomEle(array, array.length);
    }

    /**
     * 随机获得数组中的元素
     *
     * @param <T>   元素类型
     * @param array 列表
     * @param limit 限制列表的前N项
     * @return 随机元素
     */
    public static <T> T randomEle(T[] array, int limit) {
        return array[randomInt(limit)];
    }

    /**
     * 随机获得列表中的一定量元素
     *
     * @param <T>   元素类型
     * @param list  列表
     * @param count 随机取出的个数
     * @return 随机元素
     */
    public static <T> List<T> randomEles(List<T> list, int count) {
        final List<T> result = new ArrayList<T>(count);
        int limit = list.size();
        while (result.size() < count) {
            result.add(randomEle(list, limit));
        }

        return result;
    }

    /**
     * 随机获得列表中的一定量的不重复元素，返回Set
     *
     * @param <T>        元素类型
     * @param collection 列表
     * @param count      随机取出的个数
     * @return 随机元素
     * @throws IllegalArgumentException 需要的长度大于给定集合非重复总数
     */
    public static <T> Set<T> randomEleSet(Collection<T> collection, int count) {
        ArrayList<T> source = new ArrayList<>(new HashSet<>(collection));
        if (count > source.size()) {
            throw new IllegalArgumentException("Count is larger than collection distinct size !");
        }

        final HashSet<T> result = new HashSet<T>(count);
        int limit = collection.size();
        while (result.size() < count) {
            result.add(randomEle(source, limit));
        }

        return result;
    }

    /**
     * 获得一个随机的字符串（只包含数字和字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串（只包含数字和大写字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomStringUpper(int length) {
        return randomString(BASE_CHAR_NUMBER, length).toUpperCase();
    }

    /**
     * 获得一个只包含数字的字符串
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomNumbers(int length) {
        return randomString(StringUtils.ARAB_NUMBER, length);
    }

    /**
     * 获得一个随机的字符串
     *
     * @param baseString 随机字符选取的样本
     * @param length     字符串的长度
     * @return 随机字符串
     */
    public static String randomString(String baseString, int length) {
        final StringBuilder sb = new StringBuilder();

        if (length < 1) {
            length = 1;
        }
        int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            int number = getRandom().nextInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 随机数字，数字为0~9单个数字
     *
     * @return 随机数字字符
     */
    public static int randomNumber() {
        return randomChar(StringUtils.ARAB_NUMBER);
    }

    /**
     * 随机字母或数字，小写
     *
     * @return 随机字符
     */
    public static char randomChar() {
        return randomChar(BASE_CHAR_NUMBER);
    }

    /**
     * 随机字符
     *
     * @param baseString 随机字符选取的样本
     * @return 随机字符
     */
    public static char randomChar(String baseString) {
        return baseString.charAt(getRandom().nextInt(baseString.length()));
    }

    /**
     * @author godotg
     * @version 3.0
     */
    public static class RandomSelector<T> {

        private int cursor = 0;

        private final TreeMap<Integer, T> elementMap = new TreeMap<>();

        public void addElement(T value, int weight) {
            if (value == null || weight <= 0) {
                return;
            }

            cursor += weight;
            elementMap.put(cursor, value);
        }

        public void clear() {
            elementMap.clear();
            cursor = 0;
        }

        public int size() {
            return elementMap.size();
        }

        public T select() {
            if (cursor <= 0) {
                throw new IllegalStateException("all weights are 0");
            }
            if (elementMap.isEmpty()) {
                throw new IllegalStateException("selected element is empty, please insert some elements");
            }

            var randomInt = randomInt(cursor) + 1;
            return elementMap.ceilingEntry(randomInt).getValue();
        }

        public List<T> select(int count) {
            List<T> resultList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                resultList.add(select());
            }
            return resultList;
        }

        public Collection<T> getAll() {
            return elementMap.values();
        }
    }
}
