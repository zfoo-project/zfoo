/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.zfoo.protocol.serializer.reflect;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import io.netty.buffer.ByteBuf;

/**
 * 只要是protocol都是使用FieldSerializer
 *
 * @author godotg
 * @version 3.0
 */
public class ObjectProtocolSerializer implements ISerializer {

    public static final ObjectProtocolSerializer INSTANCE = new ObjectProtocolSerializer();

    /**
     * @param buffer ByteBuf
     */
    @Override
    public void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration) {
        ObjectProtocolField objectProtocolField = (ObjectProtocolField) fieldRegistration;
        IProtocolRegistration protocol = ProtocolManager.getProtocol(objectProtocolField.getProtocolId());
        protocol.write(buffer, object);
    }

    @Override
    public Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration) {
        ObjectProtocolField objectProtocolField = (ObjectProtocolField) fieldRegistration;
        IProtocolRegistration protocol = ProtocolManager.getProtocol(objectProtocolField.getProtocolId());
        return protocol.read(buffer);
    }
}
