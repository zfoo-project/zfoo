/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.zfoo.orm.codec;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础类型枚举
 *
 * @author lqh
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
