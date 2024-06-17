package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * map解析器
 *
 * @Author：lqh
 * @Date：2024/6/14 10:32
 */
public class IntMapCodec implements MapKeyCodec<Integer> {

    @Override
    public Integer decode(String key) {
        return Integer.valueOf(key);
    }
}
