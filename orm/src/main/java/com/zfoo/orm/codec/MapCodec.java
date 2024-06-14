package com.zfoo.orm.codec;

import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.json.JsonReader;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * map解析器
 * @Author：lqh
 * @Date：2024/6/14 10:32
 */
public class MapCodec<K, T> implements Codec<Map<K, T>> {

    private static final Logger LOGGER = Logger.getLogger(MapCodec.class.getName());

    private final Class<Map<K, T>> encoderClass;
    private final Codec<K> keyCodec;
    private final Codec<T> valueCodec;

    MapCodec(final Class<Map<K, T>> encoderClass, final Codec<K> keyCodec, final Codec<T> valueCodec,
             Map<Class<?>, Class<? extends PropertyEditor>> map) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;

        map.forEach(PropertyEditorManager::registerEditor);
    }

    MapCodec(final Class<Map<K, T>> encoderClass, final Codec<K> keyCodec, final Codec<T> valueCodec) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    public void encode(final BsonWriter writer, final Map<K, T> map, final EncoderContext encoderContext) {
        try (var dummyWriter = new BsonDocumentWriter(new BsonDocument())) {
            dummyWriter.writeStartDocument();
            writer.writeStartDocument();

            for (final Map.Entry<K, T> entry : map.entrySet()) {
                PropertyEditor editor = PropertyEditorManager.findEditor(keyCodec.getEncoderClass());
                if (editor != null) {
                    LOGGER.fine("Found PropertyEditor for class: " + keyCodec.getEncoderClass().getName());

                    editor.setValue(entry.getKey());
                    writer.writeName(editor.getAsText());
                } else {
                    String dummyId = UUID.randomUUID().toString();
                    dummyWriter.writeName(dummyId);
                    keyCodec.encode(dummyWriter, entry.getKey(), encoderContext);
                    writer.writeName(dummyWriter.getDocument().asDocument().get(dummyId).asString().getValue());
                }

                valueCodec.encode(writer, entry.getValue(), encoderContext);
            }
            dummyWriter.writeEndDocument();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to encode map: " + map, e);
            throw new IllegalArgumentException(e);
        }
        writer.writeEndDocument();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, T> decode(final BsonReader reader, final DecoderContext context) {
        reader.readStartDocument();
        Map<K, T> map = getInstance();
        while (!BsonType.END_OF_DOCUMENT.equals(reader.readBsonType())) {
            K key;
            PropertyEditor editor = PropertyEditorManager.findEditor(keyCodec.getEncoderClass());
            if (editor != null) {
                LOGGER.fine("Found PropertyEditor for class: " + keyCodec.getEncoderClass().getName());
                editor.setAsText(reader.readName());
                key = (K) editor.getValue();
            } else {
                var dummyReader = new JsonReader(String.format("\"key\": \"%s\"", reader.readName()));
                key = keyCodec.decode(dummyReader, context);
            }

            map.put(key,
                    (!BsonType.NULL.equals(reader.getCurrentBsonType()))
                            ? valueCodec.decode(reader, context)
                            : null);
        }
        reader.readEndDocument();
        return map;
    }

    @Override
    public Class<Map<K, T>> getEncoderClass() {
        return encoderClass;
    }

    private Map<K, T> getInstance() {
        if (encoderClass.isInterface()) {
            return new HashMap<>();
        }
        try {
            return encoderClass.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            throw new CodecConfigurationException(e.getMessage(), e);
        }
    }
}
