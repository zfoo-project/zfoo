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

package com.zfoo.protocol.serializer.enhance;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.EnhanceUtils;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * 对应于ObjectProtocolSerializer
 *
 * @author godotg
 */
public class EnhanceObjectProtocolSerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        var objectProtocolField = (ObjectProtocolField) fieldRegistration;
        builder.append(StringUtils.format("{}.write($1,{});", EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId()), objectStr));
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var objectProtocolField = (ObjectProtocolField) fieldRegistration;
        var result = "result" + GenerateProtocolFile.index.getAndIncrement();
        var protocolName = getProtocolClassCanonicalName(objectProtocolField.getProtocolId());
        builder.append(StringUtils.format("{} {} = ({}){}.read($1);", protocolName, result, protocolName, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
        return result;
    }

    @Override
    public String defaultValue(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var objectProtocolField = (ObjectProtocolField) fieldRegistration;
        var result = "result" + GenerateProtocolFile.index.getAndIncrement();
        var protocolName = getProtocolClassCanonicalName(objectProtocolField.getProtocolId());
        builder.append(StringUtils.format("{} {} = null;", protocolName, result));
        return result;
    }

    public static String getProtocolClassSimpleName(short protocolId) {
        return ProtocolManager.getProtocol(protocolId).protocolConstructor().getDeclaringClass().getSimpleName();
    }

    public static String getProtocolClassCanonicalName(short protocolId) {
        return ProtocolManager.getProtocol(protocolId).protocolConstructor().getDeclaringClass().getCanonicalName();
    }

}
