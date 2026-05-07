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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author godotg
 */
public abstract class RandomUtils {


    /**
     * Characters and digits used for random selection
     */
    public static final String BASE_CHAR_NUMBER = StringUtils.ENGLISH_CHAR + StringUtils.ARAB_NUMBER;

    /**
     * Uses ThreadLocalRandom to avoid thread contention.
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * Get a {@link SecureRandom} instance; provides a cryptographically strong random number generator (RNG)
     */
    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a random number generator
     *
     * @param isSecure true to return a cryptographically strong RNG
     * @return {@link Random}
     */
    public static Random getRandom(boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }

    /**
     * Returns a random boolean value
     *
     * @return true or false
     */
    public static boolean randomBoolean() {
        return 0 == randomInt(2);
    }

    /**
     * Returns a random number within the specified range
     *
     * @param min minimum (inclusive)
     * @param max maximum (exclusive)
     * @return random number
     */
    public static int randomInt(int min, int max) {
        return getRandom().nextInt(min, max);
    }

    /**
     * Returns a random number in [-2^32, 2^32)
     *
     * @return random number
     */
    public static int randomInt() {
        return getRandom().nextInt();
    }

    /**
     * Returns a random number in [0, limit)
     *
     * @param limit upper bound (exclusive)
     * @return random number
     */
    public static int randomInt(int limit) {
        return getRandom().nextInt(limit);
    }

    /**
     * Returns a random number within the specified range[min, max)
     *
     * @param min minimum (inclusive)
     * @param max maximum (exclusive)
     * @return random number
     */
    public static long randomLong(long min, long max) {
        return getRandom().nextLong(min, max);
    }

    /**
     * Returns a random number
     *
     * @return random number
     */
    public static long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * Returns a random number in [0, limit)
     *
     * @param limit upper bound (exclusive)
     * @return random number
     */
    public static long randomLong(long limit) {
        return getRandom().nextLong(limit);
    }

    /**
     * Returns a random number within the specified range
     *
     * @param min minimum (inclusive)
     * @param max maximum (exclusive)
     * @return random number
     */
    public static double randomDouble(double min, double max) {
        return getRandom().nextDouble(min, max);
    }

    /**
     * Returns a random number within the specified range
     *
     * @param min          minimum (inclusive)
     * @param max          maximum (exclusive)
     * @param scale        number of decimal places
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return random number
     */
    public static double randomDouble(double min, double max, int scale, RoundingMode roundingMode) {
        return NumberUtils.round(randomDouble(min, max), scale, roundingMode).doubleValue();
    }

    /**
     * Returns a random number in [0, 1)
     *
     * @return random number
     */
    public static double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * Returns a random number within the specified range
     *
     * @param scale        number of decimal places
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return random number
     */
    public static double randomDouble(int scale, RoundingMode roundingMode) {
        return NumberUtils.round(randomDouble(), scale, roundingMode).doubleValue();
    }

    /**
     * Returns a random number in [0, limit)
     *
     * @param limit upper bound (exclusive)
     * @return random number
     */
    public static double randomDouble(double limit) {
        return getRandom().nextDouble(limit);
    }

    /**
     * Returns a random number within the specified range
     *
     * @param limit        upper bound (exclusive)
     * @param scale        number of decimal places
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return random number
     */
    public static double randomDouble(double limit, int scale, RoundingMode roundingMode) {
        return NumberUtils.round(randomDouble(limit), scale, roundingMode).doubleValue();
    }

    /**
     * Returns a random number within the specified range[0, 1)
     *
     * @return random number
     */
    public static BigDecimal randomBigDecimal() {
        return NumberUtils.toBigDecimal(getRandom().nextDouble());
    }

    /**
     * Returns a random number in [0, limit)
     *
     * @param limit maximum (exclusive)
     * @return random number
     */
    public static BigDecimal randomBigDecimal(BigDecimal limit) {
        return NumberUtils.toBigDecimal(getRandom().nextDouble(limit.doubleValue()));
    }

    /**
     * Returns a random number within the specified range
     *
     * @param min minimum (inclusive)
     * @param max maximum (exclusive)
     * @return random number
     */
    public static BigDecimal randomBigDecimal(BigDecimal min, BigDecimal max) {
        return NumberUtils.toBigDecimal(getRandom().nextDouble(min.doubleValue(), max.doubleValue()));
    }

    /**
     * Random bytes
     *
     * @param length length
     * @return bytes
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        getRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * Pick a random element from the list
     *
     * @param <T>  element type
     * @param list the list
     * @return random element
     */
    public static <T> T randomEle(List<T> list) {
        return randomEle(list, list.size());
    }

    /**
     * Pick a random element from the list
     *
     * @param <T>   element type
     * @param list  the list
     * @param limit limit to first N items
     * @return random element
     */
    public static <T> T randomEle(List<T> list, int limit) {
        return list.get(randomInt(limit));
    }

    /**
     * Pick a random element from the array
     *
     * @param <T>   element type
     * @param array the array
     * @return random element
     */
    public static <T> T randomEle(T[] array) {
        return randomEle(array, array.length);
    }

    /**
     * Pick a random element from the array
     *
     * @param <T>   element type
     * @param array the array
     * @param limit limit to first N items
     * @return random element
     */
    public static <T> T randomEle(T[] array, int limit) {
        return array[randomInt(limit)];
    }

    /**
     * Pick a specified number of random elements from the list
     *
     * @param <T>   element type
     * @param list  the list
     * @param count number of elements to pick
     * @return random element
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
     * Pick a specified number of unique random elements, returned as a Set
     *
     * @param <T>        element type
     * @param collection the collection
     * @param count      number of elements to pick
     * @return random element
     * @throws IllegalArgumentException if count exceeds the number of unique elements
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
     * Returns a random alphanumeric string
     *
     * @param length string length
     * @return random string
     */
    public static String randomString(int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * Returns a random string of digits and uppercase letters
     *
     * @param length string length
     * @return random string
     */
    public static String randomStringUpper(int length) {
        return randomString(BASE_CHAR_NUMBER, length).toUpperCase();
    }

    /**
     * Returns a random numeric string
     *
     * @param length string length
     * @return random string
     */
    public static String randomNumbers(int length) {
        return randomString(StringUtils.ARAB_NUMBER, length);
    }

    /**
     * Returns a random string
     *
     * @param baseString character pool
     * @param length     string length
     * @return random string
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
     * Random digit character (0-9)
     *
     * @return random character
     */
    public static int randomNumber() {
        return randomChar(StringUtils.ARAB_NUMBER);
    }

    /**
     * Random lowercase alphanumeric character
     *
     * @return random character
     */
    public static char randomChar() {
        return randomChar(BASE_CHAR_NUMBER);
    }

    /**
     * Random character
     *
     * @param baseString character pool
     * @return random character
     */
    public static char randomChar(String baseString) {
        return baseString.charAt(getRandom().nextInt(baseString.length()));
    }

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
