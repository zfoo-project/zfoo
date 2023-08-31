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

package com.zfoo.protocol.serializer.gdscript;

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
public class GdMapSerializer implements IGdSerializer {

    @Override
    public String fieldType(Field field, IFieldRegistration fieldRegistration) {
        return "Dictionary";
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTabAscii(builder, deep);
        if (CutDownMapSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.GdScript)) {
            return;
        }

        MapField mapField = (MapField) fieldRegistration;
        builder.append(StringUtils.format("if ({} == null):", objectStr)).append(LS);
        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append("buffer.writeInt(0)").append(LS);

        GenerateProtocolFile.addTabAscii(builder, deep);
        builder.append("else:").append(LS);

        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append(StringUtils.format("buffer.writeInt({}.size())", objectStr)).append(LS);

        String key = "key" + GenerateProtocolFile.index.getAndIncrement();
        String value = "value" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append(StringUtils.format("for {} in {}:", key, objectStr)).append(LS);
        GenerateProtocolFile.addTabAscii(builder, deep + 2);
        builder.append(StringUtils.format("var {} = {}[{}]", value, objectStr, key)).append(LS);
        GenerateGdUtils.gdSerializer(mapField.getMapKeyRegistration().serializer())
                .writeObject(builder, key, deep + 2, field, mapField.getMapKeyRegistration());
        GenerateGdUtils.gdSerializer(mapField.getMapValueRegistration().serializer())
                .writeObject(builder, value, deep + 2, field, mapField.getMapValueRegistration());
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTabAscii(builder, deep);
        var cutDown = CutDownMapSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.GdScript);
        if (cutDown != null) {
            return cutDown;
        }

        MapField mapField = (MapField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("var {} = {}", result)).append(LS);

        GenerateProtocolFile.addTabAscii(builder, deep);
        String size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("var {} = buffer.readInt()", size)).append(LS);

        GenerateProtocolFile.addTabAscii(builder, deep);
        builder.append(StringUtils.format("if ({} > 0):", size)).append(LS);

        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append(StringUtils.format("for {} in range({}):", i, size)).append(LS);

        String keyObject = GenerateGdUtils.gdSerializer(mapField.getMapKeyRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapKeyRegistration());


        String valueObject = GenerateGdUtils.gdSerializer(mapField.getMapValueRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTabAscii(builder, deep + 2);

        builder.append(StringUtils.format("{}[{}] = {}", result, keyObject, valueObject)).append(LS);
        return result;
    }
}
