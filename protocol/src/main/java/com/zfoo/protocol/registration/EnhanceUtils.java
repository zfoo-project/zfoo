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
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.enhance.*;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.FieldUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 对应于ProtocolRegistration
 *
 * @author godotg
 */
public abstract class EnhanceUtils {

    // 临时变量，是一个基本类型序列化器对应的增强类型序列化器
    private static Map<ISerializer, IEnhanceSerializer> tempEnhanceSerializerMap = new HashMap<>();

    public static String byteBufUtils = ByteBufUtils.class.getSimpleName();
    public static String byteBufUtilsWriteBooleanFalse = byteBufUtils + ".writeBoolean($1, false);";
    public static String byteBufUtilsWriteBooleanTrue = byteBufUtils + ".writeBoolean($1, true);";
    public static String byteBufUtilsReadBoolean = byteBufUtils + ".readBoolean($1)";
    public static String byteBufUtilsWriteInt0 = byteBufUtils + ".writeInt($1, 0);";

    static {
        var classArray = new Class<?>[]{IProtocolRegistration.class, IFieldRegistration.class, ByteBuf.class};

        var classPool = ClassPool.getDefault();

        // 导入需要的包
        classPool.importPackage(ByteBufUtils.class.getName());
        classPool.importPackage(CollectionUtils.class.getName());
        classPool.importPackage(ArrayUtils.class.getName());
        classPool.importPackage(Iterator.class.getName());
        classPool.importPackage(List.class.getName());
        classPool.importPackage(Map.class.getName());
        classPool.importPackage(Set.class.getName());

        // 增加类的路径
        for (var clazz : classArray) {
            if (classPool.find(clazz.getName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }

        tempEnhanceSerializerMap.put(BooleanSerializer.INSTANCE, new EnhanceBooleanSerializer());
        tempEnhanceSerializerMap.put(ByteSerializer.INSTANCE, new EnhanceByteSerializer());
        tempEnhanceSerializerMap.put(ShortSerializer.INSTANCE, new EnhanceShortSerializer());
        tempEnhanceSerializerMap.put(IntSerializer.INSTANCE, new EnhanceIntSerializer());
        tempEnhanceSerializerMap.put(LongSerializer.INSTANCE, new EnhanceLongSerializer());
        tempEnhanceSerializerMap.put(FloatSerializer.INSTANCE, new EnhanceFloatSerializer());
        tempEnhanceSerializerMap.put(DoubleSerializer.INSTANCE, new EnhanceDoubleSerializer());
        tempEnhanceSerializerMap.put(StringSerializer.INSTANCE, new EnhanceStringSerializer());
        tempEnhanceSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new EnhanceObjectProtocolSerializer());
        tempEnhanceSerializerMap.put(ListSerializer.INSTANCE, new EnhanceListSerializer());
        tempEnhanceSerializerMap.put(SetSerializer.INSTANCE, new EnhanceSetSerializer());
        tempEnhanceSerializerMap.put(MapSerializer.INSTANCE, new EnhanceMapSerializer());
        tempEnhanceSerializerMap.put(ArraySerializer.INSTANCE, new EnhanceArraySerializer());
    }

    public static IEnhanceSerializer enhanceSerializer(ISerializer serializer) {
        return tempEnhanceSerializerMap.get(serializer);
    }

    public static void clear() {
        tempEnhanceSerializerMap.clear();
        tempEnhanceSerializerMap = null;

        byteBufUtils = null;
        byteBufUtilsWriteBooleanFalse = null;
        byteBufUtilsWriteBooleanTrue = null;
        byteBufUtilsReadBoolean = null;
        byteBufUtilsWriteInt0 = null;
    }

    /**
     * @param registration 需要增强的类
     * @return 返回类的名称格式：EnhanceUtilsProtocolRegistration1
     */
    public static IProtocolRegistration createProtocolRegistration(ProtocolRegistration registration) throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        GenerateProtocolFile.index.set(0);

        var classPool = ClassPool.getDefault();
        var protocolId = registration.getId();
        var packetFields = registration.getFieldRegistrations();

        // 定义类名称
        CtClass enhanceClazz = classPool.makeClass(ProtocolRegistration.class.getName() + protocolId);
        enhanceClazz.addInterface(classPool.get(IProtocolRegistration.class.getName()));

        // 定义类中的一个成员
        CtField constructorFiled = new CtField(classPool.get(Constructor.class.getName()), "constructor", enhanceClazz);
        constructorFiled.setModifiers(Modifier.PRIVATE);
        enhanceClazz.addField(constructorFiled);


        // 定义类所包含的所有子协议成员
        var allSubProtocolIds = ProtocolAnalysis.getAllSubProtocolIds(protocolId)
                .stream()
                .sorted((a, b) -> Short.compare(a, b))
                .toList();

        for (var subProtocolId : allSubProtocolIds) {
            var protocolRegistrationField = new CtField(classPool.get(IProtocolRegistration.class.getName()), getProtocolRegistrationFieldNameByProtocolId(subProtocolId), enhanceClazz);
            protocolRegistrationField.setModifiers(Modifier.PRIVATE);
            enhanceClazz.addField(protocolRegistrationField);
        }

        // 定义类的构造器
        CtConstructor constructor = new CtConstructor(classPool.get(new String[]{Constructor.class.getName()}), enhanceClazz);
        constructor.setBody("{this.constructor=$1;}");
        constructor.setModifiers(Modifier.PUBLIC);
        enhanceClazz.addConstructor(constructor);

        // 定义类实现的接口方法
        CtMethod protocolIdMethod = new CtMethod(classPool.get(short.class.getName()), "protocolId", null, enhanceClazz);
        protocolIdMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        protocolIdMethod.setBody("{return " + registration.protocolId() + ";}");
        enhanceClazz.addMethod(protocolIdMethod);

        CtMethod protocolConstructorMethod = new CtMethod(classPool.get(Constructor.class.getName()), "protocolConstructor", null, enhanceClazz);
        protocolConstructorMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        protocolConstructorMethod.setBody("{return this.constructor;}");
        enhanceClazz.addMethod(protocolConstructorMethod);

        CtMethod moduleMethod = new CtMethod(classPool.get(byte.class.getName()), "module", null, enhanceClazz);
        moduleMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        moduleMethod.setBody("{return " + registration.module() + ";}");
        enhanceClazz.addMethod(moduleMethod);

        CtMethod writeMethod = new CtMethod(classPool.get(void.class.getName()), "write", classPool.get(new String[]{ByteBuf.class.getName(), Object.class.getName()}), enhanceClazz);
        writeMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        writeMethod.setBody(writeMethodBody(registration));
        enhanceClazz.addMethod(writeMethod);

        CtMethod readMethod = new CtMethod(classPool.get(Object.class.getName()), "read", classPool.get(new String[]{ByteBuf.class.getName()}), enhanceClazz);
        readMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        readMethod.setBody(readMethodBody(registration));
        enhanceClazz.addMethod(readMethod);

        // 释放缓存
        enhanceClazz.detach();

        Class<?> resultClazz = enhanceClazz.toClass(IProtocolRegistration.class);
        Constructor<?> resultConstructor = resultClazz.getConstructor(Constructor.class);

        return (IProtocolRegistration) resultConstructor.newInstance(registration.protocolConstructor());
    }

