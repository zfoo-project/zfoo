package com.zfoo.orm.codec;

import com.zfoo.orm.codec.basetype.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基础类型枚举
 * @Author：lqh
 * @Date：2024/6/17 11:41
 */
public enum BaseTypeEnum {

    Character(Character.class, new CharacterMapCodec()),

    Boolean(Boolean.class, new BooleanMapCodec()),

    Byte(Byte.class, new ByteMapCodec()),

    Short(Short.class, new ShortMapCodec()),

    Integer(Integer.class, new IntMapCodec()),

    Long(Long.class, new LongMapCodec()),

    Float(Float.class, new FloatMapCodec()),

    Double(Double.class, new DoubleMapCodec()),

    ;

    private final Class<?> clazz;

    private final MapKeyCodec<? extends Serializable> mapKeyCodec;


    BaseTypeEnum(Class<?> clazz, MapKeyCodec<? extends Serializable> mapKeyCodec) {
        this.clazz = clazz;
        this.mapKeyCodec = mapKeyCodec;
    }

    public static final Map<Class<?>, MapKeyCodec<? extends Serializable>> baseTypeMap = Arrays.stream(values()).collect(Collectors.toMap(BaseTypeEnum::getClazz, BaseTypeEnum::getMapKeyCodec));


    public Class<?> getClazz() {
        return clazz;
    }

    public MapKeyCodec<? extends Serializable> getMapKeyCodec() {
        return mapKeyCodec;
    }

    public static boolean containsKeyType(Class<?> keyClass){
        return baseTypeMap.containsKey(keyClass);
    }

    public static MapKeyCodec<? extends Serializable> getCodec(Class<?> clazz) {
        return baseTypeMap.get(clazz);
    }
}
