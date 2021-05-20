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

package com.zfoo.protocol.serializer.lua;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.SetField;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class LuaSetSerializer implements ILuaSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        SetField setField = (SetField) fieldRegistration;

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if {} == null then", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("byteBuffer:writeInt(0)").append(LS);
        GenerateProtocolFile.addTab(builder, deep);

        builder.append("else").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("byteBuffer:writeInt(table.setSize({}))", objectStr)).append(LS);

        String index = "index" + GenerateProtocolFile.index.getAndIncrement();
        String element = "element" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for {}, {} in pairs({}) do", index, element, objectStr)).append(LS);
        GenerateLuaUtils.luaSerializer(setField.getSetElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, setField.getSetElementRegistration());
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("end").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("end").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        SetField setField = (SetField) fieldRegistration;
        String result = "result" + GenerateProtocolFile.index.getAndIncrement();

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("local {} = {}", result)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        String size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("local {} = byteBuffer:readInt()", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if {} > 0 then", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        String i = "index" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for {} = 1, {} do", i, size)).append(LS);
        String readObject = GenerateLuaUtils.luaSerializer(setField.getSetElementRegistration().serializer())
                .readObject(builder, deep + 2, field, setField.getSetElementRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}[{}] = {}", result, readObject, readObject)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("end").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("end").append(LS);

        return result;
    }
}
