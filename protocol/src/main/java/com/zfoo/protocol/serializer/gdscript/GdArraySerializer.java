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
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownArraySerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;


/**
 * @author godotg
 * @version 3.0
 */
public class GdArraySerializer implements IGdSerializer {

    public static String arrayType(String type) {
        if (type.startsWith("Array") || type.startsWith("Dictionary")) {
            return "Array";
        } else {
            return StringUtils.format("Array[{}]", type);
        }
    }

    @Override
    public String fieldType(Field field, IFieldRegistration fieldRegistration) {
        var arrayField = (ArrayField) fieldRegistration;
        var registration = arrayField.getArrayElementRegistration();
        var type = GenerateGdUtils.gdSerializer(registration.serializer()).fieldType(field, registration);
        return arrayType(type);
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTabAscii(builder, deep);
        if (CutDownArraySerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.GdScript)) {
            return;
        }

        ArrayField arrayField = (ArrayField) fieldRegistration;

        builder.append(StringUtils.format("if ({} == null):", objectStr)).append(LS);
        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append("buffer.writeInt(0)").append(LS);
        GenerateProtocolFile.addTabAscii(builder, deep);

        builder.append("else:").append(LS);
        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append(StringUtils.format("buffer.writeInt({}.size())", objectStr)).append(LS);

        String element = "element" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append(StringUtils.format("for {} in {}:", element, objectStr)).append(LS);
        GenerateGdUtils.gdSerializer(arrayField.getArrayElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, arrayField.getArrayElementRegistration());
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTabAscii(builder, deep);
        var cutDown = CutDownArraySerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.GdScript);
        if (cutDown != null) {
            return cutDown;
        }

        ArrayField arrayField = (ArrayField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("var {} = []", result)).append(LS);

        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        String size = "size" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTabAscii(builder, deep);
        builder.append(StringUtils.format("var {} = buffer.readInt()", size)).append(LS);

        GenerateProtocolFile.addTabAscii(builder, deep);
        builder.append(StringUtils.format("if ({} > 0):", size)).append(LS);
        GenerateProtocolFile.addTabAscii(builder, deep + 1);
        builder.append(StringUtils.format("for {} in range({}):", i, size)).append(LS);
        String readObject = GenerateGdUtils.gdSerializer(arrayField.getArrayElementRegistration().serializer())
                .readObject(builder, deep + 2, field, arrayField.getArrayElementRegistration());
        GenerateProtocolFile.addTabAscii(builder, deep + 2);
        builder.append(StringUtils.format("{}.append({})", result, readObject)).append(LS);
        return result;
    }
}
