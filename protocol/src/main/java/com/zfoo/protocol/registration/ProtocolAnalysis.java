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

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.exception.UnknownException;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.field.*;
import com.zfoo.protocol.serializer.cpp.GenerateCppUtils;
import com.zfoo.protocol.serializer.csharp.GenerateCsUtils;
import com.zfoo.protocol.serializer.gdscript.GenerateGdUtils;
import com.zfoo.protocol.serializer.go.GenerateGoUtils;
import com.zfoo.protocol.serializer.javascript.GenerateJsUtils;
import com.zfoo.protocol.serializer.lua.GenerateLuaUtils;
import com.zfoo.protocol.serializer.protobuf.GenerateProtobufUtils;
import com.zfoo.protocol.serializer.python.GeneratePyUtils;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.serializer.typescript.GenerateTsUtils;
import com.zfoo.protocol.util.*;
import com.zfoo.protocol.xml.XmlProtocols;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import static com.zfoo.protocol.ProtocolManager.*;

/**
 * @author godotg
 */
public class ProtocolAnalysis {

    /**
     * EN: Temp field will be destroyed after startup, and the class corresponding to the protocolId
     * CN: 临时变量，启动完成就会销毁，协议Id对应的Class类
     */
    private static Map<Short, Class<?>> protocolClassMap = new HashMap<>(MAX_PROTOCOL_NUM);

    /**
     * EN: Temp field, sub protocols
     * CN: 临时变量，启动完成就会销毁，协议下包含的子协议，只包含一层子协议
     */
    private static Map<Short, Set<Short>> subProtocolIdMap = new HashMap<>(MAX_PROTOCOL_NUM);

    /**
     * EN: Temp field, name reserved keywords, The name of the protocol cannot be named with the following name
     * CN: 临时变量，启动完成就会销毁，协议名称保留字符，即协议的名称不能用以下名称命名
     */
    private static Set<String> protocolReserved = Set.of("Buffer", "ByteBuf", "ByteBuffer", "LittleEndianByteBuffer", "NormalByteBuffer"
            , "IProtocol", "IProtocolRegistration", "ProtocolManager", "IFieldRegistration"
            , "ByteBufUtils", "ArrayUtils", "CollectionUtils"
            , "Boolean", "Byte", "Short", "Integer", "Long", "Float", "Double", "String", "Character", "Object"
            , "Collections", "Iterator", "List", "ArrayList", "Map", "HashMap", "Set", "HashSet");

    /**
     * EN: Temp field, unsupported type
     * CN: 临时变量，启动完成就会销毁，不支持的类型
     */
    private static Set<Class<?>> unsupportedTypes = Set.of(char.class, Character.class);

