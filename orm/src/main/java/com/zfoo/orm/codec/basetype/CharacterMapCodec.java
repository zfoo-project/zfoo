package com.zfoo.orm.codec.basetype;

import com.zfoo.orm.codec.MapKeyCodec;
import org.springframework.util.StringUtils;


/**
 * @Author：lqh
 * @Date：2024/6/17 11:34
 */
public class CharacterMapCodec implements MapKeyCodec<Character> {
    @Override
    public String encode(Character value) {
        return value.toString();
    }

    @Override
    public Character decode(String text) {
        if (!StringUtils.hasLength(text)) {
           return null;
        } else {
            return text.charAt(0);
        }
    }
}
