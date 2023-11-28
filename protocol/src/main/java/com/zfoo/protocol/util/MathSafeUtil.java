package com.zfoo.protocol.util;

/**
 * @author Boone Jack
 */
public abstract class MathSafeUtil {

    public static int findNextPositivePowerOfTwo(int value) {
        assert value > Integer.MIN_VALUE && value < IOUtils.BYTES_PER_MB;

        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    public static int safeFindNextPositivePowerOfTwo(int value) {
        return value <= 0 ? 1 : (value >= IOUtils.BYTES_PER_MB ? IOUtils.BYTES_PER_MB : findNextPositivePowerOfTwo(value));
    }
}
