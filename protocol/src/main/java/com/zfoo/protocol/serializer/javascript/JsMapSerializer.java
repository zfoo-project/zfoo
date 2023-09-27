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

package com.zfoo.protocol.serializer.javascript;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.model.Triple;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownMapSerializer;
import com.zfoo.protocol.serializer.typescript.GenerateTsUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class JsMapSerializer implements IJsSerializer {
    @Override
    public Triple<String, String, String> field(Field field, IFieldRegistration fieldRegistration) {
        return new Triple<>(GenerateTsUtils.toTsClassName(field.getGenericType().toString()), field.getName(), "new Map()");
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownMapSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.JavaScript)) {
            return;
        }

        MapField mapField = (MapField) fieldRegistration;
        builder.append(StringUtils.format("if ({} === null) {", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("buffer.writeInt(0);").append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("} else {").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("buffer.writeInt({}.size);", objectStr)).append(LS);

        String key = "key" + GenerateProtocolFile.index.getAndIncrement();
        String value = "value" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("{}.forEach(({}, {}) => {", objectStr, value, key)).append(LS);
        GenerateJsUtils.jsSerializer(mapField.getMapKeyRegistration().serializer())
                .writeObject(builder, key, deep + 2, field, mapField.getMapKeyRegistration());
        GenerateJsUtils.jsSerializer(mapField.getMapValueRegistration().serializer())
                .writeObject(builder, value, deep + 2, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("});").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownMapSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.JavaScript);
        if (cutDown != null) {
            return cutDown;
        }

        MapField mapField = (MapField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("const {} = new Map();", result)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        String size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("const {} = buffer.readInt();", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0) {", size)).append(LS);

        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for (let {} = 0; {} < {}; {}++) {", i, i, size, i)).append(LS);

        String keyObject = GenerateJsUtils.jsSerializer(mapField.getMapKeyRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapKeyRegistration());


        String valueObject = GenerateJsUtils.jsSerializer(mapField.getMapValueRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);

        builder.append(StringUtils.format("{}.set({}, {});", result, keyObject, valueObject)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
        return result;
    }
}
