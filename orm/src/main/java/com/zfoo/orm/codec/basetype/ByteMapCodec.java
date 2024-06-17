package com.zfoo.orm.codec.basetype;


import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:39
 */
public class ByteMapCodec implements MapKeyCodec<Byte> {
    @Override
    public String encode(Byte value) {
        return value.toString();
    }

    @Override
    public Byte decode(String text) {
        return (text == null) ? null : Byte.decode(text);
    }
}
