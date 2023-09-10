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

package com.zfoo.boot.graalvm;

import com.zfoo.net.config.model.NetConfig;
import com.zfoo.net.core.gateway.model.*;
import com.zfoo.net.packet.IPacket;
import com.zfoo.net.packet.common.Error;
import com.zfoo.net.packet.common.*;
import com.zfoo.net.router.attachment.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.xml.XmlProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Register runtime hints for the token library
 *
 * @author godotg
 */
public class GraalvmNetHints implements RuntimeHintsRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(GraalvmNetHints.class);

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        logger.info("Hint of net for spring aot runtime register in graalvm");

        var classes = new HashSet<Class<?>>();
        classes.add(NetConfig.class);

        // atachment
        classes.add(SignalAttachment.class);
        classes.add(GatewayAttachment.class);
        classes.add(UdpAttachment.class);
        classes.add(HttpAttachment.class);
        classes.add(NoAnswerAttachment.class);
        classes.add(AuthUidToGatewayCheck.class);
        classes.add(AuthUidToGatewayConfirm.class);
        classes.add(AuthUidAsk.class);
        classes.add(GatewaySessionInactiveAsk.class);
        classes.add(GatewaySynchronizeSidAsk.class);

        // packet
        classes.add(Message.class);
        classes.add(Error.class);
        classes.add(Heartbeat.class);
        classes.add(Ping.class);
        classes.add(Pong.class);
        classes.add(PairIntLong.class);
        classes.add(PairLong.class);
        classes.add(PairString.class);
        classes.add(PairLS.class);
        classes.add(TripleLong.class);
        classes.add(TripleString.class);
        classes.add(TripleLSS.class);

        var filterClasses = HintUtils.filterAllClass(Collections.emptyList(), List.of(IPacket.class));
        classes.addAll(filterClasses);

        // protocol.xml
        try {
            var resourcePatternResolver = new PathMatchingResourcePatternResolver();
            var protocolResources = new HashSet<Resource>();
            protocolResources.addAll(List.of(resourcePatternResolver.getResources("classpath*:/**/*protocol*.xml")));
            protocolResources.addAll(List.of(resourcePatternResolver.getResources("classpath*:*protocol*.xml")));
            for (var protocolResource : protocolResources) {
                try {
                    var protocolXml = StringUtils.bytesToString(IOUtils.toByteArray(protocolResource.getInputStream()));
                    logger.info("Net found [{}] and register hint by this xml", protocolResource.getURL());
                    var xmlProtocols = DomUtils.string2Object(protocolXml, XmlProtocols.class);
                    for (var moduleDefinition : xmlProtocols.getModules()) {
                        for (var protocolDefinition : moduleDefinition.getProtocols()) {
                            var clazz = ClassUtils.forName(protocolDefinition.getLocation());
                            classes.add(clazz);
                        }
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
        } catch (Exception e) {
            // do nothing
        }

        HintUtils.registerRelevantClasses(hints, classes);

        var include = "*.xml";
        hints.resources().registerPattern(include);
        logger.info("Net graalvm aot hints register resources [{}]", include);
    }
}
