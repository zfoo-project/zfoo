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


import com.zfoo.protocol.exception.AssertException;

import java.util.Collection;
import java.util.Map;

/**
 * Assertion utility class that assists in validating arguments.
 * This class is similar to JUnit's assertion library. If an argument value is
 * deemed invalid, an {@link IllegalArgumentException} is thrown (typically).
 *
 * @author godotg
 * @version 3.0
 */
public abstract class AssertionUtils {

    // ----------------------------------bool----------------------------------

    /**
     * Assert a boolean expression, throwing {@code IllegalArgumentException}
     * if the test result is {@code false}.
     *
     * @param expression a boolean expression
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new AssertException(message);
        }
    }

    /**
     * 可支持带参数format的类型：类{}的成员变量：{}不能有set方法：{}
     *
     * @param expression 表达式
     * @param format     格式
     * @param args       参数
     */
    public static void isTrue(boolean expression, String format, Object... args) {
        if (!expression) {
            throw new AssertException(format, args);
        }
    }

    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }


    // ----------------------------------long----------------------------------

    /**
     * lt 参数1是否小于参数2
     * le 参数1是否小于等于参数2
     * gt 参数1是否大于参数2
     * ge 参数1是否大于等于参数2
     */
    public static void ge(long x, long y) {
        if (x < y) {
            throw new AssertException("[Assertion failed] - the param [x:{}] must greater or equal [y:{}]", x, y);
        }
    }

    public static void le(long x, long y) {
        if (x > y) {
            throw new AssertException("[Assertion failed] - the param [x:{}] must less or equal [y:{}]", x, y);
        }
    }

    public static void ge0(long x) {
        ge(x, 0);
    }

    public static void ge1(long x) {
        ge(x, 1);
    }

    public static void le0(long x) {
        le(x, 0);
    }

    public static void le1(long x) {
        le(x, 1);
    }


    // ----------------------------------collection----------------------------------

    /**
     * Assert that a collection has elements; that is, it must not be
     * {@code null} and must have at least one element.
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new AssertException(message);
        }
    }

    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection,
                "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    /**
     * Assert that a Map has entries; that is, it must not be {@code null}
     * and must have at least one entry.
     *
     * @param map     the map to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new AssertException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
    }


    // ----------------------------------object----------------------------------

    /**
     * Assert that an object is {@code null} .
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new AssertException(message);
        }
    }

    public static void isNull(Object object, String format, Object... args) {
        if (object != null) {
            throw new AssertException(format, args);
        }
    }

    public static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new AssertException(message);
        }
    }

    public static void notNull(Object object, String format, Object... args) {
        if (object == null) {
            throw new AssertException(format, args);
        }
    }

    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void notNull(Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            notNull(objects[i], "the [index:{}] of objects must not be null", i);
        }
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     *
     * @param type    the type to check against
     * @param obj     the object to check
     * @param message a message which will be prepended to the message produced by
     *                the function itself, and which may be used to provide context. It should
     *                normally end in ":" or "." so that the generated message looks OK when
     *                appended to it.
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance
     */
    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new AssertException(
                    (StringUtils.isNotBlank(message) ? message + " " : "") +
                            "Object of class [" + (obj != null ? obj.getClass().getName() : "null") +
                            "] must be an instance of " + type);
        }
    }

    public static void isInstanceOf(Class<?> clazz, Object obj) {
        isInstanceOf(clazz, obj, "");
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
     *
     * @param superType the super type to check against
     * @param subType   the sub type to check
     * @param message   a message which will be prepended to the message produced by
     *                  the function itself, and which may be used to provide context. It should
     *                  normally end in ":" or "." so that the generated message looks OK when
     *                  appended to it.
     * @throws IllegalArgumentException if the classes are not assignable
     */
    public static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new AssertException((StringUtils.isNotBlank(message) ? message + " " : "")
                    + subType + " is not assignable to " + superType);
        }
    }

    public static void isAssignable(Class<?> superType, Class<?> subType) {
        isAssignable(superType, subType, "");
    }

}
