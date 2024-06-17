package com.zfoo.orm.codec;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础类型作为key的map解析器 (key 默认不能为null)
 * @Author：lqh
 * @Date：2024/6/17 13:55
 */
public class BaseTypeKeyMapCodec<K,V> implements Codec<Map<K, V>> {

    private final Class<Map<K, V>> encoderClass;
    private final Codec<K> keyCodec;
    private final Codec<V> valueCodec;

    BaseTypeKeyMapCodec(final Class<Map<K, V>> encoderClass, final Codec<K> keyCodec, final Codec<V> valueCodec) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void encode(final BsonWriter writer, final Map<K, V> map, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            MapKeyCodec<K> codec = (MapKeyCodec<K>) BaseTypeEnum.getCodec(keyCodec.getEncoderClass());
            String keyValue = codec.encode(key);
            writer.writeName(keyValue);
            if (value == null) {
                writer.writeNull();
            } else {
                valueCodec.encode(writer, value, encoderContext);
            }
        }
        writer.writeEndDocument();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Map<K, V> decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartDocument();
        var map = new HashMap<K, V>();
        while (BsonType.END_OF_DOCUMENT != reader.readBsonType()) {
            MapKeyCodec<K> codec = (MapKeyCodec<K>) BaseTypeEnum.getCodec(keyCodec.getEncoderClass());
            K key = codec.decode(reader.readName());
            V value = null;
            if (BsonType.NULL == reader.getCurrentBsonType()) {
                reader.readNull();
            } else {
                value = valueCodec.decode(reader, context);
            }
            map.put(key, value);
        }
        reader.readEndDocument();
        return map;
    }

    @Override
    public Class<Map<K, V>> getEncoderClass() {
        return encoderClass;
    }

}

