package com.zfoo.storage.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UtilsNumber {
    Logger log = LoggerFactory.getLogger(UtilsNumber.class);

    /**
     * 拆箱，null为0
     */
    static int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 拆箱，null为0
     */
    static float floatValue(Float value) {
        return value == null ? 0f : value;
    }

    /**
     * String转为boolean型
     * 如果出错 则为false
     */
    static boolean booleanValue(String value) {
        return "true".equalsIgnoreCase(value);
    }

    /**
     * 从byte数组中，指定下标开始，读出一个int
     */
    static int bytesToInt(byte[] b, int offset) {
        int num = 0;
        for (int i = offset; i < offset + 4; ++i) {
            num <<= 8;
            num |= (b[i] & 0xff);
        }
        return num;
    }

    /**
     * long型百分比转换为实际百分比
     *
     * @param pctLong .
     * @return .
     */
    static double pctFromLong(long pctLong) throws Exception {
        // 百分比转换比例
        double ratio = 100_000;
        // 转换为实际百分比
        double pct = pctLong / ratio;

        // 结果不是有限浮点数, 抛出异常
        if (!Double.isFinite(pct)) {
            log.error("long型百分比转换为实际百分比错误, pctLong={}, ratio={}, pct={}",
                      pctLong,
                      ratio,
                      pct);
            throw new Exception();
        }

        return pct;
    }
}
