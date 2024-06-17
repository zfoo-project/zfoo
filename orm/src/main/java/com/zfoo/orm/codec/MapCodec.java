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

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 基础类型作为key的map解析器 (key 默认不能为null)
 *
 * @Author：lqh
 * @Date：2024/6/17 13:55
 */
public class MapCodec<K, V> implements Codec<Map<K, V>> {

    private final Class<Map<K, V>> encoderClass;
    private final Codec<K> keyCodec;
    private final Codec<V> valueCodec;
    private final Function<String, K> keyDecodeFunction;

    @SuppressWarnings("unchecked")
    MapCodec(Class<Map<K, V>> encoderClass, Codec<K> keyCodec, Codec<V> valueCodec) {
        this.encoderClass = encoderClass;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
        this.keyDecodeFunction = (Function<String, K>) MapKeyCodecEnum.keyDecode(keyCodec.getEncoderClass());
    }

    @Override
    public void encode(BsonWriter writer, Map<K, V> map, EncoderContext encoderContext) {
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
    public Map<K, V> decode(BsonReader reader, DecoderContext context) {
        var map = new HashMap<K, V>();
        reader.readStartDocument();
        while (BsonType.END_OF_DOCUMENT != reader.readBsonType()) {
            K key = keyDecodeFunction.apply(reader.readName());
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

