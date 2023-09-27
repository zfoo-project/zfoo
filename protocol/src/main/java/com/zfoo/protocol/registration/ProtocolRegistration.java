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

package com.zfoo.protocol.registration;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.reflect.ISerializer;
import com.zfoo.protocol.util.ReflectionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author godotg
 */
public class ProtocolRegistration implements IProtocolRegistration {

    private short id;
    private byte module;
    private Constructor<?> constructor;
    private Field[] fields;

    /**
     * 所有的协议里的发送顺序都是按字段名称排序
     */
    private IFieldRegistration[] fieldRegistrations;

    // 兼容相关
    private boolean compatible;
    private int predictionLength;

    public ProtocolRegistration(short id, byte module, Constructor<?> constructor, Field[] fields, IFieldRegistration[] fieldRegistrations) {
        this.id = id;
        this.module = module;
        this.constructor = constructor;
        this.fields = fields;
        this.fieldRegistrations = fieldRegistrations;

        this.compatible = Arrays.stream(fields).anyMatch(it -> it.isAnnotationPresent(Compatible.class));
        this.predictionLength = Arrays.stream(fieldRegistrations).mapToInt(it -> it.predictionLength()).sum();
    }

    @Override
    public short protocolId() {
        return id;
    }

    @Override
    public byte module() {
        return module;
    }

    @Override
    public Constructor<?> protocolConstructor() {
        return constructor;
    }

    @Override
    public void write(ByteBuf byteBuf, Object packet) {
        if (packet == null) {
            // equals with ByteBufUtils.writeInt(byteBuf, 0);
            byteBuf.writeByte(0);
            return;
        }

        var beforeWriteIndex = byteBuf.writerIndex();

        if (compatible) {
            ByteBufUtils.writeInt(byteBuf, predictionLength);
        } else {
            // equals with ByteBufUtils.writeInt(byteBuf, -1);
            byteBuf.writeByte(1);
        }

        for (int i = 0, length = fields.length; i < length; i++) {
            Field field = fields[i];
            IFieldRegistration packetFieldRegistration = fieldRegistrations[i];
            ISerializer serializer = packetFieldRegistration.serializer();
            Object fieldValue = ReflectionUtils.getField(field, packet);
            serializer.writeObject(byteBuf, fieldValue, packetFieldRegistration);
        }

        if (compatible) {
            // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
            ByteBufUtils.adjustPadding(byteBuf, predictionLength, beforeWriteIndex);
        }
    }

    @Override
    public Object read(ByteBuf byteBuf) {
        // length为-1代表协议没有可兼容的部分，0代表为空对象，正数代表需要兼容的协议长度
        var length = ByteBufUtils.readInt(byteBuf);
        if (length == 0) {
            return null;
        }
        Object object = null;

        var beforeReadIndex = byteBuf.readerIndex();
        var packetClazz = constructor.getDeclaringClass();
        if (packetClazz.isRecord()) {
            var originFields = ProtocolAnalysis.getFields(packetClazz);
            var constructorParams = new Object[originFields.size()];
            for (int i = 0, j = fields.length; i < j; i++) {
                var field = fields[i];
                var index = originFields.indexOf(field);
                var packetFieldRegistration = fieldRegistrations[i];

                // 协议向后兼容
                if (field.isAnnotationPresent(Compatible.class)) {
                    if (!ByteBufUtils.compatibleRead(byteBuf, beforeReadIndex, length)) {
                        constructorParams[index] = packetFieldRegistration.defaultValue();
                        continue;
                    }
                }
                ISerializer serializer = packetFieldRegistration.serializer();
                Object fieldValue = serializer.readObject(byteBuf, packetFieldRegistration);
                constructorParams[index] = fieldValue;
            }
            object = ReflectionUtils.newInstance(constructor, constructorParams);
        } else {
            object = ReflectionUtils.newInstance(constructor);
            for (int i = 0, j = fields.length; i < j; i++) {
                Field field = fields[i];
                IFieldRegistration packetFieldRegistration = fieldRegistrations[i];
                ISerializer serializer = packetFieldRegistration.serializer();
                // 协议向后兼容
                if (field.isAnnotationPresent(Compatible.class)) {
                    if (!ByteBufUtils.compatibleRead(byteBuf, beforeReadIndex, length)) {
                        ReflectionUtils.setField(field, object, packetFieldRegistration.defaultValue());
                        continue;
                    }
                }
                Object fieldValue = serializer.readObject(byteBuf, packetFieldRegistration);
                ReflectionUtils.setField(field, object, fieldValue);
            }
        }

        if (length > 0) {
            byteBuf.readerIndex(beforeReadIndex + length);
        }

        return object;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public byte getModule() {
        return module;
    }

    public void setModule(byte module) {
        this.module = module;
    }

    public Field[] getFields() {
        return fields;
    }

    public void setFields(Field[] fields) {
        this.fields = fields;
    }

    public IFieldRegistration[] getFieldRegistrations() {
        return fieldRegistrations;
    }

    public void setFieldRegistrations(IFieldRegistration[] fieldRegistrations) {
        this.fieldRegistrations = fieldRegistrations;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    public int getPredictionLength() {
        return predictionLength;
    }

    public void setPredictionLength(int predictionLength) {
        this.predictionLength = predictionLength;
    }
}
