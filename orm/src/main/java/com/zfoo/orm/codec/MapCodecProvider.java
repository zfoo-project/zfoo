package com.zfoo.orm.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

/**
 * @Author：lqh
 * @Date：2024/6/14 10:23
 */
public class MapCodecProvider implements PropertyCodecProvider {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
        if (!Map.class.isAssignableFrom(type.getType())) {
            return null;
        }
        var typeParameters = type.getTypeParameters();
        if (type.getTypeParameters().size() != 2) {
            return null;
        }
        var keyType = typeParameters.get(0);
        var valueType = typeParameters.get(1);
        if (keyType.getType() == Integer.class) {
            return new IntMapCodec(type.getType(), registry.get(keyType), registry.get(valueType));
        }
        if (keyType.getType() == Long.class) {
            return new LongMapCodec(type.getType(), registry.get(keyType), registry.get(valueType));
        }
        return null;
    }
}