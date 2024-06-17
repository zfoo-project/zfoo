package com.zfoo.orm.codec;

import com.zfoo.orm.codec.basetype.*;

/**
 * 基础类型枚举
 * @Author：lqh
 * @Date：2024/6/17 11:41
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public enum BaseTypeEnum {
    Float(java.lang.Float.class, new FloatMapCodec()),
    Integer(java.lang.Integer.class, new IntMapCodec()),
    Double(java.lang.Double.class, new DoubleMapCodec()),
    Character(java.lang.Character.class, new CharacterMapCodec()),
    Byte(java.lang.Byte.class, new ByteMapCodec()),
    Boolean(java.lang.Boolean.class, new BooleanMapCodec()),
    Long(java.lang.Long.class, new LongMapCodec()),
    Short(java.lang.Short.class, new ShortMapCodec()),
    ;
    private final MapKeyCodec mapKeyCodec;

    private final Class<?> clazz;

    BaseTypeEnum(Class<?> clazz, MapKeyCodec mapKeyCodec) {
        this.clazz = clazz;
        this.mapKeyCodec = mapKeyCodec;
    }

    public MapKeyCodec getMapKeyCodec() {
        return mapKeyCodec;
    }

    public Class<?> getClazz() {
        return clazz;
    }
    public static boolean containsKeyType(Class<?> keyClass){
        for (BaseTypeEnum typeEnum : values()) {
            if (typeEnum.getClazz() == keyClass) {
                return true;
            }
        }
        return false;
    }
    public static MapKeyCodec getCodec(Class<?> clazz) {
        for (BaseTypeEnum typeEnum : values()) {
            if (typeEnum.getClazz() == clazz) {
                return typeEnum.getMapKeyCodec();
            }
        }
        return null;
    }
}
