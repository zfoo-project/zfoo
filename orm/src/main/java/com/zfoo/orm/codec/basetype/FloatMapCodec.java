package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:38
 */
public class FloatMapCodec implements MapKeyCodec<Float> {

    @Override
    public Float decode(String key) {
        return Float.valueOf(key);
    }
}
