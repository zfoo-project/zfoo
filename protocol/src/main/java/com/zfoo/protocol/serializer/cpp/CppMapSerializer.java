/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.serializer.cpp;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownMapSerializer;
import com.zfoo.protocol.serializer.reflect.ObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CppMapSerializer implements ICppSerializer {

    @Override
    public Pair<String, String> field(Field field, IFieldRegistration fieldRegistration) {
        var type = GenerateCppUtils.toCppClassName(field.getGenericType().toString());
        return new Pair<>(type, field.getName());
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownMapSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Cpp)) {
            return;
        }

        MapField mapField = (MapField) fieldRegistration;
        builder.append(StringUtils.format("buffer.writeInt({}.size());", objectStr)).append(LS);

        String key = "keyElement" + GenerateProtocolFile.index.getAndIncrement();
        String value = "valueElement" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("for (auto&[{}, {}] : {}) {", key, value, objectStr)).append(LS);

        var mapKeyRegistration = mapField.getMapKeyRegistration();
        var mapValueRegistration = mapField.getMapValueRegistration();
        GenerateCppUtils.cppSerializer(mapField.getMapKeyRegistration().serializer())
                .writeObject(builder, key, deep + 1, field, mapField.getMapKeyRegistration());
        GenerateCppUtils.cppSerializer(mapField.getMapValueRegistration().serializer())
                .writeObject(builder, value, deep + 1, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }


    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownMapSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Cpp);
        if (cutDown != null) {
            return cutDown;
        }

        MapField mapField = (MapField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        var typeName = GenerateCppUtils.toCppClassName(mapField.getType().toString());

        String size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int32_t {} = buffer.readInt();", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("{} {};", typeName, result)).append(LS);


        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("for (auto {} = 0; {} < {}; {}++) {", i, i, size, i)).append(LS);

        String keyObject = GenerateCppUtils.cppSerializer(mapField.getMapKeyRegistration().serializer())
                .readObject(builder, deep + 1, field, mapField.getMapKeyRegistration());

        String valueObject = GenerateCppUtils.cppSerializer(mapField.getMapValueRegistration().serializer())
                .readObject(builder, deep + 1, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep + 1);

        var keyPoint = StringUtils.EMPTY;
        var valuePoint = StringUtils.EMPTY;
        if (mapField.getMapKeyRegistration().serializer() == ObjectProtocolSerializer.INSTANCE) {
            keyPoint = "*";
        }
        if (mapField.getMapValueRegistration().serializer() == ObjectProtocolSerializer.INSTANCE) {
            valuePoint = "*";
        }

        builder.append(StringUtils.format("{}.insert(make_pair({}{}, {}{}));", result, keyPoint, keyObject, valuePoint, valueObject)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
        return result;
    }
}
