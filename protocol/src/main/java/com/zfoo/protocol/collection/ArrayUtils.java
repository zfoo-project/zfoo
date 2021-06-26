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
import java.util.Collections;
import java.util.List;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class ArrayUtils {
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
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Boolean>();
        for (var value : array) {
            list.add(value);
        }
        return list;
    }

    public static List<Byte> toList(byte[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Byte>();
        for (var value : array) {
            list.add(value);
        }
        return list;
    }

    public static List<Short> toList(short[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Short>();
        for (var value : array) {
            list.add(value);
        }
        return list;
    }

    public static List<Integer> toList(int[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Integer>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Long> toList(long[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Long>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Float> toList(float[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Float>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Double> toList(double[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Double>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static List<Character> toList(char[] array) {
        if (array == null || array.length == 0) {
            return Collections.emptyList();
        }
        var list = new ArrayList<Character>();
        for (var j : array) {
            list.add(j);
        }
        return list;
    }

    public static <T> List<T> toList(T[] array) {
        if (CollectionUtils.isEmpty(array)) {
            return Collections.emptyList();
        }
        return Arrays.asList(array);
    }


    public static <T> T[] listToArray(List<T> list, Class<T> clazz) {
        AssertionUtils.notNull(list);
        AssertionUtils.notNull(clazz);

        var length = list.size();
        var objectArray = Array.newInstance(clazz, length);

        System.arraycopy(list.toArray(), 0, objectArray, 0, length);
        return (T[]) objectArray;
    }

}
