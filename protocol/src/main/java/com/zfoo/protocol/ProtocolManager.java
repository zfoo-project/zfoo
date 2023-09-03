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

package com.zfoo.protocol;

import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.collection.HashMapIntShort;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolModule;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.xml.XmlProtocols;
import io.netty.buffer.ByteBuf;

import java.util.*;

/**
 * @author godotg
 * @version 3.0
 */
public class ProtocolManager {

    public static final short MAX_PROTOCOL_NUM = Short.MAX_VALUE;
    public static final byte MAX_MODULE_NUM = Byte.MAX_VALUE;

    /**
     * The protocol corresponding to the protocolId.(协议号protocolId对应的协议，数组下标是协议号protocolId)
     */
    public static final IProtocolRegistration[] protocols = new IProtocolRegistration[MAX_PROTOCOL_NUM];
    /**
     * The modules of the protocol.(协议的模块)
     */
    public static final ProtocolModule[] modules = new ProtocolModule[MAX_MODULE_NUM];

    /**
     * key:packet class，value:protocolId.(如果所有协议Class返回的hashcode都不相同（大概率事件），则使用高性能的HashMapIntShort)
     */
    public static Map<Class<?>, Short> protocolIdMap = new HashMap<>();
    public static HashMapIntShort protocolIdPrimitiveMap = new HashMapIntShort();

    static {
        // default protocol module
        modules[0] = ProtocolModule.DEFAULT_PROTOCOL_MODULE;
    }

    /**
     * serialize the packet into the buffer
     */
    public static void write(ByteBuf buffer, Object packet) {
        var protocolId = protocolId(packet.getClass());
        // write the protocolId
        ByteBufUtils.writeShort(buffer, protocolId);
        // write the package
        protocols[protocolId].write(buffer, packet);
    }

    /**
     * deserialization a packet from the buffer
     */
    public static Object read(ByteBuf buffer) {
        return protocols[ByteBufUtils.readShort(buffer)].read(buffer);
    }

    public static IProtocolRegistration getProtocol(short protocolId) {
        return protocols[protocolId];
    }

    public static IProtocolRegistration getProtocol(Class<?> protocolClass) {
        return getProtocol(protocolId(protocolClass));
    }

    public static ProtocolModule moduleByProtocolId(short id) {
        return modules[protocols[id].module()];
    }
    public static ProtocolModule moduleByProtocol(Class<?> clazz) {
        return moduleByProtocolId(protocolId(clazz));
    }

    /**
     * Find the module based on the module ID
     */
    public static ProtocolModule moduleByModuleId(byte moduleId) {
        var module = modules[moduleId];
        AssertionUtils.notNull(module, "[moduleId:{}]不存在", moduleId);
        return module;
    }

    /**
     * Find modules by module name
     */
    public static ProtocolModule moduleByModuleName(String name) {
        var moduleOptional = Arrays.stream(modules)
                .filter(Objects::nonNull)
                .filter(it -> it.getName().equals(name))
                .findFirst();
        if (moduleOptional.isEmpty()) {
            return null;
        }
        return moduleOptional.get();
    }

    public static short protocolId(Class<?> clazz) {
        return protocolIdMap == null ? protocolIdPrimitiveMap.getPrimitive(clazz.hashCode()) : protocolIdMap.get(clazz);
    }

    public static boolean isProtocolClass(Class<?> clazz) {
        return protocolIdMap == null ? protocolIdPrimitiveMap.containsKey(clazz.hashCode()) : protocolIdMap.containsKey(clazz);
    }

    public static void initProtocol(Set<Class<?>> protocolClassSet) {
        ProtocolAnalysis.analyze(protocolClassSet, GenerateOperation.NO_OPERATION);
    }

    /**
     * Register protocol
     *
     * @param protocolClassSet  A list of protocols that need to be initialized
     * @param generateOperation Protocol configuration(需要生成哪些语言的协议文件 是否折叠等信息)
     */
    public static void initProtocol(Set<Class<?>> protocolClassSet, GenerateOperation generateOperation) {
        ProtocolAnalysis.analyze(protocolClassSet, generateOperation);
    }

    public static void initProtocol(XmlProtocols xmlProtocols, GenerateOperation generateOperation) {
        ProtocolAnalysis.analyze(xmlProtocols, generateOperation);
    }

    /**
     * EN:Register protocol and automatically generates a protocol ID if the subprotocol does not specify a protocol ID
     * CN:子协议会自动注册协议号protocolId，如果子协议没有指定protocolId则自动生成protocolId
     */
    public static void initProtocolAuto(Set<Class<?>> protocolClassSet, GenerateOperation generateOperation) {
        ProtocolAnalysis.analyzeAuto(protocolClassSet, generateOperation);
    }

}
