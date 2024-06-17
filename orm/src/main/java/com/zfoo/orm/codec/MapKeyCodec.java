package com.zfoo.orm.codec;

/**
 * map key编码接口
 * @Author：lqh
 * @Date：2024/6/17 13:51
 */
public interface MapKeyCodec<K> {
    /**
     * 解析
     */
    K decode(String key);
}
