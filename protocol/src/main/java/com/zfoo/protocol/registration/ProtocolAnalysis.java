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
 */

package com.zfoo.protocol.registration;

import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.exception.UnknownException;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolDocument;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.field.*;
import com.zfoo.protocol.serializer.cs.GenerateCsUtils;
import com.zfoo.protocol.serializer.gd.GenerateGdUtils;
import com.zfoo.protocol.serializer.js.GenerateJsUtils;
import com.zfoo.protocol.serializer.lua.GenerateLuaUtils;
import com.zfoo.protocol.serializer.protobuf.GenerateProtobufUtils;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.xml.XmlProtocols;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.zfoo.protocol.ProtocolManager.*;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ProtocolAnalysis {


    // 临时变量，启动完成就会销毁，协议名称保留字符，即协议的名称不能用以下名称命名
    private static Set<String> protocolReserved = Set.of("Buffer", "ByteBuf", "ByteBuffer", "LittleEndianByteBuffer", "NormalByteBuffer"
            , "IPacket", "IProtocolRegistration", "ProtocolManager", "IFieldRegistration"
            , "ByteBufUtils", "ArrayUtils", "CollectionUtils"
            , "Boolean", "Byte", "Short", "Integer", "Long", "Float", "Double", "String", "Character", "Object"
            , "Collections", "Iterator", "List", "ArrayList", "Map", "HashMap", "Set", "HashSet");

    // 临时变量，启动完成就会销毁，是一个基本类型序列化器
    private static Map<Class<?>, ISerializer> baseSerializerMap = new HashMap<>();

    // 临时变量，启动完成就会销毁，协议Id对应的Class类
    private static final Map<Short, Class<?>> protocolClassMap = new HashMap<>(MAX_PROTOCOL_NUM);

    // 临时变量，启动完成就会销毁，协议下包含的子协议，只包含一层子协议
    private static Map<Short, Set<Short>> subProtocolIdMap = new HashMap<>(MAX_PROTOCOL_NUM);

    static {
        // 初始化基础类型序列化器
        baseSerializerMap.put(boolean.class, BooleanSerializer.INSTANCE);
        baseSerializerMap.put(Boolean.class, BooleanSerializer.INSTANCE);
        baseSerializerMap.put(byte.class, ByteSerializer.INSTANCE);
        baseSerializerMap.put(Byte.class, ByteSerializer.INSTANCE);
        baseSerializerMap.put(short.class, ShortSerializer.INSTANCE);
        baseSerializerMap.put(Short.class, ShortSerializer.INSTANCE);
        baseSerializerMap.put(int.class, IntSerializer.INSTANCE);
        baseSerializerMap.put(Integer.class, IntSerializer.INSTANCE);
        baseSerializerMap.put(long.class, LongSerializer.INSTANCE);
        baseSerializerMap.put(Long.class, LongSerializer.INSTANCE);
        baseSerializerMap.put(float.class, FloatSerializer.INSTANCE);
        baseSerializerMap.put(Float.class, FloatSerializer.INSTANCE);
        baseSerializerMap.put(double.class, DoubleSerializer.INSTANCE);
        baseSerializerMap.put(Double.class, DoubleSerializer.INSTANCE);
        baseSerializerMap.put(char.class, CharSerializer.INSTANCE);
        baseSerializerMap.put(Character.class, CharSerializer.INSTANCE);
        baseSerializerMap.put(String.class, StringSerializer.INSTANCE);
    }

    public static synchronized void analyze(Set<Class<?>> protocolClassSet, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}]已经初始完成，请不要重复初始化", ProtocolManager.class.getSimpleName());
        try {
            for (var protocolClass : protocolClassSet) {
                var id = getProtocolIdByClass(protocolClass);
                var previous = protocolClassMap.put(id, protocolClass);
                if (previous != null) {
                    throw new RunException("[{}][{}]协议号[protocolId:{}]重复", protocolClass.getCanonicalName(), previous.getCanonicalName(), id);
                }
            }

            for (var protocolClass : protocolClassSet) {
                try {
                    var registration = parseProtocolRegistration(protocolClass, ProtocolModule.DEFAULT_PROTOCOL_MODULE);
                    // 注册协议
                    protocols[registration.protocolId()] = registration;
                } catch (Exception e) {
                    throw new RuntimeException(StringUtils.format("解析协议[class:{}]异常", protocolClass), e);
                }
            }

            enhanceProtocolBefore(generateOperation);

            // 通过指定类注册的协议，全部使用字节码增强
            enhanceProtocolRegistration(Arrays.stream(protocols).filter(it -> Objects.nonNull(it)).collect(Collectors.toList()));

            enhanceProtocolAfter();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static synchronized void analyze(XmlProtocols xmlProtocols, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}]已经初始完成，请不要重复初始化", ProtocolManager.class.getSimpleName());
        try {
            var enhanceList = new ArrayList<IProtocolRegistration>();

            for (var moduleDefinition : xmlProtocols.getModules()) {
                var module = new ProtocolModule(moduleDefinition.getId(), moduleDefinition.getName(), moduleDefinition.getVersion());

                AssertionUtils.isTrue(module.getId() > 0, "[module:{}] [id:{}] 模块必须大于等于1", module.getName(), module.getId());
                AssertionUtils.isNull(modules[module.getId()], "duplicate [module:{}] [id:{}] Exception!", module.getName(), module.getId());
                AssertionUtils.notNull(moduleDefinition.getProtocols(), "[module:{}] does not have any protocols", module.getName());

                modules[module.getId()] = module;

                for (var protocolDefinition : moduleDefinition.getProtocols()) {
                    var id = protocolDefinition.getId();
                    var location = protocolDefinition.getLocation();
                    var clazz = Class.forName(location);

                    AssertionUtils.isTrue(id >= moduleDefinition.getMinId(), "模块[{}]中的协议[{}]的协议号必须大于或者等于[{}]", moduleDefinition.getName(), clazz.getSimpleName(), moduleDefinition.getMinId());
                    AssertionUtils.isTrue(id < moduleDefinition.getMaxId(), "模块[{}]中的协议[{}]的协议号必须小于[{}]", moduleDefinition.getName(), clazz.getSimpleName(), moduleDefinition.getMaxId());
                    AssertionUtils.isNull(protocols[id], "duplicate definition [id:{}] Exception!", id);

                    var packet = (IPacket) ReflectionUtils.newInstance(clazz);

                    // 协议号是否和id是否相等
                    AssertionUtils.isTrue(packet.protocolId() == id, "[class:{}]协议序列号[{}]和协议文件里的协议序列号不相等", clazz.getCanonicalName(), PROTOCOL_ID);

                    var previous = protocolClassMap.put(id, clazz);
                    if (previous != null) {
                        throw new RunException("[{}][{}]协议号[protocolId:{}]重复", clazz.getCanonicalName(), previous.getCanonicalName(), id);
                    }
                }
            }

            for (var moduleDefinition : xmlProtocols.getModules()) {
                var module = modules[moduleDefinition.getId()];
                for (var protocolDefinition : moduleDefinition.getProtocols()) {
                    var id = protocolDefinition.getId();
                    var clazz = protocolClassMap.get(id);
                    try {
                        var registration = parseProtocolRegistration(clazz, module);
                        if (protocolDefinition.isEnhance()) {
                            enhanceList.add(registration);
                        }
                        // 注册协议
                        protocols[id] = registration;
                    } catch (Exception e) {
                        throw new UnknownException(e, "解析协议[id:{}][class:{}]异常", id, clazz);
                    }
                }
            }

            enhanceProtocolBefore(generateOperation);

            enhanceProtocolRegistration(enhanceList);

            enhanceProtocolAfter();
        } catch (Exception e) {
            throw new UnknownException(e);
        }
    }

    private static void enhanceProtocolRegistration(List<IProtocolRegistration> enhanceList) throws NoSuchMethodException, IllegalAccessException, InstantiationException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchFieldException {
        // 字节码增强(生成协议解析对象)
        for (var registration : enhanceList) {
            protocols[registration.protocolId()] = EnhanceUtils.createProtocolRegistration((ProtocolRegistration) registration);
        }

        // 字节码增强过后，初始化各个子协议成员变量
        for (var registration : enhanceList) {
            var enhanceProtocolRegistration = protocols[registration.protocolId()];
            var subProtocolIds = getAllSubProtocolIds(registration.protocolId());
            for (var subProtocolId : subProtocolIds) {
                var protocolRegistrationField = enhanceProtocolRegistration.getClass().getDeclaredField(EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(subProtocolId));
                ReflectionUtils.makeAccessible(protocolRegistrationField);
                ReflectionUtils.setField(protocolRegistrationField, enhanceProtocolRegistration, protocols[subProtocolId]);
            }
        }
    }

    private static void enhanceProtocolBefore(GenerateOperation generateOperation) throws IOException, ClassNotFoundException {
        // 检查协议格式
        checkAllProtocolClass();

        // 检查模块格式
        checkAllModules();

        // 生成协议
        GenerateProtocolFile.generate(generateOperation);
    }

    private static void enhanceProtocolAfter() {
        subProtocolIdMap.clear();
        subProtocolIdMap = null;

        protocolReserved = null;

        baseSerializerMap.clear();
        baseSerializerMap = null;

        GenerateProtocolDocument.clear();
        GenerateProtocolPath.clear();
        GenerateProtocolFile.clear();
        GenerateCsUtils.clear();
        GenerateJsUtils.clear();
        GenerateLuaUtils.clear();
        GenerateGdUtils.clear();
        GenerateProtobufUtils.clear();

        EnhanceUtils.clear();
    }


    private static short checkProtocol(Class<?> clazz) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        // 是否为一个简单的javabean
        ReflectionUtils.assertIsPojoClass(clazz);
        // 是否实现了IPacket接口
        AssertionUtils.isTrue(IPacket.class.isAssignableFrom(clazz), "[class:{}]没有实现接口[IPacket:{}]", clazz.getCanonicalName(), IPacket.class.getCanonicalName());
        // 不能是泛型类
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}]不能是泛型类", clazz.getCanonicalName());

        Field protocolIdField;
        try {
            protocolIdField = clazz.getDeclaredField(PROTOCOL_ID);
        } catch (NoSuchFieldException e) {
            throw new UnknownException(e, "[class:{}]没有[{}]协议序列号", clazz.getCanonicalName(), PROTOCOL_ID);
        }

        // 是否被public修饰
        AssertionUtils.isTrue(Modifier.isPublic(protocolIdField.getModifiers()), "[class:{}]协议序列号[{}]没有被public修饰", clazz.getCanonicalName(), PROTOCOL_ID);
        // 是否被static修饰
        AssertionUtils.isTrue(Modifier.isStatic(protocolIdField.getModifiers()), "[class:{}]协议序列号[{}]没有被static修饰", clazz.getCanonicalName(), PROTOCOL_ID);
        // 是否被final修饰
        AssertionUtils.isTrue(Modifier.isFinal(protocolIdField.getModifiers()), "[class:{}]协议序列号[{}]没有被final修饰", clazz.getCanonicalName(), PROTOCOL_ID);
        // 是否被transient修饰
        AssertionUtils.isTrue(Modifier.isTransient(protocolIdField.getModifiers()), "[class:{}]协议序列号[{}]没有被transient修饰", clazz.getCanonicalName(), PROTOCOL_ID);
        // 命名只能包含字母，数字，下划线
        AssertionUtils.isTrue(clazz.getSimpleName().matches("[a-zA-Z0-9_]*"), "[class:{}]的命名只能包含字母，数字，下划线", clazz.getCanonicalName(), PROTOCOL_ID);

        // 必须要有一个空的构造器
        Constructor<?> constructor = ReflectionUtils.publicEmptyConstructor(clazz);

        ReflectionUtils.makeAccessible(protocolIdField);
        IPacket packet = (IPacket) constructor.newInstance();

        // 验证protocol()方法的返回是否和PROTOCOL_ID相等
        AssertionUtils.isTrue(Short.valueOf(packet.protocolId()).equals(protocolIdField.get(null)), "[class:{}]的protocolId返回的值和协议号的静态变量[{}]不相等", clazz.getCanonicalName(), PROTOCOL_ID);
        return packet.protocolId();
    }

    private static void checkAllModules() {
        // 模块id不能重复
        var moduleIdSet = new HashSet<Byte>();
        Arrays.stream(modules)
                .filter(it -> Objects.nonNull(it))
                .peek(it -> AssertionUtils.isTrue(!moduleIdSet.contains(it.getId()), "模块[{}]存在重复的id，模块的id不能重复", it))
                .forEach(it -> moduleIdSet.add(it.getId()));

        // 模块名称不能重复
        var moduleNameSet = new HashSet<String>();
        Arrays.stream(modules)
                .filter(it -> Objects.nonNull(it))
                .peek(it -> AssertionUtils.isTrue(!moduleNameSet.contains(it.getName()), "模块[{}]存在重复的name，模块名称不能重复", it))
                .forEach(it -> moduleNameSet.add(it.getName()));
    }

    private static void checkAllProtocolClass() {
        // 检查协议格式

        // 协议的名称不能重复
        var allProtocolNameMap = new HashMap<String, Class<?>>();
        for (var protocolRegistration : protocols) {
            if (protocolRegistration == null) {
                continue;
            }

            var protocolClass = protocolRegistration.protocolConstructor().getDeclaringClass();
            var protocolName = protocolClass.getSimpleName();
            if (allProtocolNameMap.containsKey(protocolName)) {
                throw new RunException("[class:{}]和[class:{}]协议名称重复，协议不能含有重复的名称", protocolClass.getCanonicalName(), allProtocolNameMap.get(protocolName).getCanonicalName());
            }

            if (protocolReserved.stream().anyMatch(it -> it.equalsIgnoreCase(protocolName))) {
                throw new RunException("协议的名称[class:{}]不能是保留名称[{}]", protocolClass.getCanonicalName(), protocolName);
            }

            allProtocolNameMap.put(protocolName, protocolClass);
        }


        // 检查循环协议
        for (var protocolEntry : subProtocolIdMap.entrySet()) {
            var protocolId = protocolEntry.getKey();
            var subProtocolSet = protocolEntry.getValue();
            if (subProtocolSet.contains(protocolId)) {
                var protocolClass = protocols[protocolId].protocolConstructor().getDeclaringClass();
                throw new RunException("[class:{}]在第一层包含循环引用协议[class:{}]", protocolClass.getSimpleName(), protocolClass.getSimpleName());
            }

            getAllSubProtocolIds(protocolId);
        }
    }

    private static void checkSubProtocol(Class<?> clazz, short id, Class<?> subClass) {
        var registerProtocolClass = protocolClassMap.get(id);
        if (registerProtocolClass == null || !registerProtocolClass.equals(subClass)) {
            throw new RunException("协议[{}]的子协议[{}][{}]没有注册", clazz.getCanonicalName(), id, subClass.getCanonicalName());
        }
    }

    private static ProtocolRegistration parseProtocolRegistration(Class<?> clazz, ProtocolModule module) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        var protocolId = checkProtocol(clazz);

        // 对象需要被序列化的属性
        var fields = new ArrayList<Field>();
        for (var field : clazz.getDeclaredFields()) {
            var modifiers = field.getModifiers();
            if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }

            if (Modifier.isFinal(modifiers)) {
                throw new RunException("[{}]协议号[protocolId:{}]中的[filed:{}]属性的访问修饰符不能为final"
                        , clazz.getCanonicalName(), protocolId, field.getName());
            }

            if (!Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers)) {
                throw new RunException("[{}]协议号[protocolId:{}]中的[filed:{}]属性的访问修饰符必须是public或者private"
                        , clazz.getCanonicalName(), protocolId, field.getName());
            }

            ReflectionUtils.makeAccessible(field);
            fields.add(field);
        }

        // 按变量名称从小到大排序
        fields.sort(PACKET_FIELD_COMPARATOR);

        var registrationList = new ArrayList<IFieldRegistration>();
        for (var field : fields) {
            registrationList.add(toRegistration(clazz, field));
        }

        var constructor = clazz.getDeclaredConstructor();
        ReflectionUtils.makeAccessible(constructor);
        var protocol = new ProtocolRegistration();
        protocol.setId(protocolId);
        protocol.setConstructor(constructor);
        protocol.setFields(ArrayUtils.listToArray(fields, Field.class));
        protocol.setFieldRegistrations(ArrayUtils.listToArray(registrationList, IFieldRegistration.class));
        protocol.setModule(module.getId());
        return protocol;
    }

    private static IFieldRegistration toRegistration(Class<?> clazz, Field field) {
        Class<?> fieldTypeClazz = field.getType();

        ISerializer serializer = baseSerializerMap.get(fieldTypeClazz);

        // 是一个基本类型变量
        if (serializer != null) {
            return BaseField.valueOf(serializer);
        } else if (fieldTypeClazz.getComponentType() != null) {
            // 是一个数组
            Class<?> arrayClazz = fieldTypeClazz.getComponentType();

            IFieldRegistration registration = typeToRegistration(clazz, arrayClazz);
            return ArrayField.valueOf(registration, field.getType().getComponentType());
        } else if (Set.class.isAssignableFrom(fieldTypeClazz)) {
            if (!fieldTypeClazz.equals(Set.class)) {
                throw new RunException("[class:{}]类型声明不正确，必须是Set接口类型", clazz.getCanonicalName());
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                throw new RunException("[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 1) {
                throw new RunException("[class:{}]中Set类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration registration = typeToRegistration(clazz, types[0]);
            return SetField.valueOf(registration, type);
        } else if (List.class.isAssignableFrom(fieldTypeClazz)) {
            // 是一个List
            if (!fieldTypeClazz.equals(List.class)) {
                throw new RunException("[class:{}]类型声明不正确，必须是List接口类型", clazz.getCanonicalName());
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                throw new RunException("[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 1) {
                throw new RunException("[class:{}]中List类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration registration = typeToRegistration(clazz, types[0]);
            return ListField.valueOf(registration, type);

        } else if (Map.class.isAssignableFrom(fieldTypeClazz)) {
            if (!fieldTypeClazz.equals(Map.class)) {
                throw new RunException("[class:{}]类型声明不正确，必须是Map接口类型", clazz.getCanonicalName());
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                throw new RunException("[class:{}]中数组类型声明不正确，[field:{}]不是泛型类", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 2) {
                throw new RunException("[class:{}]中数组类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration keyRegistration = typeToRegistration(clazz, types[0]);
            IFieldRegistration valueRegistration = typeToRegistration(clazz, types[1]);

            return MapField.valueOf(keyRegistration, valueRegistration, type);
        } else {
            // 是一个协议引用变量
            var referenceProtocolId = getProtocolIdByClass(field.getType());
            checkSubProtocol(clazz, referenceProtocolId, field.getType());
            subProtocolIdMap.computeIfAbsent(getProtocolIdByClass(clazz), it -> new HashSet<>()).add(referenceProtocolId);
            return ObjectProtocolField.valueOf(referenceProtocolId);
        }
    }

    private static IFieldRegistration typeToRegistration(Class<?> currentProtocolClass, Type type) {
        if (type instanceof ParameterizedType) {
            // 泛型类
            Class<?> clazz = (Class<?>) ((ParameterizedType) type).getRawType();
            if (Set.class.equals(clazz)) {
                // Set<Set<String>>
                IFieldRegistration registration = typeToRegistration(currentProtocolClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
                return SetField.valueOf(registration, type);
            } else if (List.class.equals(clazz)) {
                // List<List<String>>
                IFieldRegistration registration = typeToRegistration(currentProtocolClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
                return ListField.valueOf(registration, type);
            } else if (Map.class.equals(clazz)) {
                // Map<List<String>, List<String>>
                IFieldRegistration keyRegistration = typeToRegistration(currentProtocolClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
                IFieldRegistration valueRegistration = typeToRegistration(currentProtocolClass, ((ParameterizedType) type).getActualTypeArguments()[1]);
                return MapField.valueOf(keyRegistration, valueRegistration, type);
            }
        } else if (type instanceof Class) {
            Class<?> clazz = ((Class<?>) type);
            ISerializer serializer = baseSerializerMap.get(clazz);
            if (serializer != null) {
                // 基础类型
                return BaseField.valueOf(serializer);
            } else if (clazz.getComponentType() != null) {
                // 是一个二维以上数组
                throw new RunException("不支持多维数组或集合嵌套数组[type:{}]类型，仅支持一维数组", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                throw new RunException("不支持数组和集合联合使用[type:{}]类型", type);
            } else {
                // 是一个协议引用变量
                var referenceProtocolId = getProtocolIdByClass(clazz);
                checkSubProtocol(clazz, referenceProtocolId, clazz);
                subProtocolIdMap.computeIfAbsent(getProtocolIdByClass(currentProtocolClass), it -> new HashSet<>()).add(referenceProtocolId);
                return ObjectProtocolField.valueOf(referenceProtocolId);
            }
        }
        throw new RunException("[type:{}]类型不正确", type);
    }

    public static short getProtocolIdByClass(Class<?> clazz) {
        var protocolIdField = ReflectionUtils.getFieldByNameInPOJOClass(clazz, PROTOCOL_ID);
        ReflectionUtils.makeAccessible(protocolIdField);
        return (short) ReflectionUtils.getField(protocolIdField, null);
    }


    /**
     * 此方法仅在生成协议的时候调用，一旦运行，不能调用
     */
    public static Set<Short> getAllSubProtocolIds(short protocolId) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}]已经初始完成，初始化完成过后不能调用getAllSubProtocolIds", ProtocolAnalysis.class.getSimpleName());

        if (!subProtocolIdMap.containsKey(protocolId)) {
            return Collections.emptySet();
        }

        var protocolClass = protocols[protocolId].protocolConstructor().getDeclaringClass();

        var queue = new LinkedList<>(subProtocolIdMap.get(protocolId));
        var allSubProtocolIdSet = new HashSet<>(queue);
        while (!queue.isEmpty()) {
            var firstSubProtocolId = queue.poll();
            if (subProtocolIdMap.containsKey(firstSubProtocolId)) {
                for (var subClassId : subProtocolIdMap.get(firstSubProtocolId)) {
                    if (subClassId == protocolId) {
                        throw new RunException("[class:{}]在下层协议[class:{}]包含循环引用协议[class:{}]", protocolClass.getSimpleName(), protocols[firstSubProtocolId].protocolConstructor().getDeclaringClass(), protocolClass.getSimpleName());
                    }

                    if (!allSubProtocolIdSet.contains(subClassId)) {
                        allSubProtocolIdSet.add(subClassId);
                        queue.offer(subClassId);
                    }
                }
            }
        }
        return allSubProtocolIdSet;
    }

}
