package com.zfoo.orm.codec.basetype;


import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:39
 */
public class ByteMapCodec implements MapKeyCodec<Byte> {
    @Override
    public Byte decode(String key) {
        return Byte.valueOf(key);
    }
}
