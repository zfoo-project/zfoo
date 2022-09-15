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

package com.zfoo.net.packet.service;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.router.route.PacketBus;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.xml.XmlProtocols;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class PacketService implements IPacketService {

    private static final Logger logger = LoggerFactory.getLogger(PacketService.class);

    /**
     * 包体的头部的长度，一个int字节长度
     */
    public static final int PACKET_HEAD_LENGTH = 4;

    /**
     * 网络包的约定规则如下：
     * 1. 客户端的请求约定以Request结尾，服务器的响应约定以Response结尾
     * 2. 服务器内部请求约定以Ask结尾，服务器内部的响应约定以Answer结尾
     * 3. 服务器主动通知客户端以Notice结尾
     * 4. 公共的协议放在common模块
     */
    public static final String NET_REQUEST_SUFFIX = "Request";
    public static final String NET_RESPONSE_SUFFIX = "Response";

    public static final String NET_ASK_SUFFIX = "Ask";
    public static final String NET_ANSWER_SUFFIX = "Answer";

    public static final String NET_NOTICE_SUFFIX = "Notice";


    public static final String NET_COMMON_MODULE = "common";

    private final Predicate<IProtocolRegistration> netGenerateProtocolFilter = registration
            -> ProtocolManager.moduleByModuleId(registration.module()).getName().matches(NET_COMMON_MODULE)
            || registration.protocolConstructor().getDeclaringClass().getSimpleName().endsWith(NET_REQUEST_SUFFIX)
            || registration.protocolConstructor().getDeclaringClass().getSimpleName().endsWith(NET_RESPONSE_SUFFIX)
            || registration.protocolConstructor().getDeclaringClass().getSimpleName().endsWith(NET_NOTICE_SUFFIX);

    public PacketService() {

    }

    @Override
    public void init() {
        var applicationContext = NetContext.getApplicationContext();

        var netConfig = NetContext.getConfigManager().getLocalConfig();
        var protocolLocation = netConfig.getProtocolLocation();

        var generateOperation = new GenerateOperation();
        generateOperation.setFoldProtocol(netConfig.isFoldProtocol());
        generateOperation.setProtocolPath(netConfig.getProtocolPath());
        generateOperation.setProtocolParam(netConfig.getProtocolParam());
        if (netConfig.isJavascriptProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.JavaScript);
        }
        if (netConfig.isTypescriptProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.TypeScript);
        }
        if (netConfig.isCsharpProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.CSharp);
        }
        if (netConfig.isLuaProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.Lua);
        }
        if (netConfig.isGdscriptProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.GdScript);
        }
        if (netConfig.isCppProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.Cpp);
        }
        if (netConfig.isGoProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.Go);
        }
        if (netConfig.isProtobufProtocol()) {
            generateOperation.getGenerateLanguages().add(CodeLanguage.Protobuf);
        }

        // 设置生成协议的过滤器
        GenerateProtocolFile.generateProtocolFilter = netGenerateProtocolFilter;

        // 解析protocol.xml文件，并将协议生成ProtocolRegistration
        var resource = applicationContext.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + protocolLocation);
        try {
            var xmlProtocols = DomUtils.inputStream2Object(resource.getInputStream(), XmlProtocols.class);
            ProtocolManager.initProtocol(xmlProtocols, generateOperation);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getMessage(e));
            throw new RuntimeException(e);
        }

        // 注册协议接收器
        var beanNames = applicationContext.getBeanDefinitionNames();
        for (var beanName : beanNames) {
            var bean = applicationContext.getBean(beanName);
            PacketBus.registerPacketReceiverDefinition(bean);
        }
    }

    @Override
    public DecodedPacketInfo read(ByteBuf buffer) {
        // 包的长度在上一层已经解析过

        // 解析包体
        var packet = ProtocolManager.read(buffer);
        // 解析包的附加包
        var hasAttachment = ByteBufUtils.tryReadBoolean(buffer);
        var attachment = hasAttachment ? ((IAttachment) ProtocolManager.read(buffer)) : null;
        return DecodedPacketInfo.valueOf(packet, attachment);
    }

    @Override
    public void write(ByteBuf buffer, IPacket packet, IAttachment attachment) {

        if (packet == null) {
            logger.error("packet is null and can not be sent.");
            return;
        }

        // 预留写入包的长度，一个int字节大小
        buffer.writeInt(PACKET_HEAD_LENGTH);

        // 写入包packet
        ProtocolManager.write(buffer, packet);

        // 写入包的附加包attachment
        if (attachment == null) {
            ByteBufUtils.writeBoolean(buffer, false);
        } else {
            ByteBufUtils.writeBoolean(buffer, true);
            // 写入包的附加包attachment
            ProtocolManager.write(buffer, attachment);
        }

        int length = buffer.readableBytes();

        int packetLength = length - PACKET_HEAD_LENGTH;

        buffer.writerIndex(0);

        buffer.writeInt(packetLength);

        buffer.writerIndex(length);
    }
}
