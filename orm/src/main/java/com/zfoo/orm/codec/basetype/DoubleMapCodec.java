package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:35
 */
public class DoubleMapCodec implements MapKeyCodec<Double> {

    @Override
    public Double decode(String key) {
        return Double.valueOf(key);
    }
}
