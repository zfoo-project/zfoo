package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;


/**
 * @Author：lqh
 * @Date：2024/6/17 11:34
 */
public class CharacterMapCodec implements MapKeyCodec<Character> {

    @Override
    public Character decode(String key) {
        return key.charAt(0);
    }

}