    /**
     * EN: Temp field, base type serializer
     * CN: 临时变量，启动完成就会销毁，是基本类型序列化器
     */
    private static Map<Class<?>, ISerializer> baseSerializerMap = new HashMap<>(128);

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
        baseSerializerMap.put(String.class, StringSerializer.INSTANCE);
    }

    /**
     * 真正的注册协议，将协议id和协议信息关联起来
     */
    public static synchronized void analyze(Set<Class<?>> protocolClassSet, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}]已经初始完成，请不要重复初始化", ProtocolManager.class.getSimpleName());
        // 检查协议类是否合法
        for (var protocolClass : protocolClassSet) {
            var protocolId = getProtocolIdAndCheckClass(protocolClass);
            AssertionUtils.isTrue(protocolId >= 0, "[class:{}]必须使用注解@Protocol注解标注", protocolClass.getCanonicalName());
            initProtocolClass(protocolId, protocolClass);
        }

        // 协议id和协议信息对应起来
        for (var protocolClass : protocolClassSet) {
            var registration = parseProtocolRegistration(protocolClass, ProtocolModule.DEFAULT_PROTOCOL_MODULE);
            protocols[registration.protocolId()] = registration;
        }

        // 通过指定类注册的协议，全部使用字节码增强
        var enhanceList = Arrays.stream(protocols).filter(Objects::nonNull).toList();
        enhance(generateOperation, enhanceList);
    }

    public static synchronized void analyzeAuto(Set<Class<?>> protocolClassSet, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}]已经初始完成，请不要重复初始化", ProtocolManager.class.getSimpleName());
        // 获取所有协议类
        var relevantClassSet = new HashSet<>(protocolClassSet);
        for (var clazz : protocolClassSet) {
            relevantClassSet.addAll(ClassUtils.relevantClass(clazz));
        }

        var relevantClassList = relevantClassSet.stream()
                .sorted((a, b) -> a.getCanonicalName().compareTo(b.getCanonicalName()))
                .toList();

        // 检查协议类是否合法
        var noProtocolIds = new ArrayList<Class<?>>();
        for (var protocolClass : relevantClassList) {
            var protocolId = getProtocolIdAndCheckClass(protocolClass);
            if (protocolId >= 0) {
                initProtocolClass(protocolId, protocolClass);
            } else {
                noProtocolIds.add(protocolClass);
            }
        }
        var countProtocolId = (short) 0;
        for (var protocolClass : noProtocolIds) {
            while (protocolClassMap.containsKey(countProtocolId)) {
                countProtocolId++;
            }
            initProtocolClass(countProtocolId, protocolClass);
        }

        // 协议id和协议信息对应起来
        for (var protocolClass : relevantClassSet) {
            var registration = parseProtocolRegistration(protocolClass, ProtocolModule.DEFAULT_PROTOCOL_MODULE);
            protocols[registration.protocolId()] = registration;
        }

        // 通过指定类注册的协议，全部使用字节码增强
        var enhanceList = Arrays.stream(protocols).filter(Objects::nonNull).toList();
        enhance(generateOperation, enhanceList);
    }

    public static synchronized void analyze(XmlProtocols xmlProtocols, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}]已经初始完成，请不要重复初始化", ProtocolManager.class.getSimpleName());

        var protocolXmlEnhanceMap = new HashMap<Class<?>, Boolean>();
        var classModuleDefinitionMap = new HashMap<Class<?>, Byte>();
        var moduleDefinitionClassMap = new HashMap<Byte, Set<Class<?>>>();

        // 先注册类，再注册包
        for (var moduleDefinition : xmlProtocols.getModules()) {
            var moduleId = moduleDefinition.getId();
            var module = new ProtocolModule(moduleId, moduleDefinition.getName());
            AssertionUtils.isTrue(module.getId() > 0, "[module:{}] [id:{}] 模块必须大于等于1", module.getName(), moduleId);
            AssertionUtils.isNull(modules[module.getId()], "duplicate [module:{}] [id:{}] Exception!", module.getName(), moduleId);

            modules[module.getId()] = module;
            if (CollectionUtils.isEmpty(moduleDefinition.getProtocols())) {
                continue;
            }
            //模块定义所有协议
            var protocolModuleSet = moduleDefinitionClassMap.computeIfAbsent(module.getId(), it -> new HashSet<>());
            for (var protocolDefinition : moduleDefinition.getProtocols()) {
                var id = protocolDefinition.getId();
                var location = protocolDefinition.getLocation();
                var enhance = protocolDefinition.isEnhance();

                // Use the class path first to obtain the class name, and search the directory if it cannot be obtained
                // 优先使用类路径获取类名，获取不到才去搜索目录
                Class<?> clazz = null;
                try {
                    clazz = ClassUtils.forName(location);
                } catch (Exception e) {
                }

                // 如果定义的是类，则需要检查一下格式
                if (clazz == null) {
                    continue;
                }

                var protocolId = getProtocolIdAndCheckClass(clazz);
                // 没有使用Protocol注解，则使用xml定义的protocolId
                if (protocolId < 0) {
                    if (id < 0) {
                        throw new RunException("[{}] Can not find protocol id, use @Protocol annotation or specify id in xml", clazz.getSimpleName());
                    }
                    protocolId = id;
                } else {
                    if (id >= 0 && protocolId != id) {
                        throw new RunException("[{}] @Protocol annotation id not equal to id in xml", clazz.getSimpleName());
                    }
                }

                initProtocolClass(protocolId, clazz);
                protocolModuleSet.add(clazz);
                classModuleDefinitionMap.put(clazz, moduleId);
                protocolXmlEnhanceMap.put(clazz, enhance);
            }
        }

        // 再注册包路径，扫描包开始注册
        for (var moduleDefinition : xmlProtocols.getModules()) {
            var moduleId = moduleDefinition.getId();
            var module = modules[moduleId];
            if (CollectionUtils.isEmpty(moduleDefinition.getProtocols())) {
                continue;
            }

            //模块定义所有协议
            var protocolModuleSet = moduleDefinitionClassMap.computeIfAbsent(module.getId(), it -> new HashSet<>());
            for (var protocolDefinition : moduleDefinition.getProtocols()) {
                var id = protocolDefinition.getId();
                var location = protocolDefinition.getLocation();
                var enhance = protocolDefinition.isEnhance();

                // Use the class path first to obtain the class name, and search the directory if it cannot be obtained
                // 优先使用类路径获取类名，获取不到才去搜索目录
                Class<?> clazz = null;
                try {
                    clazz = ClassUtils.forName(location);
                } catch (Exception e) {
                }

                // 如果定义的是类，则需要检查一下格式
                if (clazz != null) {
                    continue;
                }

                var packetClazzList = scanPackageList(protocolDefinition.getLocation());
                // 是类路径的话一定不能指定protocol id
                if (CollectionUtils.isEmpty(packetClazzList)) {
                    throw new RunException("can not scan any protocol class in [{}]", location);
                }
                if (id >= 0) {
                    throw new RunException("When use package location, specify protocol id in xml");
                }

                for (Class<?> protocolClass : packetClazzList) {
                    // 如果location已经指定过了，则检测格式
                    if (classModuleDefinitionMap.containsKey(protocolClass)) {
                        var xmlProtocolModuleId = classModuleDefinitionMap.get(protocolClass);
                        if (xmlProtocolModuleId != moduleId) {
                            throw new RunException("[class:{}] defined two different [module:[{}][{}]]", protocolClass.getName(), xmlProtocolModuleId, module.getId());
                        }
                        continue;
                    }
                    var protocolId = getProtocolIdAndCheckClass(protocolClass);
                    initProtocolClass(protocolId, protocolClass);
                    protocolModuleSet.add(protocolClass);
                    classModuleDefinitionMap.put(clazz, moduleId);
                    protocolXmlEnhanceMap.compute(clazz, (key, value) -> Boolean.TRUE.equals(value) || enhance);
                }
            }
        }

        var enhanceList = new ArrayList<IProtocolRegistration>();
        for (var moduleDefinition : xmlProtocols.getModules()) {
            var module = modules[moduleDefinition.getId()];
            var packetClazzList = moduleDefinitionClassMap.get(moduleDefinition.getId());
            if (CollectionUtils.isEmpty(packetClazzList)) {
                continue;
            }
            for (Class<?> clazz : packetClazzList) {
                var protocolId = ProtocolManager.protocolId(clazz);
                var registration = parseProtocolRegistration(clazz, module);
                // Protocol注解或者xml任意一个定义了增强协议，那么就增强协议
                if ((clazz.isAnnotationPresent(Protocol.class) && clazz.getAnnotation(Protocol.class).enhance()) || protocolXmlEnhanceMap.get(clazz)) {
                    enhanceList.add(registration);
                }
                // 注册协议
                protocols[protocolId] = registration;
            }
        }

        enhance(generateOperation, enhanceList);
    }

    public static List<Class<?>> scanPackageList(String location) {
        // 获取该路径下所有类
        var clazzNameSet = new HashSet<String>();
        try {
            var clazzList = ClassUtils.getAllClasses(location);
            clazzNameSet.addAll(clazzList);
        } catch (Exception e) {
            throw new RunException("[{}] scanning exception", location, e);
        }
        var classes = clazzNameSet.stream()
                .map(it -> ClassUtils.forName(it))
                .filter(it -> it.isAnnotationPresent(Protocol.class))
                .filter(it -> !it.isInterface())
                .filter(it -> !it.isEnum())
                .distinct()
                .toList();
        return new ArrayList<>(classes);
    }

    private static void enhance(GenerateOperation generateOperation, List<IProtocolRegistration> enhanceList) {
        try {
            enhanceProtocolBefore(generateOperation);
            enhanceProtocolRegistration(enhanceList);
            enhanceProtocolAfter(generateOperation);
        } catch (Exception e) {
            throw new UnknownException(e);
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

    private static void enhanceProtocolRegistration(List<IProtocolRegistration> enhanceList) throws NoSuchMethodException, IllegalAccessException, InstantiationException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchFieldException {
        if (GraalVmUtils.isGraalVM()) {
            return;
        }
        // 字节码增强
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

    private static void enhanceProtocolAfter(GenerateOperation generateOperation) {
        var distinctHashcode = protocolIdMap.keySet().stream().map(Object::hashCode).distinct().count();
        if (distinctHashcode == protocolIdMap.size()) {
            protocolIdMap = null;
        } else {
            protocolIdPrimitiveMap = null;
        }

        protocolClassMap = null;
        subProtocolIdMap = null;
        protocolReserved = null;
        baseSerializerMap = null;
        unsupportedTypes = null;

        EnhanceUtils.clear();

        if (CollectionUtils.isEmpty(generateOperation.getGenerateLanguages())) {
            return;
        }

        GenerateProtocolNote.clear();
        GenerateProtocolPath.clear();
        GenerateProtocolFile.clear();
        GenerateCppUtils.clear();
        GenerateGoUtils.clear();
        GenerateCsUtils.clear();
        GenerateJsUtils.clear();
        GenerateTsUtils.clear();
        GenerateLuaUtils.clear();
        GenerateGdUtils.clear();
        GeneratePyUtils.clear();
        GenerateProtobufUtils.clear();
    }

    public static List<Field> getFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (var field : clazz.getDeclaredFields()) {
            var modifiers = field.getModifiers();
            if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }
            if (!clazz.isRecord() && Modifier.isFinal(modifiers)) {
                throw new RunException("[{}]协议号中的[field:{}]属性的访问修饰符不能为final", clazz.getCanonicalName(), field.getName());
            }
            if (!Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers)) {
                throw new RunException("[{}]协议号中的[field:{}]属性的访问修饰符必须是public或者private", clazz.getCanonicalName(), field.getName());
            }

            ReflectionUtils.makeAccessible(field);
            fields.add(field);
        }
        return fields;
    }

    private static List<Field> customFieldOrder(Class<?> clazz, List<Field> fields) {
        var notCompatibleFields = new ArrayList<Field>();
        var compatibleFieldMap = new HashMap<Integer, Field>();
        for (var field : fields) {
            if (field.isAnnotationPresent(Compatible.class)) {
                var order = field.getAnnotation(Compatible.class).value();
                var oldField = compatibleFieldMap.put(order, field);
                if (oldField != null) {
                    throw new RunException("[{}]协议号中的[field:{}]和[field:{}]不能有相同的Compatible顺序[order:{}]", clazz.getCanonicalName(), oldField.getName(), field.getName(), oldField, order);
                }
            } else {
                notCompatibleFields.add(field);
            }
        }

        // 默认无法兼容的协议变量名称从小到大排序，如果想自定义私有协议规则，修改这个排序规则即可
        // 如果为了增加协议的安全性，每个版本都可以重新修改协议排序规则，让每个版本的协议都不相同，间接实现加密
        notCompatibleFields.sort((a, b) -> a.getName().compareTo(b.getName()));

        // 可兼容的协议变量默认都添加到最后
        var compatibleFields = compatibleFieldMap.entrySet()
                .stream()
                .sorted((a, b) -> a.getKey() - b.getKey())
                .map(Map.Entry::getValue)
                .toList();
        notCompatibleFields.addAll(compatibleFields);
        return notCompatibleFields;
    }

    private static ProtocolRegistration parseProtocolRegistration(Class<?> clazz, ProtocolModule module) {
        var protocolId = ProtocolManager.protocolId(clazz);
        var declaredFields = getFields(clazz);
        // 对象需要被序列化的属性
        var fields = customFieldOrder(clazz, declaredFields);

        try {
            var registrationList = new ArrayList<IFieldRegistration>();
            boolean isRecord = clazz.isRecord();
            for (var field : fields) {
                registrationList.add(toRegistration(clazz, field));
            }

            Constructor constructor;
            if (isRecord) {
                constructor = ReflectionUtils.getConstructor(clazz, declaredFields.stream().map(p -> p.getType()).toList().toArray(new Class[]{}));
            } else {
                constructor = clazz.getDeclaredConstructor();
            }

            ReflectionUtils.makeAccessible(constructor);
            return new ProtocolRegistration(protocolId, module.getId(), constructor, ArrayUtils.listToArray(fields, Field.class), ArrayUtils.listToArray(registrationList, IFieldRegistration.class));
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("解析协议[class:{}]异常", clazz), e);
        }
    }

    private static IFieldRegistration toRegistration(Class<?> clazz, Field field) {
        Class<?> fieldTypeClazz = field.getType();

        ISerializer serializer = baseSerializerMap.get(fieldTypeClazz);

        // 是一个基本类型变量
        if (serializer != null) {
            return BaseField.valueOf(serializer);
        } else if (fieldTypeClazz.isArray()) {
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
                throw new RunException("[class:{}]中Map类型声明不正确，[field:{}]不是泛型类", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 2) {
                throw new RunException("[class:{}]中Map类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration keyRegistration = typeToRegistration(clazz, types[0]);
            IFieldRegistration valueRegistration = typeToRegistration(clazz, types[1]);

            return MapField.valueOf(keyRegistration, valueRegistration, type);
        } else {
            checkUnsupportedType(fieldTypeClazz);
            // 是一个协议引用变量
            if (!protocolIdMap.containsKey(fieldTypeClazz)) {
                throw new RunException("sub protocol:[{}] needs to register in protocol:[{}]", fieldTypeClazz.getCanonicalName(), clazz.getCanonicalName());
            }
            var protocolId = ProtocolManager.protocolId(clazz);
            var subProtocolId = ProtocolManager.protocolId(fieldTypeClazz);
            subProtocolIdMap.computeIfAbsent(protocolId, it -> new HashSet<>()).add(subProtocolId);
            return ObjectProtocolField.valueOf(subProtocolId);
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
            } else if (clazz.isArray()) {
                // 是一个二维以上数组
                throw new RunException("不支持多维数组或集合嵌套数组[type:{}]类型，仅支持一维数组", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                throw new RunException("不支持数组和集合联合使用[type:{}]类型", type);
            } else {
                checkUnsupportedType(clazz);
                // 是一个协议引用变量
                if (!protocolIdMap.containsKey(clazz)) {
                    throw new RunException("sub protocol:[{}] needs to register in protocol:[{}]", clazz.getCanonicalName(), currentProtocolClass.getCanonicalName());
                }
                var protocolId = ProtocolManager.protocolId(currentProtocolClass);
                var subProtocolId = ProtocolManager.protocolId(clazz);
                subProtocolIdMap.computeIfAbsent(protocolId, it -> new HashSet<>()).add(subProtocolId);
                return ObjectProtocolField.valueOf(subProtocolId);
            }
        }
        throw new RunException("[type:{}] is incorrect", type);
    }


    /**
     * 此方法仅在生成协议的时候调用，一旦运行，不能调用
     */
    public static Set<Short> getFirstSubProtocolIds(short protocolId) {
        return subProtocolIdMap.get(protocolId);
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

    // 协议智能语法分析，错误的协议定义将无法启动程序并给出错误警告
    //-----------------------------------------------------------------------
    private static void initProtocolClass(short protocolId, Class<?> clazz) {
        protocolIdMap.put(clazz, protocolId);
        protocolIdPrimitiveMap.putPrimitive(clazz.hashCode(), protocolId);
        var previous = protocolClassMap.put(protocolId, clazz);
        if (previous != null) {
            throw new RunException("[{}][{}]协议号[protocolId:{}]重复", clazz.getCanonicalName(), previous.getCanonicalName(), protocolId);
        }
    }

    public static short getProtocolIdAndCheckClass(Class<?> clazz) {
        checkUnsupportedType(clazz);
        // 是否为一个简单的javabean
        ReflectionUtils.assertIsPojoClass(clazz);
        // 不能是泛型类
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}]不能是泛型类", clazz.getCanonicalName());

        // 普通Pojo必须要有一个空的构造器
        if (!clazz.isRecord()) {
            Constructor<?> constructor = ReflectionUtils.publicEmptyConstructor(clazz);
        }

        var protocolAnnotation = clazz.getDeclaredAnnotation(Protocol.class);
        short protocolId = -1;
        if (protocolAnnotation != null) {
            protocolId = protocolAnnotation.id();
        }

        return protocolId;
    }

    private static void checkUnsupportedType(Class<?> clazz) {
        if (clazz.isEnum()) {
            throw new RunException("[{}] enum is not supported, since other language not support enum", clazz.getSimpleName());
        }
        if (unsupportedTypes.stream().anyMatch(it -> clazz.isAssignableFrom(it))) {
            throw new RunException("[{}] is not supported, since other language not support it", clazz.getSimpleName());
        }
    }

    private static void checkAllModules() {
        // 模块id不能重复
        var moduleIdSet = new HashSet<Byte>();
        Arrays.stream(modules)
                .filter(Objects::nonNull)
                .peek(it -> AssertionUtils.isTrue(!moduleIdSet.contains(it.getId()), "模块[{}]存在重复的id，模块的id不能重复", it))
                .forEach(it -> moduleIdSet.add(it.getId()));

        // 模块名称不能重复
        var moduleNameSet = new HashSet<String>();
        Arrays.stream(modules)
                .filter(Objects::nonNull)
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


        // 拓扑排序检查循环协议
        if (subProtocolIdMap.isEmpty()) {
            return;
        }
        // 先判断自循环引用
        for (var protocolEntry : subProtocolIdMap.entrySet()) {
            var protocolId = protocolEntry.getKey();
            var subProtocolSet = protocolEntry.getValue();
            if (subProtocolSet.contains(protocolId)) {
                var protocolClass = protocols[protocolId].protocolConstructor().getDeclaringClass();
                throw new RunException("[class:{}]中存在自循环引用", protocolClass.getSimpleName());
            }
        }
        // 入度
        var inDegree = new HashMap<Short, Integer>();
        // 初始化入度
        for (var protocolEntry : subProtocolIdMap.entrySet()) {
            var protocolId = protocolEntry.getKey();
            inDegree.put(protocolId, inDegree.getOrDefault(protocolId, 0));
            var subProtocolSet = protocolEntry.getValue();
            for (var subProtocolId : subProtocolSet) {
                inDegree.put(subProtocolId, inDegree.getOrDefault(subProtocolId, 0) + 1);
            }
        }
        var queue = new LinkedList<Short>();
        for (var protocolEntry : inDegree.entrySet()) {
            var protocolInDegree = protocolEntry.getValue();
            if (protocolInDegree == 0) {
                queue.offer(protocolEntry.getKey());
            }
        }
        while (!queue.isEmpty()) {
            var protocolId = queue.poll();
            if (subProtocolIdMap.containsKey(protocolId)) {
                for (var subProtocolId : subProtocolIdMap.get(protocolId)) {
                    inDegree.put(subProtocolId, inDegree.get(subProtocolId) - 1);
                    if (inDegree.get(subProtocolId) == 0) {
                        queue.offer(subProtocolId);
                    }
                }
            }
        }
        var circularReferenceProtocols = new ArrayList<String>();
        // 入度不为0的表示存在循环引用的协议
        for (var protocolEntry : inDegree.entrySet()) {
            if (protocolEntry.getValue() > 0) {
                circularReferenceProtocols.add(protocols[protocolEntry.getKey()].protocolConstructor().getDeclaringClass().getSimpleName());
            }
        }
        // 抛出所有存在循环引用的协议类名
        if (!circularReferenceProtocols.isEmpty()) {
            throw new RunException("[class:{}]中存在循环引用", StringUtils.joinWith(",", circularReferenceProtocols.toArray()));
        }
    }

}
