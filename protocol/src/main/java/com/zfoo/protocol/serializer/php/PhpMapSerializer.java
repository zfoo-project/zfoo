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

package com.zfoo.protocol.serializer.php;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.model.Triple;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownMapSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class PhpMapSerializer implements IPhpSerializer {

    @Override
    public Pair<String, String> field(Field field, IFieldRegistration fieldRegistration) {
        return new Pair<>("array", "array()");
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownMapSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Php)) {
            return;
        }

        MapField mapField = (MapField) fieldRegistration;
        builder.append(StringUtils.format("if (empty({})) {", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("$buffer->writeInt(0);").append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("} else {").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        String length = "$length" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("{} = count({});", length, objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("$buffer->writeInt({});", length)).append(LS);

        String key = "$key" + GenerateProtocolFile.localVariableId++;
        String value = "$value" + GenerateProtocolFile.localVariableId++;

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("foreach ({} as {} => {}) {", objectStr, value, key)).append(LS);
        CodeGeneratePhp.phpSerializer(mapField.getMapKeyRegistration().serializer())
                .writeObject(builder, key, deep + 2, field, mapField.getMapKeyRegistration());
        CodeGeneratePhp.phpSerializer(mapField.getMapValueRegistration().serializer())
                .writeObject(builder, value, deep + 2, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownMapSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Php);
        if (cutDown != null) {
            return cutDown;
        }

        MapField mapField = (MapField) fieldRegistration;
        String result = "$result" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("{} = array();", result)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        String size = "$size" + GenerateProtocolFile.localVariableId++;
        builder.append(StringUtils.format("{} = $buffer->readInt();", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0) {", size)).append(LS);

        String i = "$index" + GenerateProtocolFile.localVariableId++;
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for ({} = 0; {} < {}; {}++) {", i, i, size, i)).append(LS);

        String keyObject = CodeGeneratePhp.phpSerializer(mapField.getMapKeyRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapKeyRegistration());


        String valueObject = CodeGeneratePhp.phpSerializer(mapField.getMapValueRegistration().serializer())
                .readObject(builder, deep + 2, field, mapField.getMapValueRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);

        builder.append(StringUtils.format("{}[{}] = {};", result, keyObject, valueObject)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
        return result;
    }
}
