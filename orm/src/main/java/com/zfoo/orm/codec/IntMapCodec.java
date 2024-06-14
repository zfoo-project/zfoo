package com.zfoo.orm.codec;

import org.bson.*;
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
public class IntMapCodec<V> implements Codec<Map<Integer, V>> {

    private final Class<Map<Integer, V>> encoderClass;
    private final Codec<Integer> keyCodec;
    private final Codec<V> valueCodec;

    IntMapCodec(final Class<Map<Integer, V>> encoderClass, final Codec<Integer> keyCodec, final Codec<V> valueCodec) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    public void encode(final BsonWriter writer, final Map<Integer, V> map, final EncoderContext encoderContext) {
        try (var dummyWriter = new BsonDocumentWriter(new BsonDocument())) {
            dummyWriter.writeStartDocument();
            writer.writeStartDocument();

            for (final Map.Entry<Integer, V> entry : map.entrySet()) {
                writer.writeName(entry.getKey().toString());
                valueCodec.encode(writer, entry.getValue(), encoderContext);
            }
            dummyWriter.writeEndDocument();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        writer.writeEndDocument();
    }

    @Override
    public Map<Integer, V> decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartDocument();
        Map<Integer, V> map = new HashMap<>();
        while (!BsonType.END_OF_DOCUMENT.equals(reader.readBsonType())) {
            int key = Integer.parseInt(reader.readName());
            V value = BsonType.NULL.equals(reader.getCurrentBsonType()) ? null : valueCodec.decode(reader, context);
            map.put(key, value);
        }
        reader.readEndDocument();
        return map;
    }

    @Override
    public Class<Map<Integer, V>> getEncoderClass() {
        return encoderClass;
    }

}