    // see: ProtocolRegistration.write()
    private static String writeMethodBody(ProtocolRegistration registration) {
        var constructor = registration.getConstructor();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var packetClazz = constructor.getDeclaringClass();

        var builder = new StringBuilder();
        builder.append("{");
        builder.append("if ($2 == null) { $1.writeByte(0); return; }");
        builder.append(StringUtils.format("{} packet = ({})$2;", packetClazz.getName(), packetClazz.getName()));
        if (registration.isCompatible()) {
            builder.append("int beforeWriteIndex = $1.writerIndex();");
            builder.append(StringUtils.format("{}.writeInt($1, {});", byteBufUtils, registration.getPredictionLength()));
        } else {
            builder.append("$1.writeByte(1);");
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (Modifier.isPublic(field.getModifiers())) {
                enhanceSerializer(fieldRegistration.serializer())
                        .writeObject(builder, StringUtils.format("packet.{}", field.getName()), field, fieldRegistration);
            } else {
                enhanceSerializer(fieldRegistration.serializer())
                        .writeObject(builder, StringUtils.format("packet.{}()", FieldUtils.fieldToGetMethod(packetClazz, field)), field, fieldRegistration);
            }
        }
        if (registration.isCompatible()) {
            builder.append(StringUtils.format("{}.adjustPadding($1, {}, beforeWriteIndex);", byteBufUtils, registration.getPredictionLength()));
        }
        builder.append("}");
        return builder.toString();
    }

