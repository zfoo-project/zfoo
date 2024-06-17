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
 * map解析器
 *
 * @Author：lqh
 * @Date：2024/6/14 10:32
 */
public class LongMapCodec<V> implements Codec<Map<Long, V>> {

    private final Class<Map<Long, V>> encoderClass;
    private final Codec<Long> keyCodec;
    private final Codec<V> valueCodec;

    LongMapCodec(final Class<Map<Long, V>> encoderClass, final Codec<Long> keyCodec, final Codec<V> valueCodec) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    public void encode(final BsonWriter writer, final Map<Long, V> map, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            writer.writeName(key.toString());
            if (value == null) {
                writer.writeNull();
            } else {
                valueCodec.encode(writer, value, encoderContext);
            }
        }
        writer.writeEndDocument();
    }

    @Override
    public Map<Long, V> decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartDocument();
        var map = new HashMap<Long, V>();
        while (!BsonType.END_OF_DOCUMENT.equals(reader.readBsonType())) {
            long key = Long.parseLong(reader.readName());
            V value = null;
            if (BsonType.NULL.equals(reader.getCurrentBsonType())) {
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
    public Class<Map<Long, V>> getEncoderClass() {
        return encoderClass;
    }

}
