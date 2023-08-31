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

package com.zfoo.protocol.serializer.python;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownMapSerializer;

import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 * @version 3.0
 */
public class PyMapSerializer implements IPySerializer {
    @Override
    public String fieldDefaultValue(Field field, IFieldRegistration fieldRegistration) {
        return "{}";
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownMapSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Python)) {
            return;
        }

        MapField mapField = (MapField) fieldRegistration;
        builder.append(StringUtils.format("if {} is None:", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("buffer.writeInt(0)").append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("else:").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("buffer.writeInt(len({}))", objectStr)).append(LS);

        String key = "key" + GenerateProtocolFile.index.getAndIncrement();
        String value = "value" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for {} in {}:", key, objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{} = {}[{}]", value, objectStr, key)).append(LS);
        GeneratePyUtils.pySerializer(mapField.getMapKeyRegistration().serializer())
                .writeObject(builder, key, deep + 2, field, mapField.getMapKeyRegistration());
        GeneratePyUtils.pySerializer(mapField.getMapValueRegistration().serializer())
                .writeObject(builder, value, deep + 2, field, mapField.getMapValueRegistration());
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownMapSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Python);
        if (cutDown != null) {
            return cutDown;
        }

        MapField mapField = (MapField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("{} = {}", result)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        String size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("{} = buffer.readInt()", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if {} > 0:", size)).append(LS);

        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for {} in range({}):", i, size)).append(LS);

        String keyObject = GeneratePyUtils.pySerializer(mapField.getMapKeyRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapKeyRegistration());


        String valueObject = GeneratePyUtils.pySerializer(mapField.getMapValueRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);

        builder.append(StringUtils.format("{}[{}] = {}", result, keyObject, valueObject)).append(LS);
        return result;
    }
}
