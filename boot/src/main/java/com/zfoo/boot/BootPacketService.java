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

package com.zfoo.boot;

import com.zfoo.net.core.broker.BrokerRegisterAsk;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.packet.common.Ping;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.NativeDetector;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;

class BootPacketService extends PacketService {

    @Override
    protected void initProtocols(ApplicationContext context, String protocolLocation, GenerateOperation operation) {
        if (StringUtils.isNotBlank(protocolLocation)) {
            super.initProtocols(context, protocolLocation, operation);
            return;
        }
        if (NativeDetector.inNativeImage() || !AutoConfigurationPackages.has(context)) {
            throw new IllegalStateException("Set NetConfig.protocolLocation when running in a native image or without Boot base packages");
        }

        var packages = new LinkedHashSet<>(AutoConfigurationPackages.get(context));
        packages.add(Ping.class.getPackageName());
        packages.add(SignalAttachment.class.getPackageName());
        packages.add(BrokerRegisterAsk.class.getPackageName());

        // Inspect annotation metadata without initializing unrelated application classes or creating beans.
        var scanner = new ClassPathScanningCandidateComponentProvider(false, context.getEnvironment());
        scanner.setResourceLoader(context);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Protocol.class));
        var protocols = new HashSet<Class<?>>();
        for (var basePackage : packages) {
            for (var candidate : scanner.findCandidateComponents(basePackage)) {
                var type = ClassUtils.resolveClassName(candidate.getBeanClassName(), context.getClassLoader());
                if (!type.isEnum()) {
                    protocols.add(type);
                }
            }
        }
        ProtocolManager.initProtocol(protocols, operation);
    }
}
