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
import com.zfoo.protocol.registration.field.SetField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownSetSerializer;

import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 * @version 3.0
 */
public class PySetSerializer implements IPySerializer {
    @Override
    public String fieldDefaultValue(Field field, IFieldRegistration fieldRegistration) {
        return "{}";
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownSetSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Python)) {
            return;
        }

        SetField setField = (SetField) fieldRegistration;

        builder.append(StringUtils.format("if {} is None:", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("buffer.writeInt(0)").append(LS);
        GenerateProtocolFile.addTab(builder, deep);

        builder.append("else:").append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("buffer.writeInt(len({}))", objectStr)).append(LS);

        String element = "element" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for {} in {}:", element, objectStr)).append(LS);
        GeneratePyUtils.pySerializer(setField.getSetElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, setField.getSetElementRegistration());
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownSetSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Python);
        if (cutDown != null) {
            return cutDown;
        }

        SetField setField = (SetField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("{} = []", result)).append(LS);

        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        String size = "size" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("{} = buffer.readInt()", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if {} > 0:", size)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for {} in range({}):", i, size)).append(LS);
        String readObject = GeneratePyUtils.pySerializer(setField.getSetElementRegistration().serializer())
                .readObject(builder, deep + 2, field, setField.getSetElementRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}.append({})", result, readObject)).append(LS);
        return result;
    }
}
