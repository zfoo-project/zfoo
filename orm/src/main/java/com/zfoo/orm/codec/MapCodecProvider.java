package com.zfoo.orm.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

/**
 *
 * @Author：lqh
 * @Date：2024/6/14 10:23
 */
public class MapCodecProvider implements PropertyCodecProvider {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(final TypeWithTypeParameters<T> type, final PropertyCodecRegistry registry) {
        if (Map.class.isAssignableFrom(type.getType()) && type.getTypeParameters().size() == 2) {
            return new MapCodec(
                    type.getType(),
                    registry.get(type.getTypeParameters().get(0)),
                    registry.get(type.getTypeParameters().get(1)));
        }
        return null;
    }
}