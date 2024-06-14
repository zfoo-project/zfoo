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
        for (final Map.Entry<Long, V> entry : map.entrySet()) {
            writer.writeName(entry.getKey().toString());
            valueCodec.encode(writer, entry.getValue(), encoderContext);
        }
        writer.writeEndDocument();
    }

    @Override
    public Map<Long, V> decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartDocument();
        var map = new HashMap<Long, V>();
        while (!BsonType.END_OF_DOCUMENT.equals(reader.readBsonType())) {
            long key = Long.parseLong(reader.readName());
            V value = BsonType.NULL.equals(reader.getCurrentBsonType()) ? null : valueCodec.decode(reader, context);
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
