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

import com.zfoo.protocol.registration.field.IFieldRegistration;

import java.lang.reflect.Field;

/**
 * @author godotg
 */
public interface IEnhanceSerializer {

    default boolean isPrimitiveField(Field field) {
        if (field.getType().isPrimitive()) {
            return true;
        }
        return field.getType().isArray() && field.getType().getComponentType().isPrimitive();
    }

    /**
     * IProtocolRegistration.write(ByteBuf buffer, Object packet);
     * $1=buffer
     * $2=packet
     */
    void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration);

    /**
     * IProtocolRegistration.Object read(ByteBuf buffer);
     * $1=buffer
     */
    String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration);

    String defaultValue(StringBuilder builder, Field field, IFieldRegistration fieldRegistration);

}
