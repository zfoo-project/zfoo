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

package com.zfoo.protocol.registration.field;

import com.zfoo.protocol.serializer.reflect.ISerializer;
import com.zfoo.protocol.serializer.reflect.ObjectProtocolSerializer;

/**
 * @author godotg
 */
public class ObjectProtocolField implements IFieldRegistration {

    /**
     * 协议序列号是ProtocolRegistration的id
     */
    private short protocolId;

    public static ObjectProtocolField valueOf(short protocolId) {
        ObjectProtocolField objectProtocolField = new ObjectProtocolField();
        objectProtocolField.protocolId = protocolId;
        return objectProtocolField;
    }

    public short getProtocolId() {
        return protocolId;
    }

    @Override
    public ISerializer serializer() {
        return ObjectProtocolSerializer.INSTANCE;
    }
}
