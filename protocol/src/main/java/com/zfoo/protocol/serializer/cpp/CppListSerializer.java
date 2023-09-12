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
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownListSerializer;
import com.zfoo.protocol.serializer.reflect.ObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CppListSerializer implements ICppSerializer {

    @Override
    public Pair<String, String> field(Field field, IFieldRegistration fieldRegistration) {
        var type = GenerateCppUtils.toCppClassName(field.getGenericType().toString());
        return new Pair<>(type, field.getName());
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownListSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Cpp)) {
            return;
        }

        ListField listField = (ListField) fieldRegistration;

        builder.append(StringUtils.format("buffer.writeInt({}.size());", objectStr)).append(LS);


        GenerateProtocolFile.addTab(builder, deep);
        String i = "i" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for (auto {} : {}) {", i, objectStr)).append(LS);

        GenerateCppUtils.cppSerializer(listField.getListElementRegistration().serializer())
                .writeObject(builder, i, deep + 1, field, listField.getListElementRegistration());

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownListSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Cpp);
        if (cutDown != null) {
            return cutDown;
        }

        var listField = (ListField) fieldRegistration;

        var result = "result" + GenerateProtocolFile.index.getAndIncrement();
        var typeName = GenerateCppUtils.toCppClassName(listField.getType().toString());

        var i = "index" + GenerateProtocolFile.index.getAndIncrement();
        var size = "size" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("int32_t {} = buffer.readInt();", size)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("{} {};", typeName, result)).append(LS);


        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("for (int {} = 0; {} < {}; {}++) {", i, i, size, i)).append(LS);
        var readObject = GenerateCppUtils.cppSerializer(listField.getListElementRegistration().serializer())
                .readObject(builder, deep + 1, field, listField.getListElementRegistration());

        var point = StringUtils.EMPTY;
        if (listField.getListElementRegistration().serializer() == ObjectProtocolSerializer.INSTANCE) {
            point = "*";
        }

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("{}.emplace_back({}{});", result, point, readObject)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);


        return result;
    }

}
