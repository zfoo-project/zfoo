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

package com.zfoo.protocol.collection;

import com.zfoo.protocol.util.AssertionUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author godotg
 */
public abstract class ArrayUtils {

    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * unbox
     */
    public static boolean booleanValue(Boolean value) {
        return value != null && value;
    }

    public static byte byteValue(Number value) {
        return value == null ? 0 : value.byteValue();
    }

    public static short shortValue(Number value) {
        return value == null ? 0 : value.shortValue();
    }

    public static int intValue(Number value) {
        return value == null ? 0 : value.intValue();
    }

    public static long longValue(Number value) {
        return value == null ? 0 : value.longValue();
    }

    public static float floatValue(Number value) {
        return value == null ? 0 : value.floatValue();
    }

    public static double doubleValue(Number value) {
        return value == null ? 0 : value.doubleValue();
    }

    public static char charValue(Character value) {
        return value == null ? Character.MIN_VALUE : value;
    }

    /**
     * length
     */
    public static int length(boolean[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(byte[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(short[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(int[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(long[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(float[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(double[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(char[] array) {
        return array == null ? 0 : array.length;
    }

    public static <T> int length(T[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * isEmpty
     */
    public static boolean isEmpty(boolean[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(byte[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(short[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(int[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(long[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(float[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(double[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(char[] array) {
        return (array == null || array.length == 0);
    }

    public static <T> boolean isEmpty(T[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * isNotEmpty
     */
    public static boolean isNotEmpty(boolean[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(byte[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(short[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(int[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(long[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(float[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(double[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(char[] array) {
        return !isEmpty(array);
    }

    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }


    /**
     * toList
     */
    public static List<Boolean> toList(boolean[] array) {
        var list = new ArrayList<Boolean>();
        for (var value : array) {
            list.add(value);
        }
        return list;
    }

    public static List<Byte> toList(byte[] array) {
        var list = new ArrayList<Byte>();
        for (var value : array) {
            list.add(value);
        }
        return list;
    }

    public static List<Short> toList(short[] array) {
        var list = new ArrayList<Short>();
        for (var value : array) {
            list.add(value);
        }
        return list;
    }

    public static List<Integer> toList(int[] array) {
        var list = new ArrayList<Integer>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Long> toList(long[] array) {
        var list = new ArrayList<Long>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Float> toList(float[] array) {
        var list = new ArrayList<Float>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Double> toList(double[] array) {
        var list = new ArrayList<Double>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Character> toList(char[] array) {
        var list = new ArrayList<Character>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static <T> List<T> toList(T[] array) {
        if (isEmpty(array)) {
            return CollectionUtils.emptyList();
        }
        return new ArrayList<>(Arrays.asList(array));
    }


    /**
     * toArray
     */
    public static boolean[] booleanToArray(List<Boolean> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        var size = list.size();
        var array = new boolean[size];
        for (var i = 0; i < size; i++) {
            array[i] = booleanValue(list.get(i));
        }
        return array;
    }

    public static byte[] byteToArray(List<Byte> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_BYTE_ARRAY;
        }
        var size = list.size();
        var array = new byte[size];
        for (var i = 0; i < size; i++) {
            array[i] = byteValue(list.get(i));
        }
        return array;
    }

    public static short[] shortToArray(List<Short> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_SHORT_ARRAY;
        }
        var size = list.size();
        var array = new short[size];
        for (var i = 0; i < size; i++) {
            array[i] = shortValue(list.get(i));
        }
        return array;
    }

    public static int[] intToArray(List<Integer> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_INT_ARRAY;
        }
        var size = list.size();
        var array = new int[size];
        for (var i = 0; i < size; i++) {
            array[i] = intValue(list.get(i));
        }
        return array;
    }


    public static long[] longToArray(List<Long> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_LONG_ARRAY;
        }
        var size = list.size();
        var array = new long[size];
        for (var i = 0; i < size; i++) {
            array[i] = longValue(list.get(i));
        }
        return array;
    }

    public static float[] floatToArray(List<Float> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_FLOAT_ARRAY;
        }
        var size = list.size();
        var array = new float[size];
        for (var i = 0; i < size; i++) {
            array[i] = floatValue(list.get(i));
        }
        return array;
    }

    public static double[] doubleToArray(List<Double> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_DOUBLE_ARRAY;
        }
        var size = list.size();
        var array = new double[size];
        for (var i = 0; i < size; i++) {
            array[i] = doubleValue(list.get(i));
        }
        return array;
    }

    public static char[] charToArray(List<Character> list) {
        if (CollectionUtils.isEmpty(list)) {
            return EMPTY_CHAR_ARRAY;
        }
        var size = list.size();
        var array = new char[size];
        for (var i = 0; i < size; i++) {
            array[i] = charValue(list.get(i));
        }
        return array;
    }

    public static <T> T[] listToArray(List<T> list, Class<T> clazz) {
        AssertionUtils.notNull(list);
        AssertionUtils.notNull(clazz);

        var length = list.size();
        var objectArray = Array.newInstance(clazz, length);
        @SuppressWarnings("unchecked")
        var copy = (T[]) copy(list.toArray(), objectArray, length);
        return copy;
    }


    /**
     * copy
     */
    public static boolean[] copy(boolean[] source) {
        if (isEmpty(source)) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        var length = source.length;
        var target = new boolean[length];
        return (boolean[]) copy(source, target, length);
    }

    public static byte[] copy(byte[] source) {
        if (isEmpty(source)) {
            return EMPTY_BYTE_ARRAY;
        }
        var length = source.length;
        var target = new byte[length];
        return (byte[]) copy(source, target, length);
    }

    public static short[] copy(short[] source) {
        if (isEmpty(source)) {
            return EMPTY_SHORT_ARRAY;
        }
        var length = source.length;
        var target = new short[length];
        return (short[]) copy(source, target, length);
    }

    public static int[] copy(int[] source) {
        if (isEmpty(source)) {
            return EMPTY_INT_ARRAY;
        }
        var length = source.length;
        var target = new int[length];
        return (int[]) copy(source, target, length);
    }

    public static long[] copy(long[] source) {
        if (isEmpty(source)) {
            return EMPTY_LONG_ARRAY;
        }
        var length = source.length;
        var target = new long[length];
        return (long[]) copy(source, target, length);
    }

    public static float[] copy(float[] source) {
        if (isEmpty(source)) {
            return EMPTY_FLOAT_ARRAY;
        }
        var length = source.length;
        var target = new float[length];
        return (float[]) copy(source, target, length);
    }

    public static double[] copy(double[] source) {
        if (isEmpty(source)) {
            return EMPTY_DOUBLE_ARRAY;
        }
        var length = source.length;
        var target = new double[length];
        return (double[]) copy(source, target, length);
    }

    public static char[] copy(char[] source) {
        if (isEmpty(source)) {
            return EMPTY_CHAR_ARRAY;
        }
        var length = source.length;
        var target = new char[length];
        return (char[]) copy(source, target, length);
    }

    public static <T> T[] copy(T[] source, Class<T> clazz) {
        AssertionUtils.notNull(source);
        AssertionUtils.notNull(clazz);
        var length = source.length;
        var target = Array.newInstance(clazz, length);
        @SuppressWarnings("unchecked")
        var copy = (T[]) copy(source, target, length);
        return copy;
    }

    private static Object copy(Object source, Object target, int length) {
        System.arraycopy(source, 0, target, 0, length);
        return target;
    }
}
