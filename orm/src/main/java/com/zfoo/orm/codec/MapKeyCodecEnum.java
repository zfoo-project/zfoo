package com.zfoo.orm.codec;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础类型枚举
 *
 * @Author：lqh
 * @Date：2024/6/17 11:41
 */
public enum MapKeyCodecEnum {

    char_key(Character.class, (Function<String, Character>) key -> key.charAt(0)),

    bool_key(Boolean.class, (Function<String, Boolean>) Boolean::valueOf),

    byte_key(Byte.class, (Function<String, Byte>) Byte::valueOf),

    short_key(Short.class, (Function<String, Short>) Short::valueOf),

    int_key(Integer.class, (Function<String, Integer>) Integer::valueOf),

    long_key(Long.class, (Function<String, Long>) Long::valueOf),

    float_key(Float.class, (Function<String, Float>) Float::valueOf),

    double_key(Double.class, (Function<String, Double>) Double::valueOf),

    ;

    public final Class<?> keyType;

    public final Function<String, ?> keyDecodeFunction;


    MapKeyCodecEnum(Class<?> keyType, Function<String, ?> keyDecodeFunction) {
        this.keyType = keyType;
        this.keyDecodeFunction = keyDecodeFunction;
    }

    public Class<?> keyType() {
        return keyType;
    }

    public Function<String, ?> keyDecodeFunction() {
        return keyDecodeFunction;
    }

    public static final Map<Class<?>, Function<String, ?>> keyDecodeMap = Arrays.stream(values()).collect(Collectors.toMap(MapKeyCodecEnum::keyType, MapKeyCodecEnum::keyDecodeFunction));

    public static boolean containsKeyDecode(Class<?> keyClass) {
        return keyDecodeMap.containsKey(keyClass);
    }

    public static Function<String, ?> keyDecode(Class<?> clazz) {
        return keyDecodeMap.get(clazz);
    }
}
