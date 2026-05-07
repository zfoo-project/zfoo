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
import com.zfoo.protocol.registration.field.*;
import com.zfoo.protocol.serializer.reflect.*;
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
     * Temp field: will be destroyed after startup. Maps protocolId to the corresponding Class.
     */
    private static Map<Short, Class<?>> protocolClassMap = new HashMap<>(MAX_PROTOCOL_NUM);

    /**
     * Temp field: will be destroyed after startup. Maps each protocol to its direct sub-protocols (one level only).
     */
    private static Map<Short, Set<Short>> subProtocolIdMap = new HashMap<>(MAX_PROTOCOL_NUM);

    /**
     * Temp field: will be destroyed after startup. Reserved names that cannot be used as protocol class names.
     */
    private static Set<String> protocolReserved = Set.of("IProtocol", "IProtocolRegistration", "ProtocolManager", "IFieldRegistration"
            , "Buffer", "ByteBuf", "IByteBuffer", "ByteBuffer", "LittleEndianByteBuffer", "NormalByteBuffer"
            , "ByteBufUtils", "ArrayUtils", "CollectionUtils"
            , "Boolean", "Byte", "Short", "Integer", "Long", "Float", "Double", "String", "Character", "Object"
            , "Collections", "Iterator", "List", "ArrayList", "Map", "HashMap", "Set", "HashSet"
            , "DecodedPacketInfo", "EncodedPacketInfo"
            , "Protocols");

    /**
     * Temp field: will be destroyed after startup. Types that are not supported as protocol fields.
     */
    private static Set<Class<?>> unsupportedTypes = Set.of(char.class, Character.class);

    /**
     * Temp field: will be destroyed after startup. Serializers for base primitive types.
     */
    private static Map<Class<?>, ISerializer> baseSerializerMap = new HashMap<>(128);

    static {
        // Initialize base type serializers
        baseSerializerMap.put(boolean.class, BoolSerializer.INSTANCE);
        baseSerializerMap.put(Boolean.class, BoolSerializer.INSTANCE);
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
     * parse protocol type
     */
    public static synchronized void analyze(Set<Class<?>> protocolClassSet, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}] initialization has already been completed, please do not repeat the initialization", ProtocolManager.class.getSimpleName());
        // Validate protocol class
        for (var protocolClass : protocolClassSet) {
            var protocolId = getProtocolIdAndCheckClass(protocolClass);
            AssertionUtils.isTrue(protocolId >= 0, "[class:{}] must use annotation @Protocol annotation", protocolClass.getCanonicalName());
            initProtocolClass(protocolId, protocolClass);
        }

        // Map protocol ID to protocol info
        for (var protocolClass : protocolClassSet) {
            var registration = parseProtocolRegistration(protocolClass, ProtocolModule.DEFAULT_PROTOCOL_MODULE);
            protocols[registration.protocolId()] = registration;
        }

        // All protocols registered via specific classes use bytecode enhancement
        var enhanceList = Arrays.stream(protocols).filter(Objects::nonNull).toList();
        enhance(generateOperation, enhanceList);
    }

    public static synchronized void analyzeAuto(List<Class<?>> protocolClassList, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}] initialization has already been completed, please do not repeat the initialization", ProtocolManager.class.getSimpleName());
        var relevantClassSet = new LinkedHashSet<Class<?>>(protocolClassList);
        for (var clazz : protocolClassList) {
            relevantClassSet.addAll(ClassUtils.relevantClass(clazz));
        }
        //var relevantClassList = relevantClassSet;
       /*  var relevantClassList = relevantClassSet.stream()
                .sorted((a, b) -> a.getCanonicalName().compareTo(b.getCanonicalName()))
                .toList();*/

        // Validate protocol class
        var noProtocolClassList = new ArrayList<Class<?>>();
        for (var protocolClass : relevantClassSet) {
            var protocolId = getProtocolIdAndCheckClass(protocolClass);
            if (protocolId >= 0) {
                initProtocolClass(protocolId, protocolClass);
            } else {
                noProtocolClassList.add(protocolClass);
            }
        }

        noProtocolClassList.sort(Comparator.comparing(Class::getCanonicalName));
        var countProtocolId = (short) 0;
        for (var protocolClass : noProtocolClassList) {
            while (protocolClassMap.containsKey(countProtocolId)) {
                countProtocolId++;
            }
            initProtocolClass(countProtocolId, protocolClass);
        }

        // Map protocol ID to protocol info
        for (var protocolClass : relevantClassSet) {
            var registration = parseProtocolRegistration(protocolClass, ProtocolModule.DEFAULT_PROTOCOL_MODULE);
            protocols[registration.protocolId()] = registration;
        }

        // All protocols registered via specific classes use bytecode enhancement
        var enhanceList = Arrays.stream(protocols).filter(Objects::nonNull).toList();
        enhance(generateOperation, enhanceList);
    }

    /**
     * EN: If the path of the package contains a classpath, the moduleId of the classpath is preferred, which means that the priority of the classpath will be higher.
     * Both package paths and class paths can be used in a single protocol.xml file. When a package path contains a class path, the class path's moduleId takes priority.
     */
    public static synchronized void analyze(XmlProtocols xmlProtocols, GenerateOperation generateOperation) {
        AssertionUtils.notNull(subProtocolIdMap, "[{}] initialization has already been completed, please do not repeat the initialization", ProtocolManager.class.getSimpleName());

        var protocolXmlEnhanceMap = new HashMap<Class<?>, Boolean>();
        var classModuleDefinitionMap = new HashMap<Class<?>, Byte>();
        var moduleDefinitionClassMap = new HashMap<Byte, Set<Class<?>>>();

        // Register classes first, then packages
        for (var moduleDefinition : xmlProtocols.getModules()) {
            var moduleId = moduleDefinition.getId();
            var module = new ProtocolModule(moduleId, moduleDefinition.getName());
            AssertionUtils.isTrue(module.getId() > 0, "[module:{}] [id:{}] must be greater than or equal to 1", module.getName(), moduleId);
            AssertionUtils.isNull(modules[module.getId()], "duplicate [module:{}] [id:{}] Exception!", module.getName(), moduleId);

            modules[module.getId()] = module;
            if (CollectionUtils.isEmpty(moduleDefinition.getProtocols())) {
                continue;
            }
            // Module defines all protocols
            var protocolModuleSet = moduleDefinitionClassMap.computeIfAbsent(module.getId(), it -> new HashSet<>());
            for (var protocolDefinition : moduleDefinition.getProtocols()) {
                var id = protocolDefinition.getId();
                var location = protocolDefinition.getLocation();
                var enhance = protocolDefinition.isEnhance();

                // Use the class path first to obtain the class name, and search the directory if it cannot be obtained
                // Prefer class path to get the class name; fall back to directory scan if not found
                Class<?> clazz = null;
                try {
                    clazz = ClassUtils.forName(location);
                } catch (Exception e) {
                }

                // If a class is defined, validate its format
                if (clazz == null) {
                    continue;
                }

                var protocolId = getProtocolIdAndCheckClass(clazz);
                // No @Protocol annotation; use the protocolId from XML
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

        // Then register package paths by scanning
        for (var moduleDefinition : xmlProtocols.getModules()) {
            var moduleId = moduleDefinition.getId();
            var module = modules[moduleId];
            if (CollectionUtils.isEmpty(moduleDefinition.getProtocols())) {
                continue;
            }

            // Module defines all protocols
            var protocolModuleSet = moduleDefinitionClassMap.computeIfAbsent(module.getId(), it -> new HashSet<>());
            for (var protocolDefinition : moduleDefinition.getProtocols()) {
                var id = protocolDefinition.getId();
                var location = protocolDefinition.getLocation();
                var enhance = protocolDefinition.isEnhance();

                // Use the class path first to obtain the class name, and search the directory if it cannot be obtained
                // Prefer class path to get the class name; fall back to directory scan if not found
                Class<?> clazz = null;
                try {
                    clazz = ClassUtils.forName(location);
                } catch (Exception e) {
                }

                // If a class is defined, validate its format
                if (clazz != null) {
                    continue;
                }

                var packetClazzList = scanPackageList(protocolDefinition.getLocation());
                // If it is a class path, protocol id must not be specified
                if (CollectionUtils.isEmpty(packetClazzList)) {
                    throw new RunException("can not scan any protocol class in [{}]", location);
                }
                if (id >= 0) {
                    throw new RunException("When use package location, specify protocol id in xml");
                }

                for (Class<?> protocolClass : packetClazzList) {
                    // If location was already specified, prefer the moduleId from the class absolute path
                    if (classModuleDefinitionMap.containsKey(protocolClass)) {
                        continue;
                    }
                    var protocolId = getProtocolIdAndCheckClass(protocolClass);
                    initProtocolClass(protocolId, protocolClass);
                    protocolModuleSet.add(protocolClass);
                    classModuleDefinitionMap.put(protocolClass, moduleId);
                    protocolXmlEnhanceMap.compute(protocolClass, (key, value) -> Boolean.TRUE.equals(value) || enhance);
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
                // Enhance the protocol if either @Protocol annotation or XML defines enhancement
                if ((clazz.isAnnotationPresent(Protocol.class) && clazz.getAnnotation(Protocol.class).enhance()) || protocolXmlEnhanceMap.get(clazz)) {
                    enhanceList.add(registration);
                }
                // Register protocol
                protocols[protocolId] = registration;
            }
        }

        enhance(generateOperation, enhanceList);
    }

    public static List<Class<?>> scanPackageList(String location) {
        // Get all classes under the path
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

    public static void enhance(GenerateOperation generateOperation, List<IProtocolRegistration> enhanceList) {
        try {
            enhanceProtocolBefore(generateOperation);
            enhanceProtocolRegistration(enhanceList);
            enhanceProtocolAfter(generateOperation);
        } catch (Exception e) {
            throw new UnknownException(e);
        }
    }

    private static void enhanceProtocolBefore(GenerateOperation generateOperation) throws IOException, ClassNotFoundException {
        // Validate protocol format
        checkAllProtocolClass();
        // Validate module format
        checkAllModules();
        // Generate protocol
        GenerateProtocolFile.generate(generateOperation);
    }

    private static void enhanceProtocolRegistration(List<IProtocolRegistration> enhanceList) throws NoSuchMethodException, IllegalAccessException, InstantiationException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchFieldException {
        if (GraalVmUtils.isGraalVM()) {
            return;
        }
        // Bytecode enhancement
        for (var registration : enhanceList) {
            protocols[registration.protocolId()] = EnhanceUtils.createProtocolRegistration((ProtocolRegistration) registration);
        }

        // After bytecode enhancement, initialize sub-protocol member variables
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
    }

    public static List<Field> getFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (var field : clazz.getDeclaredFields()) {
            var modifiers = field.getModifiers();
            if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }
            if (!clazz.isRecord() && Modifier.isFinal(modifiers)) {
                // Protocol fields must not be final
                throw new RunException("The access modifier for the [field:{}] attribute in the [{}] protocol number cannot be final", clazz.getCanonicalName(), field.getName());
            }
            if (!Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers)) {
                // Protocol fields must be public or private
                throw new RunException("The access modifier of the [field:{}] attribute in the [{}] protocol number must be public or private", clazz.getCanonicalName(), field.getName());
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
                    // Protocol fields must not have duplicate @Compatible ordinals
                    throw new RunException("[field:{}] and [field:{}] in the [{}] protocol number cannot have the same Compatible order [order:{}]"
                            , clazz.getCanonicalName(), oldField.getName(), field.getName(), oldField, order);
                }
            } else {
                notCompatibleFields.add(field);
            }
        }

        // By default, incompatible protocol fields are sorted alphabetically; customize this rule for private protocols
        // For added security, update the field sort rule each version; different byte orderings per version act as a form of encryption
        notCompatibleFields.sort((a, b) -> a.getName().compareTo(b.getName()));

        // Compatible protocol fields are appended at the end by default
        var compatibleFields = compatibleFieldMap.entrySet()
                .stream()
                .sorted((a, b) -> a.getKey() - b.getKey())
                .map(Map.Entry::getValue)
                .toList();
        notCompatibleFields.addAll(compatibleFields);
        return notCompatibleFields;
    }

    public static ProtocolRegistration parseProtocolRegistration(Class<?> clazz, ProtocolModule module) {
        var protocolId = ProtocolManager.protocolId(clazz);
        var declaredFields = getFields(clazz);
        // Fields of the object to be serialized
        var fields = customFieldOrder(clazz, declaredFields);

        try {
            var registrationList = new ArrayList<IFieldRegistration>();
            boolean isRecord = clazz.isRecord();
            for (var field : fields) {
                registrationList.add(toRegistration(clazz, field));
            }

            Constructor<?> constructor;
            if (isRecord) {
                constructor = ReflectionUtils.getConstructor(clazz, declaredFields.stream().map(p -> p.getType()).toList().toArray(new Class[]{}));
            } else {
                constructor = clazz.getDeclaredConstructor();
            }

            ReflectionUtils.makeAccessible(constructor);
            return new ProtocolRegistration(protocolId, module.getId(), constructor, ArrayUtils.listToArray(fields, Field.class), ArrayUtils.listToArray(registrationList, IFieldRegistration.class));
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("Resolve protocol [class:{}] exception", clazz), e);
        }
    }

    private static IFieldRegistration toRegistration(Class<?> clazz, Field field) {
        Class<?> fieldTypeClazz = field.getType();

        ISerializer serializer = baseSerializerMap.get(fieldTypeClazz);

        // Is a primitive type variable
        if (serializer != null) {
            return BaseField.valueOf(serializer);
        } else if (fieldTypeClazz.isArray()) {
            // Is an array
            Class<?> arrayClazz = fieldTypeClazz.getComponentType();

            IFieldRegistration registration = typeToRegistration(clazz, arrayClazz);
            return ArrayField.valueOf(registration, field.getType().getComponentType());
        } else if (Set.class.isAssignableFrom(fieldTypeClazz)) {
            if (!fieldTypeClazz.equals(Set.class)) {
                // Must be the Set interface type
                throw new RunException("[class:{}] type declaration is incorrect, it must be of the Set interface type", clazz.getCanonicalName());
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                // Must be a generic class
                throw new RunException("[class:{}] type declaration is incorrect, not a generic class[field:{}]", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 1) {
                // Set type must declare generic type
                throw new RunException("Set type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration registration = typeToRegistration(clazz, types[0]);
            return SetField.valueOf(registration, type);
        } else if (List.class.isAssignableFrom(fieldTypeClazz)) {
            // Is a List
            if (!fieldTypeClazz.equals(List.class)) {
                // Must be the List interface type
                throw new RunException("[class:{}] type declaration is incorrect, it must be a List interface type", clazz.getCanonicalName());
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                // List type must declare generic type
                throw new RunException("[class:{}] type declaration is incorrect, not a generic class[field:{}]", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 1) {
                // List type must declare generic type
                throw new RunException("List type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration registration = typeToRegistration(clazz, types[0]);
            return ListField.valueOf(registration, type);

        } else if (Map.class.isAssignableFrom(fieldTypeClazz)) {
            if (!fieldTypeClazz.equals(Map.class)) {
                // Must be the Map interface type
                throw new RunException("[class:{}] type declaration is incorrect, it must be a Map interface type", clazz.getCanonicalName());
            }

            Type type = field.getGenericType();

            if (!(type instanceof ParameterizedType)) {
                // Map type must declare generic type
                throw new RunException("Map type declaration in [class:{}] is incorrect, and [field:{}] is not a generic class", clazz.getCanonicalName(), field.getName());
            }

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();

            if (types.length != 2) {
                // Map type must declare generic type
                throw new RunException("Map type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
            }

            IFieldRegistration keyRegistration = typeToRegistration(clazz, types[0]);
            IFieldRegistration valueRegistration = typeToRegistration(clazz, types[1]);

            return MapField.valueOf(keyRegistration, valueRegistration, type);
        } else {
            checkUnsupportedType(fieldTypeClazz);
            // Is a protocol reference variable
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
            // Generic class
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
                // Primitive type
                return BaseField.valueOf(serializer);
            } else if (clazz.isArray()) {
                // Multi-dimensional arrays or collection-nested arrays are not supported; only one-dimensional arrays allowed
                throw new RunException("Multi-dimensional array  or set nested arrays [type:{}] types are not supported, only one-dimensional arrays are supported", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                // Combining arrays with collections is not supported
                throw new RunException("The combination of arrays and collections with the [type:{}] type is not supported", type);
            } else {
                checkUnsupportedType(clazz);
                // Is a protocol reference variable
                if (!protocolIdMap.containsKey(clazz)) {
                    throw new RunException("sub protocol:[{}] needs to register in protocol:[{}]", clazz.getSimpleName(), currentProtocolClass.getCanonicalName());
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
     * EN: This method is only called when the protocol is generated, and cannot be called once it is run
     * This method is called only during protocol generation; cannot be called once running
     */
    public static Set<Short> getFirstSubProtocolIds(short protocolId) {
        return subProtocolIdMap.getOrDefault(protocolId, Collections.emptySet());
    }

    /**
     * EN: This method is only called when the protocol is generated, and cannot be called once it is run
     * This method is called only during protocol generation; cannot be called once running
     */
    public static Set<Short> getAllSubProtocolIds(short protocolId) {
        // Cannot call getAllSubProtocolIds after initialization is complete
        AssertionUtils.notNull(subProtocolIdMap, "[{}] has been initially completed, and after the initialization is completed, you cannot call getAllSubProtocolIds", ProtocolAnalysis.class.getSimpleName());

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
                    // Circular protocol references are not supported
                    if (subClassId == protocolId) {
                        throw new RunException("[class:{}] contains circular reference protocol[class:{}] in the lower level protocol[class:{}]"
                                , protocolClass.getSimpleName(), protocols[firstSubProtocolId].protocolConstructor().getDeclaringClass(), protocolClass.getSimpleName());
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

    // Smart protocol syntax analysis; incorrect protocol definitions will prevent startup and show an error
    //-----------------------------------------------------------------------
    public static void initProtocolClass(short protocolId, Class<?> clazz) {
        // Duplicate protocol ID definition
        if (protocolIdMap.containsKey(clazz)) {
            throw new RunException("duplicate protocol:[{}] protocolId:[{}] and [protocolId:{}]", clazz.getSimpleName(), protocolIdMap.get(clazz), protocolId);
        }
        protocolIdMap.put(clazz, protocolId);
        protocolIdPrimitiveMap.putPrimitive(clazz.hashCode(), protocolId);
        var previousProtocolClass = protocolClassMap.put(protocolId, clazz);
        // Duplicate protocol ID
        if (previousProtocolClass != null) {
            throw new RunException("[{}][{}] protocol number [protocolId:{}] is repeated", clazz.getCanonicalName(), previousProtocolClass.getCanonicalName(), protocolId);
        }
    }

    public static short getProtocolIdAndCheckClass(Class<?> clazz) {
        checkUnsupportedType(clazz);
        // Check if it is a simple JavaBean
        ReflectionUtils.assertIsPojoClass(clazz);
        // Must not be a generic class
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}] can't be a generic class", clazz.getCanonicalName());

        // POJO must have a no-args constructor
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
        // Module ID must be unique
        var moduleIdSet = new HashSet<Byte>();
        Arrays.stream(modules)
                .filter(Objects::nonNull)
                .peek(it -> AssertionUtils.isTrue(!moduleIdSet.contains(it.getId()), "duplicate id in module [{}], and the id of modules cannot be duplicated", it))
                .forEach(it -> moduleIdSet.add(it.getId()));

        // Module name must be unique
        var moduleNameSet = new HashSet<String>();
        Arrays.stream(modules)
                .filter(Objects::nonNull)
                .peek(it -> AssertionUtils.isTrue(!moduleNameSet.contains(it.getName()), "duplicate name in module [{}], and the module name cannot be duplicated", it))
                .forEach(it -> moduleNameSet.add(it.getName()));
    }

    private static void checkAllProtocolClass() {
        // Validate protocol format

        // Protocol name must be unique
        var allProtocolNameMap = new HashMap<String, Class<?>>();
        for (var protocolRegistration : protocols) {
            if (protocolRegistration == null) {
                continue;
            }

            var protocolClass = protocolRegistration.protocolConstructor().getDeclaringClass();
            var protocolName = protocolClass.getSimpleName();
            // Protocol must not have duplicate names
            if (allProtocolNameMap.containsKey(protocolName)) {
                throw new RunException("[class:{}] and [class:{}] has duplicate protocol name, protocol cannot contain duplicate names"
                        , protocolClass.getCanonicalName(), allProtocolNameMap.get(protocolName).getCanonicalName());
            }

            // Protocol class name must not be a reserved keyword
            if (protocolReserved.stream().anyMatch(it -> it.equalsIgnoreCase(protocolName))) {
                throw new RunException("The name of the protocol [class:{}] cannot be a reserved name [{}]", protocolClass.getCanonicalName(), protocolName);
            }

            allProtocolNameMap.put(protocolName, protocolClass);
        }


        // Topological sort to detect circular protocol dependencies
        if (subProtocolIdMap.isEmpty()) {
            return;
        }
        // Check for self-referencing protocols first
        for (var protocolEntry : subProtocolIdMap.entrySet()) {
            var protocolId = protocolEntry.getKey();
            var subProtocolSet = protocolEntry.getValue();
            if (subProtocolSet.contains(protocolId)) {
                var protocolClass = protocols[protocolId].protocolConstructor().getDeclaringClass();
                throw new RunException("[class:{}] cannot be circular reference protocol", protocolClass.getSimpleName());
            }
        }
        // In-degree
        var inDegree = new HashMap<Short, Integer>();
        // Initialize in-degrees
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
        // Non-zero in-degree means a circular protocol dependency exists
        for (var protocolEntry : inDegree.entrySet()) {
            if (protocolEntry.getValue() > 0) {
                circularReferenceProtocols.add(protocols[protocolEntry.getKey()].protocolConstructor().getDeclaringClass().getSimpleName());
            }
        }
        // Throw listing all class names in the circular dependency
        if (!circularReferenceProtocols.isEmpty()) {
            throw new RunException("[class:{}] cannot be circular reference protocol", StringUtils.joinWith(",", circularReferenceProtocols.toArray()));
        }
    }

}
