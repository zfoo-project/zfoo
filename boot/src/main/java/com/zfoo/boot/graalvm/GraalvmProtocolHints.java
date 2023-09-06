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

import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.xml.XmlModuleDefinition;
import com.zfoo.protocol.xml.XmlProtocolDefinition;
import com.zfoo.protocol.xml.XmlProtocols;
import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.AbstractReferenceCountedByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.springframework.aot.hint.MemberCategory.*;

/**
 * Register runtime hints for the token library
 *
 * @author godotg
 */
public class GraalvmProtocolHints implements RuntimeHintsRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(GraalvmProtocolHints.class);

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        logger.info("Hint of protocol for spring aot runtime register in graalvm");

        var classes = new HashSet<Class<?>>();
        classes.add(XmlProtocols.class);
        classes.add(XmlModuleDefinition.class);
        classes.add(XmlProtocolDefinition.class);

        var filterClasses = HintUtils.filterAllClass(List.of(Protocol.class), Collections.emptyList());
        classes.addAll(filterClasses);

        HintUtils.registerRelevantClasses(hints, classes);

        // netty
        var nettyClassList = List.of(AbstractByteBufAllocator.class, AbstractReferenceCountedByteBuf.class);
        for(var clazz : nettyClassList) {
            hints.reflection().registerType(clazz, DECLARED_FIELDS, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_DECLARED_METHODS);
        }

        var include = "*.protocol";
        hints.resources().registerPattern(include);
        logger.info("Protocol graalvm aot hints register resources [{}]", include);
    }
}