    // see: ProtocolRegistration.read()
    private static String readMethodBody(ProtocolRegistration registration) {
        var constructor = registration.getConstructor();
        var fieldRegistrations = registration.getFieldRegistrations();

        var builder = new StringBuilder();
        builder.append("{");
        builder.append(StringUtils.format("int length = {}.readInt($1);", byteBufUtils));
        builder.append("if(length==0){return null;}");
        builder.append("int beforeReadIndex = $1.readerIndex();");
        var packetClazz = constructor.getDeclaringClass();
        if (packetClazz.isRecord()) {
            var fields = registration.getFields();
            var originFields = ProtocolAnalysis.getFields(packetClazz);
            var constructorParams = new String[originFields.size()];

            for (var i = 0; i < fields.length; i++) {
                var field = fields[i];
                var fieldRegistration = fieldRegistrations[i];
                int index = originFields.indexOf(field);
                // protocol backwards compatibility，协议向后兼容
                if (field.isAnnotationPresent(Compatible.class)) {
                    var defaultReadObject = enhanceSerializer(fieldRegistration.serializer()).defaultValue(builder, field, fieldRegistration);
                    builder.append(StringUtils.format("if ({}.compatibleRead($1, beforeReadIndex, length)) {", byteBufUtils));
                    var compatibleReadObject = enhanceSerializer(fieldRegistration.serializer()).readObject(builder, field, fieldRegistration);
                    builder.append(StringUtils.format("{} = {};", defaultReadObject, compatibleReadObject));
                    builder.append("}");
                    constructorParams[index] = defaultReadObject;
                    continue;
                }

                var readObject = enhanceSerializer(fieldRegistration.serializer()).readObject(builder, field, fieldRegistration);
                constructorParams[index] = readObject;
            }
            builder.append(packetClazz.getName() + " packet=new " + packetClazz.getName() + "(" + String.join(StringUtils.COMMA, constructorParams) + ");");
        } else {
            var fields = registration.getFields();
            builder.append(packetClazz.getName() + " packet=new " + packetClazz.getName() + "();");

            for (var i = 0; i < fields.length; i++) {
                var field = fields[i];
                var fieldRegistration = fieldRegistrations[i];
                // protocol backwards compatibility，协议向后兼容
                if (field.isAnnotationPresent(Compatible.class)) {
                    builder.append(StringUtils.format("if ({}.compatibleRead($1, beforeReadIndex, length)) {", byteBufUtils));
                    var compatibleReadObject = enhanceSerializer(fieldRegistration.serializer()).readObject(builder, field, fieldRegistration);
                    if (Modifier.isPublic(field.getModifiers())) {
                        builder.append(StringUtils.format("packet.{}={};", field.getName(), compatibleReadObject));
                    } else {
                        builder.append(StringUtils.format("packet.{}({});", FieldUtils.fieldToSetMethod(packetClazz, field), compatibleReadObject));
                    }
                    builder.append("} else {");
                    var defaultReadObject = enhanceSerializer(fieldRegistration.serializer()).defaultValue(builder, field, fieldRegistration);
                    if (Modifier.isPublic(field.getModifiers())) {
                        builder.append(StringUtils.format("packet.{}={};", field.getName(), defaultReadObject));
                    } else {
                        builder.append(StringUtils.format("packet.{}({});", FieldUtils.fieldToSetMethod(packetClazz, field), defaultReadObject));
                    }
                    builder.append("}");
                    continue;
                }
                var readObject = enhanceSerializer(fieldRegistration.serializer()).readObject(builder, field, fieldRegistration);
                if (Modifier.isPublic(field.getModifiers())) {
                    builder.append(StringUtils.format("packet.{}={};", field.getName(), readObject));
                } else {
                    builder.append(StringUtils.format("packet.{}({});", FieldUtils.fieldToSetMethod(packetClazz, field), readObject));
                }
            }
        }

        builder.append("if (length > 0) { $1.readerIndex(beforeReadIndex + length); }");

        builder.append("return packet;}");
        return builder.toString();
    }

    public static String getProtocolRegistrationFieldNameByProtocolId(short id) {
        return StringUtils.format("{}{}", StringUtils.uncapitalize(ProtocolRegistration.class.getSimpleName()), id);
    }
}
