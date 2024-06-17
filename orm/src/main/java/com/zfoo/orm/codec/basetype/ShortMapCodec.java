package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:38
 */
public class ShortMapCodec implements MapKeyCodec<Short> {
    @Override
    public Short decode(String key) {
        return Short.valueOf(key);
    }
}
