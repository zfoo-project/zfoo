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

package com.zfoo.protocol.serializer.kotlin;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownArraySerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class KtArraySerializer implements IKtSerializer {

    @Override
    public Pair<String, String> field(Field field, IFieldRegistration fieldRegistration) {
        var type = StringUtils.format("Array<{}>", CodeGenerateKotlin.toKotlinClassName(field.getType().getComponentType().getSimpleName()));
        return new Pair<>(type, "emptyArray()");
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownArraySerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Kotlin)) {
            return;
        }

        ArrayField arrayField = (ArrayField) fieldRegistration;

        builder.append(StringUtils.format("buffer.writeInt({}.size)", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        String length = "length" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("val {} = {}.size", length, objectStr)).append(LS);

        String i = "i" + GenerateProtocolFile.localVariableId++;
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("for ({} in 0 until {}) {", i, length)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        String element = "element" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("val {} = {}[{}]", element, objectStr, i)).append(LS);

        CodeGenerateKotlin.ktSerializer(arrayField.getArrayElementRegistration().serializer())
                .writeObject(builder, element, deep + 1, field, arrayField.getArrayElementRegistration());

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownArraySerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Kotlin);
        if (cutDown != null) {
            return cutDown;
        }


        var arrayField = (ArrayField) fieldRegistration;
        var result = "result" + GenerateProtocolFile.localVariableId++;

        var typeName = CodeGenerateKotlin.toKotlinClassName(arrayField.getType().getSimpleName());

        var i = "index" + GenerateProtocolFile.localVariableId++;
        var init = "init" + GenerateProtocolFile.localVariableId++;
        var size = "size" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("val {} = buffer.readInt()", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        var pair = CodeGenerateKotlin.ktSerializer(arrayField.getArrayElementRegistration().serializer()).field(field, arrayField.getArrayElementRegistration());
        var defaultValue = pair.getValue();
        if (defaultValue.equals("null")) {
            defaultValue = StringUtils.format("{}()", typeName);
        }
        builder.append(StringUtils.format("val {} = Array<{}>({}) { init -> {}}", result, typeName, size, defaultValue)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0) {", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for ({} in 0 until {}) {", i, size)).append(LS);
        var readObject = CodeGenerateKotlin.ktSerializer(arrayField.getArrayElementRegistration().serializer())
                .readObject(builder, deep + 2, field, arrayField.getArrayElementRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}[{}] = {}", result, i, readObject));
        builder.append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);


        return result;
    }
}
