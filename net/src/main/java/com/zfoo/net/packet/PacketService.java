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

package com.zfoo.net.packet;

import com.zfoo.net.NetContext;
import com.zfoo.net.core.broker.BrokerRegisterAsk;
import com.zfoo.net.packet.common.Ping;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.util.NumberUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.xml.XmlProtocols;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.NativeDetector;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author godotg
 */
public class PacketService implements IPacketService {

    private static final String PACKAGE_PREFIX = "package:";
    private static final String IDENTIFIER = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern PACKAGE_NAME = Pattern.compile(IDENTIFIER + "(?:\\." + IDENTIFIER + ")*");

    private static final Logger logger = LoggerFactory.getLogger(PacketService.class);

    /**
     * Length of the packet header, equal to the size of one int (4 bytes).
     */
    public static final int PACKET_HEAD_LENGTH = 4;


    public PacketService() {

    }

    @Override
    public void init() {
        var applicationContext = NetContext.getApplicationContext();

        var netConfig = NetContext.getConfigManager().getLocalConfig();
        var protocolLocation = netConfig.getProtocolLocation();

        var generateOperation = new GenerateOperation();
        generateOperation.setMergeProtocol(netConfig.isMergeProtocol());
        generateOperation.setFoldProtocol(netConfig.isFoldProtocol());
        generateOperation.setProtocolPath(netConfig.getProtocolPath());
        generateOperation.setProtocolParam(netConfig.getProtocolParam());
        var codeLanguageArr = StringUtils.tokenize(netConfig.getCodeLanguages(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

        for (var codeLanguage : codeLanguageArr) {
            var codeLanguageSet = getProtocolList(codeLanguage);
            if (CollectionUtils.isEmpty(codeLanguageSet)) {
                continue;
            }
            generateOperation.getGenerateLanguages().addAll(codeLanguageSet);
        }
        // Set the protocol generation filter
        GenerateProtocolFile.generateProtocolFilter = GenerateProtocolFile.DefaultNetGenerateProtocolFilter;

        initProtocols(applicationContext, protocolLocation, generateOperation);

        // Register protocol receivers
        var componentBeans = applicationContext.getBeansWithAnnotation(Component.class);
        for (var bean : componentBeans.values()) {
            NetContext.getRouter().registerPacketReceiverDefinition(bean);
        }
    }

    protected void initProtocols(ApplicationContext context, String protocolLocation, GenerateOperation generateOperation) {
        var resource = context.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + protocolLocation);
        var packageName = protocolPackage(protocolLocation, resource);
        if (packageName != null) {
            initProtocolPackages(context, List.of(packageName), true, generateOperation);
            return;
        }
        try (var input = resource.getInputStream()) {
            var xmlProtocols = DomUtils.inputStream2Object(input, XmlProtocols.class);
            ProtocolManager.initProtocol(xmlProtocols, generateOperation);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getMessage(e));
            throw new RuntimeException(e);
        }
    }

    static String protocolPackage(String location, Resource resource) {
        if (location == null) {
            return null;
        }
        if (location.startsWith(PACKAGE_PREFIX)) {
            var name = location.substring(PACKAGE_PREFIX.length());
            if (!PACKAGE_NAME.matcher(name).matches()) {
                throw new IllegalArgumentException("Expected a Java package name after package: " + location);
            }
            return name;
        }
        // Existing resource names (even without an extension) and XML paths retain their meaning.
        if (location.toLowerCase(Locale.ROOT).endsWith(".xml")
                || !PACKAGE_NAME.matcher(location).matches() || resource.exists()) {
            return null;
        }
        return location;
    }

    protected void initProtocolPackages(ApplicationContext context, Collection<String> roots, boolean requireMatch, GenerateOperation operation) {
        if (NativeDetector.inNativeImage()) {
            throw new IllegalStateException("Set NetConfig.protocolLocation to an XML resource in a native image");
        }
        var scanner = new ClassPathScanningCandidateComponentProvider(false, context.getEnvironment());
        scanner.setResourceLoader(context);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Protocol.class));

        var protocols = new HashSet<Class<?>>();
        for (var root : roots) {
            addProtocols(scanner, context, root, protocols);
        }
        if (requireMatch && protocols.isEmpty()) {
            throw new IllegalArgumentException("No @Protocol classes found in " + roots);
        }
        for (var root : List.of(Ping.class.getPackageName(), SignalAttachment.class.getPackageName(), BrokerRegisterAsk.class.getPackageName())) {
            addProtocols(scanner, context, root, protocols);
        }
        ProtocolManager.initProtocol(protocols, operation);
    }

    private void addProtocols(ClassPathScanningCandidateComponentProvider scanner, ApplicationContext context, String root, Set<Class<?>> protocols) {
        for (var candidate : scanner.findCandidateComponents(root)) {
            var type = ClassUtils.resolveClassName(candidate.getBeanClassName(), context.getClassLoader());
            if (!type.isEnum()) {
                protocols.add(type);
            }
        }
    }

    /**
     * Parse and return the set of code languages to generate protocols for.
     */
    private Set<CodeLanguage> getProtocolList(String codeLanguage) {
        var languageSet = new HashSet<CodeLanguage>();
        var isNumeric = NumberUtils.isNumeric(codeLanguage);
        for (var language : CodeLanguage.values()) {
            if (isNumeric) {
                var code = Integer.valueOf(codeLanguage);
                if ((code & language.id) != 0) {
                    languageSet.add(language);
                }
            } else if (language.name().equalsIgnoreCase(codeLanguage)) {
                languageSet.add(language);
                break;
            }
        }
        return languageSet;
    }

    @Override
    public DecodedPacketInfo read(ByteBuf buffer) {
        // The packet length has already been parsed in the upper layer

        // Parse the packet body
        var packet = ProtocolManager.read(buffer);
        // Parse the packet attachment
        var hasAttachment = ByteBufUtils.tryReadBool(buffer);
        var attachment = hasAttachment ? (ProtocolManager.read(buffer)) : null;
        return DecodedPacketInfo.valueOf(packet, attachment);
    }

    @Override
    public void write(ByteBuf buffer, Object packet, Object attachment) {
        // Write the packet
        ProtocolManager.write(buffer, packet);

        // Write the attachment
        if (attachment == null) {
            ByteBufUtils.writeBool(buffer, false);
        } else {
            ByteBufUtils.writeBool(buffer, true);
            // Write the attachment
            ProtocolManager.write(buffer, attachment);
        }
    }

    @Override
    public void writeHeaderAndBody(ByteBuf buffer, Object packet, Object attachment) {
        try {
            // Reserve space for the packet length header (4 bytes)
            buffer.ensureWritable(7);
            buffer.writerIndex(PACKET_HEAD_LENGTH);

            write(buffer, packet, attachment);

            writeHeaderBefore(buffer);
        } catch (Exception e) {
            logger.error("write packet exception", e);
        } catch (Throwable t) {
            logger.error("write packet error", t);
        }
    }

    @Override
    public void writeHeaderBefore(ByteBuf buffer) {
        int length = buffer.writerIndex();

        int packetLength = length - PACKET_HEAD_LENGTH;

        buffer.writerIndex(0);

        buffer.writeInt(packetLength);

        buffer.writerIndex(length);
    }
}
