package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * @Author：lqh
 * @Date：2024/6/17 11:32
 */
public class BooleanMapCodec implements MapKeyCodec<Boolean> {
    @Override
    public Boolean decode(String key) {
        return Boolean.valueOf(key);
    }

}
