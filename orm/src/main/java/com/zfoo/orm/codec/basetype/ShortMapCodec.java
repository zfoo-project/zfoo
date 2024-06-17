package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:38
 */
public class ShortMapCodec implements MapKeyCodec<Short> {
    @Override
    public String encode(Short value) {
        return value.toString();
    }

    @Override
    public Short decode(String text) {
        return (text == null) ? null : Short.parseShort(text);
    }
}
