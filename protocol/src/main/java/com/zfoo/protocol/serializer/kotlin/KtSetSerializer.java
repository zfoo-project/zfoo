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
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.SetField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownSetSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class KtSetSerializer implements IKtSerializer {

    @Override
    public Pair<String, String> field(Field field, IFieldRegistration fieldRegistration) {
        var type = StringUtils.format("{}", CodeGenerateKotlin.toKotlinClassName(field.getGenericType().toString()));
        return new Pair<>(type, "emptySet()");
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownSetSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Kotlin)) {
            return;
        }

        SetField setField = (SetField) fieldRegistration;

        builder.append(StringUtils.format("buffer.writeInt({}.size)", objectStr)).append(LS);

        String element = "i" + GenerateProtocolFile.localVariableId++;
        GenerateProtocolFile.addTab(builder, deep );
        builder.append(StringUtils.format("for ({} in {}) {", element, objectStr)).append(LS);

        CodeGenerateKotlin.ktSerializer(setField.getSetElementRegistration().serializer())
                .writeObject(builder, element, deep + 1, field, setField.getSetElementRegistration());

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownSetSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Kotlin);
        if (cutDown != null) {
            return cutDown;
        }

        SetField setField = (SetField) fieldRegistration;
        var result = "result" + GenerateProtocolFile.localVariableId++;

        var typeName = CodeGenerateKotlin.toKotlinClassName(setField.getType().toString());

        var i = "index" + GenerateProtocolFile.localVariableId++;
        var size = "size" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("val {} = buffer.readInt()", size)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("val {} = Hash{}()", result, typeName)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0) {", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for ({} in 0 until {}) {", i, size)).append(LS);

        var readObject = CodeGenerateKotlin.ktSerializer(setField.getSetElementRegistration().serializer())
                .readObject(builder, deep + 2, field, setField.getSetElementRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}.add({})", result, readObject)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);

        return result;
    }

}
