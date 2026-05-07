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

import com.zfoo.protocol.collection.ArrayUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;

/**
 * Number utility class<br>
 * The BigDecimal(double val) constructor is unpredictable, e.g.:
 *
 * <pre>
 * new BigDecimal(0.1)
 * </pre>
 * <p>
 * does not represent 0.1 but 0.1000000000000000055511151231257827021181583404541015625
 * <p>
 * because 0.1 cannot be represented exactly as a double. Use new BigDecimal(String) instead.
 *
 * @author godotg
 */
public abstract class NumberUtils {

    /**
     * Default division scale
     */
    private static final int DEFAULT_DIV_SCALE = 10;


    /**
     * Provides accurate addition<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to add
     * @return sum
     */
    public static BigDecimal add(Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = new BigDecimal(null == value ? "0" : value.toString());
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.add(new BigDecimal(value.toString()));
            }
        }
        return result;
    }

    /**
     * Provides accurate addition<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to add
     * @return sum
     */
    public static BigDecimal add(String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = new BigDecimal(null == value ? "0" : value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.add(new BigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides accurate addition<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to add
     * @return sum
     */
    public static BigDecimal add(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.add(value);
            }
        }
        return result;
    }


    /**
     * Provides accurate subtraction<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to subtract
     * @return difference
     */
    public static BigDecimal sub(Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = new BigDecimal(null == value ? "0" : value.toString());
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.subtract(new BigDecimal(value.toString()));
            }
        }
        return result;
    }

    /**
     * Provides accurate subtraction<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to subtract
     * @return difference
     */
    public static BigDecimal sub(String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = new BigDecimal(null == value ? "0" : value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.subtract(new BigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides accurate subtraction<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to subtract
     * @return difference
     */
    public static BigDecimal sub(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.subtract(value);
            }
        }
        return result;
    }


    /**
     * Provides accurate multiplication<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to multiply
     * @return product
     */
    public static BigDecimal mul(Number... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        Number value = values[0];
        BigDecimal result = new BigDecimal(null == value ? "0" : value.toString());
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.multiply(new BigDecimal(value.toString()));
            }
        }
        return result;
    }

    /**
     * Provides accurate multiplication
     *
     * @param a multiplicand
     * @param b multiplier
     * @return product
     */
    public static BigDecimal mul(String a, String b) {
        return mul(new BigDecimal(a), new BigDecimal(b));
    }

    /**
     * Provides accurate multiplication<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to multiply
     * @return product
     */
    public static BigDecimal mul(String... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        String value = values[0];
        BigDecimal result = new BigDecimal(null == value ? "0" : value);
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.multiply(new BigDecimal(value));
            }
        }
        return result;
    }

    /**
     * Provides accurate multiplication<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to multiply
     * @return product
     */
    public static BigDecimal mul(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.multiply(value);
            }
        }
        return result;
    }


    /**
     * Provides (relatively) accurate division; if indivisible, rounds to 10 decimal places
     *
     * @param a dividend
     * @param b divisor
     * @return quotient
     */
    public static BigDecimal div(Number a, Number b) {
        return div(a, b, DEFAULT_DIV_SCALE);
    }

    /**
     * Provides (relatively) accurate division; if indivisible, rounds to 10 decimal places
     *
     * @param a dividend
     * @param b divisor
     * @return quotient
     */
    public static BigDecimal div(String a, String b) {
        return div(a, b, DEFAULT_DIV_SCALE);
    }


    /**
     * Provides (relatively) accurate division; scale specifies precision; half-up rounding
     *
     * @param a     dividend
     * @param b     divisor
     * @param scale precision; negative value uses its absolute value
     * @return quotient
     */
    public static BigDecimal div(Number a, Number b, int scale) {
        return div(a, b, scale, RoundingMode.HALF_UP);
    }

    /**
     * Provides (relatively) accurate division; scale specifies precision; half-up rounding
     *
     * @param a     dividend
     * @param b     divisor
     * @param scale precision; negative value uses its absolute value
     * @return quotient
     */
    public static BigDecimal div(String a, String b, int scale) {
        return div(a, b, scale, RoundingMode.HALF_UP);
    }

    /**
     * Provides (relatively) accurate division; scale specifies precision
     *
     * @param a            dividend
     * @param b            divisor
     * @param scale        precision; negative value uses its absolute value
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return quotient
     */
    public static BigDecimal div(Number a, Number b, int scale, RoundingMode roundingMode) {
        return div(a.toString(), b.toString(), scale, roundingMode);
    }

    /**
     * Provides (relatively) accurate division; scale specifies precision
     *
     * @param a            dividend
     * @param b            divisor
     * @param scale        precision; negative value uses its absolute value
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return quotient
     */
    public static BigDecimal div(String a, String b, int scale, RoundingMode roundingMode) {
        return div(new BigDecimal(a), new BigDecimal(b), scale, roundingMode);
    }

    /**
     * Provides (relatively) accurate division; scale specifies precision
     *
     * @param a            dividend
     * @param b            divisor
     * @param scale        precision; negative value uses its absolute value
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return quotient
     */
    public static BigDecimal div(BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
        AssertionUtils.notNull(b, "Divisor must be not null !");
        if (null == a) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = -scale;
        }
        return a.divide(b, scale, roundingMode);
    }

    /**
     * Provides accurate multiplication<br>
     * Returns 0 if null or empty values are passed
     *
     * @param values values to multiply
     * @return product
     */
    public static BigDecimal div(BigDecimal... values) {
        if (ArrayUtils.isEmpty(values)) {
            return BigDecimal.ZERO;
        }

        BigDecimal value = values[0];
        BigDecimal result = null == value ? BigDecimal.ZERO : value;
        for (int i = 1; i < values.length; i++) {
            value = values[i];
            if (null != value) {
                result = result.divide(value);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------------------------- round

    /**
     * Retain a fixed number of decimal places<br>
     * Uses {@link RoundingMode#HALF_UP} rounding strategy<br>
     * E.g., 2 decimal places: 123.456789 => 123.46
     *
     * @param v     the value
     * @param scale number of decimal places
     * @return new value
     */
    public static BigDecimal round(double v, int scale) {
        return round(v, scale, RoundingMode.HALF_UP);
    }


    /**
     * Retain a fixed number of decimal places<br>
     * Uses {@link RoundingMode#HALF_UP} rounding strategy<br>
     * E.g., 2 decimal places: 123.456789 => 123.46
     *
     * @param numberStr string representation of the number
     * @param scale     number of decimal places
     * @return new value
     */
    public static BigDecimal round(String numberStr, int scale) {
        return round(numberStr, scale, RoundingMode.HALF_UP);
    }

    /**
     * Retain a fixed number of decimal places<br>
     * Uses {@link RoundingMode#HALF_UP} rounding strategy<br>
     * E.g., 2 decimal places: 123.456789 => 123.46
     *
     * @param number the number value
     * @param scale  number of decimal places
     * @return new value
     */
    public static BigDecimal round(BigDecimal number, int scale) {
        return round(number, scale, RoundingMode.HALF_UP);
    }

    /**
     * Retain a fixed number of decimal places<br>
     * E.g., 4 decimal places: 123.456789 => 123.4567
     *
     * @param v            the value
     * @param scale        number of decimal places
     * @param roundingMode rounding mode {@link RoundingMode}
     * @return new value
     */
    public static BigDecimal round(double v, int scale, RoundingMode roundingMode) {
        return round(Double.toString(v), scale, roundingMode);
    }

    /**
     * Retain a fixed number of decimal places<br>
     * E.g., 4 decimal places: 123.456789 => 123.4567
     *
     * @param numberStr    string representation of the number
     * @param scale        number of decimal places; defaults to 0 if negative
     * @param roundingMode rounding mode {@link RoundingMode}; defaults to HALF_UP if null
     * @return new value
     */
    public static BigDecimal round(String numberStr, int scale, RoundingMode roundingMode) {
        AssertionUtils.isTrue(StringUtils.isNotBlank(numberStr));
        if (scale < 0) {
            scale = 0;
        }
        return round(toBigDecimal(numberStr), scale, roundingMode);
    }

    /**
     * Retain a fixed number of decimal places<br>
     * E.g., 4 decimal places: 123.456789 => 123.4567
     *
     * @param number       the number
     * @param scale        number of decimal places; defaults to 0 if negative
     * @param roundingMode rounding mode {@link RoundingMode}; defaults to HALF_UP if null
     * @return new value
     */
    public static BigDecimal round(BigDecimal number, int scale, RoundingMode roundingMode) {
        if (null == number) {
            number = BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = 0;
        }
        if (null == roundingMode) {
            roundingMode = RoundingMode.HALF_UP;
        }

        return number.setScale(scale, roundingMode);
    }


    /**
     * Banker's rounding (round-half-to-even)
     * <p>
     * Banker's rounding is a more accurate and scientific rounding rule.
     * </p>
     *
     * <pre>
     * Algorithm rules:
     * 4 rounds down, 6 rounds up, 5 considers parity:
     * if there are non-zero digits after 5, round up;
     * if digits after 5 are all zero, check parity of the preceding digit.
     * If the digit before 5 is even, round down,
     * if it is odd, round up.
     * </pre>
     *
     * @param number the numeric value
     * @param scale  number of decimal places to retain
     * @return result
     */
    public static BigDecimal roundHalfEven(Number number, int scale) {
        return roundHalfEven(toBigDecimal(number), scale);
    }

    /**
     * Banker's rounding (round-half-to-even)
     * <p>
     * Banker's rounding is a more accurate and scientific rounding rule.
     * </p>
     *
     * <pre>
     * Algorithm rules:
     * 4 rounds down, 6 rounds up, 5 considers parity:
     * if there are non-zero digits after 5, round up;
     * if digits after 5 are all zero, check parity of the preceding digit.
     * If the digit before 5 is even, round down,
     * if it is odd, round up.
     * </pre>
     *
     * @param value the numeric value
     * @param scale number of decimal places to retain
     * @return result
     */
    public static BigDecimal roundHalfEven(BigDecimal value, int scale) {
        return round(value, scale, RoundingMode.HALF_EVEN);
    }

    /**
     * Retain a fixed number of decimal places, discarding excess digits
     *
     * @param number the numeric value
     * @param scale  number of decimal places to retain
     * @return result
     */
    public static BigDecimal roundDown(Number number, int scale) {
        return roundDown(toBigDecimal(number), scale);
    }

    /**
     * Retain a fixed number of decimal places, discarding excess digits
     *
     * @param value the numeric value
     * @param scale number of decimal places to retain
     * @return result
     */
    public static BigDecimal roundDown(BigDecimal value, int scale) {
        return round(value, scale, RoundingMode.DOWN);
    }

    // ------------------------------------------------------------------------------------------- decimalFormat

    /**
     * Format a double value<br>
     * Wraps {@link DecimalFormat}<br>
     *
     * @param pattern format string using # and 0 as placeholders. 0 pads with zeros; # shows digits only if present.<br>
     *                <ul>
     *                <li>0 =&gt; one integer digit</li>
     *                <li>0.00 =&gt; one integer digit and two decimal places</li>
     *                <li>00.000 =&gt; two integer digits and three decimal places</li>
     *                <li># =&gt; all integer digits</li>
     *                <li>#.##% =&gt; percentage with two decimal places</li>
     *                <li>#.#####E0 =&gt; scientific notation with five decimal places</li>
     *                <li>,### =&gt; comma-separated thousands, e.g. 299,792,458</li>
     *                <li>Speed of light is ,### metres per second =&gt; embed format in text</li>
     *                </ul>
     * @param value   the value
     * @return formatted value
     */
    public static String decimalFormat(String pattern, double value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Format a double value<br>
     * Wraps {@link DecimalFormat}<br>
     *
     * @param pattern format string using # and 0 as placeholders. 0 pads with zeros; # shows digits only if present.<br>
     *                <ul>
     *                <li>0 =&gt; one integer digit</li>
     *                <li>0.00 =&gt; one integer digit and two decimal places</li>
     *                <li>00.000 =&gt; two integer digits and three decimal places</li>
     *                <li># =&gt; all integer digits</li>
     *                <li>#.##% =&gt; percentage with two decimal places</li>
     *                <li>#.#####E0 =&gt; scientific notation with five decimal places</li>
     *                <li>,### =&gt; comma-separated thousands, e.g. 299,792,458</li>
     *                <li>Speed of light is ,### metres per second =&gt; embed format in text</li>
     *                </ul>
     * @param value   the value
     * @return formatted value
     */
    public static String decimalFormat(String pattern, long value) {
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Format a monetary amount with comma-separated thousands
     *
     * @param value the amount
     * @return formatted value
     */
    public static String decimalFormatMoney(double value) {
        return decimalFormat(",##0.00", value);
    }

    /**
     * Format as a percentage; decimals are rounded with half-up
     *
     * @param number the value
     * @param scale  number of decimal places
     * @return percentage string
     */
    public static String formatPercent(double number, int scale) {
        final NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(scale);
        return format.format(number);
    }

    // ------------------------------------------------------------------------------------------- isXXX

    /**
     * Check if a String is an integer<br>
     * Supports octal, decimal, and hexadecimal
     *
     * @param s String
     * @return true if the string is an integer
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Check if a String represents a Long value<br>
     * Supports octal, decimal, and hexadecimal
     *
     * @param s String
     * @return true if the string is a {@link Long}
     */
    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Check if a String is a floating-point number
     *
     * @param s String
     * @return true if the string is a {@link Double}
     */
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return s.contains(".");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if all characters of the string are digits
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = false
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * StringUtils.isNumeric("-123") = false
     * StringUtils.isNumeric("+123") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if only contains digits, and is non-null
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (StringUtils.isEmpty(cs)) {
            return false;
        }
        return cs.chars().allMatch(it -> Character.isDigit(it));
    }

    /**
     * Check if a number is prime<br>
     * A prime (or prime number) is a natural number greater than 1 that cannot be divided evenly by any integer other than 1 and itself.
     *
     * @param n the number
     * @return true if the number is prime
     */
    public static boolean isPrimes(int n) {
        AssertionUtils.isTrue(n > 1, "The number must be > 1");
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }


    // ------------------------------------------------------------------------------------------- range

    /**
     * Integer list from 0 to stop (inclusive), step 1
     *
     * @param stop end (inclusive)
     * @return list of integers
     */
    public static int[] range(int stop) {
        return range(0, stop);
    }

    /**
     * Integer list from start to stop (inclusive), step 1
     *
     * @param start start (inclusive)
     * @param stop  end (inclusive)
     * @return list of integers
     */
    public static int[] range(int start, int stop) {
        return range(start, stop, 1);
    }

    /**
     * Integer list within the given range
     *
     * @param start start (inclusive)
     * @param stop  end (inclusive)
     * @param step  step
     * @return list of integers
     */
    public static int[] range(int start, int stop, int step) {
        if (start < stop) {
            step = Math.abs(step);
        } else if (start > stop) {
            step = -Math.abs(step);
        } else {// start == end
            return new int[]{start};
        }

        int size = Math.abs((stop - start) / step) + 1;
        int[] values = new int[size];
        int index = 0;
        for (int i = start; (step > 0) ? i <= stop : i >= stop; i += step) {
            values[index] = i;
            index++;
        }
        return values;
    }

    /**
     * Add integers in range to the given collection, step 1
     *
     * @param start  start (inclusive)
     * @param stop   end (inclusive)
     * @param values the collection to add to
     * @return the collection
     */
    public static Collection<Integer> appendRange(int start, int stop, Collection<Integer> values) {
        return appendRange(start, stop, 1, values);
    }

    /**
     * Add integers in range to the given collection
     *
     * @param start  start (inclusive)
     * @param stop   end (inclusive)
     * @param step   step
     * @param values the collection to add to
     * @return the collection
     */
    public static Collection<Integer> appendRange(int start, int stop, int step, Collection<Integer> values) {
        if (start < stop) {
            step = Math.abs(step);
        } else if (start > stop) {
            step = -Math.abs(step);
        } else {// start == end
            values.add(start);
            return values;
        }

        for (int i = start; (step > 0) ? i <= stop : i >= stop; i += step) {
            values.add(i);
        }
        return values;
    }

    // ------------------------------------------------------------------------------------------- others

    /**
     * Calculate factorial
     * <p>
     * n! = n * (n-1) * ... * end
     * </p>
     *
     * @param start factorial start
     * @param end   factorial end
     * @return result
     */
    public static long factorial(long start, long end) {
        if (start < end) {
            return 0L;
        }
        if (start == end) {
            return 1L;
        }
        return start * factorial(start - 1, end);
    }

    /**
     * Calculate factorial
     * <p>
     * n! = n * (n-1) * ... * 2 * 1
     * </p>
     *
     * @param n factorial start
     * @return result
     */
    public static long factorial(long n) {
        return factorial(n, 1);
    }

    /**
     * Square root algorithm<br>
     * Prefer using {@link Math#sqrt(double)}
     *
     * @param x the value
     * @return square root
     */
    public static long sqrt(long x) {
        long y = 0;
        long b = (~Long.MAX_VALUE) >>> 1;
        while (b > 0) {
            if (x >= y + b) {
                x -= y + b;
                y >>= 1;
                y += b;
            } else {
                y >>= 1;
            }
            b >>= 2;
        }
        return y;
    }

    /**
     * Greatest common divisor
     *
     * @param m first value
     * @param n second value
     * @return Greatest common divisor
     */
    public static int divisor(int m, int n) {
        while (m % n != 0) {
            int temp = m % n;
            m = n;
            n = temp;
        }
        return n;
    }

    /**
     * Least common multiple
     *
     * @param m first value
     * @param n second value
     * @return Least common multiple
     */
    public static int multiple(int m, int n) {
        return m * n / divisor(m, n);
    }

    /**
     * Get the binary string representation of a number
     *
     * @param number the number
     * @return binary string
     */
    public static String getBinaryStr(Number number) {
        if (number instanceof Long) {
            return Long.toBinaryString((Long) number);
        } else if (number instanceof Integer) {
            return Integer.toBinaryString((Integer) number);
        } else {
            return Long.toBinaryString(number.longValue());
        }
    }

    /**
     * Parse a binary string to int
     *
     * @param binaryStr binary string
     * @return int
     */
    public static int binaryToInt(String binaryStr) {
        return Integer.parseInt(binaryStr, 2);
    }

    /**
     * Parse a binary string to long
     *
     * @param binaryStr binary string
     * @return long
     */
    public static long binaryToLong(String binaryStr) {
        return Long.parseLong(binaryStr, 2);
    }

    // ------------------------------------------------------------------------------------------- compare


    /**
     * Convert a number to {@link BigDecimal}
     *
     * @param number the number
     * @return {@link BigDecimal}
     */
    public static BigDecimal toBigDecimal(Number number) {
        if (null == number) {
            return BigDecimal.ZERO;
        }
        return toBigDecimal(number.toString());
    }

    /**
     * Convert a number to {@link BigDecimal}
     *
     * @param number the number
     * @return {@link BigDecimal}
     */
    public static BigDecimal toBigDecimal(String number) {
        return (null == number) ? BigDecimal.ZERO : new BigDecimal(number);
    }


    /**
     * Check if two numbers are adjacent (differ by exactly 1)<br>
     * Method: check that the absolute difference equals 1
     *
     * @param number1 first number
     * @param number2 second number
     * @return true if the numbers are adjacent
     */
    public static boolean isBeside(long number1, long number2) {
        return Math.abs(number1 - number2) == 1;
    }

    /**
     * Check if two numbers are adjacent (differ by exactly 1)<br>
     * Method: check that the absolute difference equals 1
     *
     * @param number1 first number
     * @param number2 second number
     * @return true if the numbers are adjacent
     */
    public static boolean isBeside(int number1, int number2) {
        return Math.abs(number1 - number2) == 1;
    }


    /**
     * Provides accurate power calculation
     *
     * @param number base
     * @param n      exponent
     * @return the power result
     */
    public static BigDecimal pow(Number number, int n) {
        return pow(toBigDecimal(number), n);
    }

    /**
     * Provides accurate power calculation
     *
     * @param number base
     * @param n      exponent
     * @return the power result
     */
    public static BigDecimal pow(BigDecimal number, int n) {
        return number.pow(n);
    }


}
