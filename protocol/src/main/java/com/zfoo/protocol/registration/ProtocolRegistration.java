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
        this.predictionLength = 128;
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
            ByteBufUtils.writeByte(byteBuf, (byte) 0);
            return;
        }

        if (compatible) {
            byteBuf.markWriterIndex();
            ByteBufUtils.writeInt(byteBuf, predictionLength);
        } else {
            ByteBufUtils.writeInt(byteBuf, -1);
        }

        var beforeWriteIndex = byteBuf.writerIndex();
        for (int i = 0, length = fields.length; i < length; i++) {
            Field field = fields[i];
            IFieldRegistration packetFieldRegistration = fieldRegistrations[i];
            ISerializer serializer = packetFieldRegistration.serializer();
            Object fieldValue = ReflectionUtils.getField(field, packet);
            serializer.writeObject(byteBuf, fieldValue, packetFieldRegistration);
        }

        if (compatible) {
            // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
            var currentWriteIndex = byteBuf.writerIndex();
            var length = currentWriteIndex - beforeWriteIndex;
            var lengthCount = ByteBufUtils.writeIntCount(length);
            var padding = lengthCount - ByteBufUtils.writeIntCount(predictionLength);
            if (padding == 0) {
                byteBuf.resetWriterIndex();
                ByteBufUtils.writeInt(byteBuf, length);
                byteBuf.writerIndex(currentWriteIndex);
            } else if (padding < 0) {
                var retainedByteBuf = byteBuf.retainedSlice(currentWriteIndex - length, length);
                byteBuf.resetWriterIndex();
                ByteBufUtils.writeInt(byteBuf, length);
                byteBuf.writeBytes(retainedByteBuf);
                ReferenceCountUtil.release(retainedByteBuf);
            } else {
                var retainedByteBuf = byteBuf.retainedSlice(currentWriteIndex - length, length);
                var bytes = ByteBufUtils.readAllBytes(retainedByteBuf);
                byteBuf.resetWriterIndex();
                ByteBufUtils.writeInt(byteBuf, length);
                byteBuf.writeBytes(bytes);
                ReferenceCountUtil.release(retainedByteBuf);
            }
        }
    }

    @Override
    public Object read(ByteBuf byteBuf) {
        var length = ByteBufUtils.readInt(byteBuf);
        if (length == 0) {
            return null;
        }
        Object object = ReflectionUtils.newInstance(constructor);

        var readIndex = byteBuf.readerIndex();
        for (int i = 0, j = fields.length; i < j; i++) {
            Field field = fields[i];
            // 协议向后兼容
            if (field.isAnnotationPresent(Compatible.class)) {
                if (length == -1 || byteBuf.readerIndex() - readIndex >= length) {
                    break;
                }
            }
            IFieldRegistration packetFieldRegistration = fieldRegistrations[i];
            ISerializer serializer = packetFieldRegistration.serializer();
            Object fieldValue = serializer.readObject(byteBuf, packetFieldRegistration);
            ReflectionUtils.setField(field, object, fieldValue);
        }

        if (length > 0) {
            byteBuf.readerIndex(readIndex + length);
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

}
