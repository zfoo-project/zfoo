package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:38
 */
public class ShortMapCodec<V> implements MapKeyCodec<Short> {
    @Override
    public String encode(Short value) {
        return (value != null)
                ? value.toString()
                : null;
    }

    @Override
    public Short decode(String text) {
        return (text == null) ? null : Short.parseShort(text);
    }
}
