package com.zfoo.protocol.util;

/**
 * @author Boone Jack
 */
public abstract class MathSafeUtils {

    public static long MAX_LENGTH = IOUtils.BYTES_PER_MB;
    public static long MAX_LENGTH_SHORT_ARRAY = MAX_LENGTH / 2;
    public static long MAX_LENGTH_INT_ARRAY = MAX_LENGTH / 4;
    public static long MAX_LENGTH_LONG_ARRAY = MAX_LENGTH / 8;

    public static int findNextPositivePowerOfTwo(int value) {
        assert value > Integer.MIN_VALUE && value < IOUtils.BYTES_PER_MB;

        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    public static int safeFindNextPositivePowerOfTwo(int value) {
        return value <= 0 ? 1 : (value >= IOUtils.BYTES_PER_MB ? IOUtils.BYTES_PER_MB : findNextPositivePowerOfTwo(value));
    }
}
