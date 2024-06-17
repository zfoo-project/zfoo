package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;

/**
 * map解析器
 *
 * @Author：lqh
 * @Date：2024/6/14 10:32
 */
public class LongMapCodec implements MapKeyCodec<Long> {

    @Override
    public Long decode(String key) {
        return Long.valueOf(key);
    }
}
