package com.zfoo.orm.codec;

/**
 * map key编码接口
 * @Author：lqh
 * @Date：2024/6/17 13:51
 */
public interface MapKeyCodec<K> {
    /**
     * 编码
     * @param value
     * @return
     */
    String encode(K value);

    /**
     * 解析
     * @param text
     * @return
     */
    K decode(String text);
}
