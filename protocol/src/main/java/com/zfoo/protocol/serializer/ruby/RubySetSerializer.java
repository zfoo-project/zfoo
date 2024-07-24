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

package com.zfoo.protocol.serializer.ruby;

import com.zfoo.protocol.generate.GenerateProtocolFile;
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
public class RubySetSerializer implements IRubySerializer {
    @Override
    public String fieldDefaultValue(Field field, IFieldRegistration fieldRegistration) {
        return "Set.new()";
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTabWith2Space(builder, deep);
        if (CutDownSetSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Ruby)) {
            return;
        }

        SetField setField = (SetField) fieldRegistration;

        builder.append(StringUtils.format("if {}.nil? || {}.empty?", objectStr, objectStr)).append(LS);
        GenerateProtocolFile.addTabWith2Space(builder, deep + 1);
        builder.append("buffer.writeInt(0)").append(LS);
        GenerateProtocolFile.addTabWith2Space(builder, deep);

        builder.append("else").append(LS);
        GenerateProtocolFile.addTabWith2Space(builder, deep + 1);
        builder.append(StringUtils.format("buffer.writeInt({}.length)", objectStr)).append(LS);

        String element = "element" + GenerateProtocolFile.localVariableId++;
        GenerateProtocolFile.addTabWith2Space(builder, deep + 1);
        builder.append(StringUtils.format("for {} in {}", element, objectStr)).append(LS);
        CodeGenerateRuby.rbSerializer(setField.getSetElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, setField.getSetElementRegistration());

        GenerateProtocolFile.addTabWith2Space(builder, deep + 1);
        builder.append("end").append(LS);
        GenerateProtocolFile.addTabWith2Space(builder, deep);
        builder.append("end").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTabWith2Space(builder, deep);
        var cutDown = CutDownSetSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Ruby);
        if (cutDown != null) {
            return cutDown;
        }

        SetField setField = (SetField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.localVariableId++;

        builder.append(StringUtils.format("{} = Set.new()", result)).append(LS);

        String i = "index" + GenerateProtocolFile.localVariableId++;
        String size = "size" + GenerateProtocolFile.localVariableId++;

        GenerateProtocolFile.addTabWith2Space(builder, deep);
        builder.append(StringUtils.format("{} = buffer.readInt()", size)).append(LS);

        GenerateProtocolFile.addTabWith2Space(builder, deep);
        builder.append(StringUtils.format("if {} > 0", size)).append(LS);
        GenerateProtocolFile.addTabWith2Space(builder, deep + 1);
        builder.append(StringUtils.format("for {} in 0..{} - 1", i, size)).append(LS);
        String readObject = CodeGenerateRuby.rbSerializer(setField.getSetElementRegistration().serializer())
                .readObject(builder, deep + 2, field, setField.getSetElementRegistration());
        GenerateProtocolFile.addTabWith2Space(builder, deep + 2);
        builder.append(StringUtils.format("{}.add({})", result, readObject)).append(LS);

        GenerateProtocolFile.addTabWith2Space(builder, deep + 1);
        builder.append("end").append(LS);
        GenerateProtocolFile.addTabWith2Space(builder, deep);
        builder.append("end").append(LS);
        return result;
    }
}
