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

package com.zfoo.protocol.generate;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.cpp.GenerateCppUtils;
import com.zfoo.protocol.serializer.csharp.GenerateCsUtils;
import com.zfoo.protocol.serializer.gdscript.GenerateGdUtils;
import com.zfoo.protocol.serializer.go.GenerateGoUtils;
import com.zfoo.protocol.serializer.javascript.GenerateJsUtils;
import com.zfoo.protocol.serializer.lua.GenerateLuaUtils;
import com.zfoo.protocol.serializer.protobuf.GenerateProtobufUtils;
import com.zfoo.protocol.serializer.typescript.GenerateTsUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class GenerateProtocolFile {

    /**
     * 生成协议的过滤器，默认不过滤
     */
    public static Predicate<IProtocolRegistration> generateProtocolFilter = registration -> true;

    public static AtomicInteger index = new AtomicInteger();

    public static StringBuilder addTab(StringBuilder builder, int deep) {
        builder.append(TAB.repeat(Math.max(0, deep)));
        return builder;
    }

    public static void clear() {
        generateProtocolFilter = null;
        index = null;
    }

    /**
     * 生成各种语言的协议文件
     *
     * @param generateOperation
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void generate(GenerateOperation generateOperation) throws IOException, ClassNotFoundException {
        var protocols = ProtocolManager.protocols;

        // 如果没有需要生成的协议则直接返回
        if (generateOperation.getGenerateLanguages().isEmpty()) {
            return;
        }

        // 外层需要生成的协议
        var outsideGenerateProtocols = Arrays.stream(protocols)
                .filter(it -> Objects.nonNull(it))
                .filter(it -> generateProtocolFilter.test(it))
                .collect(Collectors.toList());

        // 需要生成的子协议，因为外层协议的内部有其它协议
        var insideGenerateProtocols = outsideGenerateProtocols.stream()
                .map(it -> ProtocolAnalysis.getAllSubProtocolIds(it.protocolId()))
                .flatMap(it -> it.stream())
                .map(it -> protocols[it])
                .distinct()
                .collect(Collectors.toList());

        var allGenerateProtocols = new HashSet<IProtocolRegistration>();
        allGenerateProtocols.addAll(outsideGenerateProtocols);
        allGenerateProtocols.addAll(insideGenerateProtocols);

        // 通过协议号，从小到大排序
        var allSortedGenerateProtocols = allGenerateProtocols.stream()
                .sorted((a, b) -> a.protocolId() - b.protocolId())
                .collect(Collectors.toList());

        // 解析协议的文档注释
        GenerateProtocolNote.initProtocolNote(allSortedGenerateProtocols);


        // 计算协议生成的路径
        if (generateOperation.isFoldProtocol()) {
            GenerateProtocolPath.initProtocolPath(allSortedGenerateProtocols);
        }

        // 生成C++协议
        var generateLanguages = generateOperation.getGenerateLanguages();
        if (generateLanguages.contains(CodeLanguage.Cpp)) {
            GenerateCppUtils.init(generateOperation);
            GenerateCppUtils.createProtocolManager(allSortedGenerateProtocols);
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateCppUtils.createCppProtocolFile((ProtocolRegistration) protocolRegistration);
            }
        }

        // 生成Golang协议
        if (generateLanguages.contains(CodeLanguage.Go)) {
            GenerateGoUtils.init(generateOperation);
            GenerateGoUtils.createProtocolManager(allSortedGenerateProtocols);
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateGoUtils.createGoProtocolFile((ProtocolRegistration) protocolRegistration);
            }
        }

        // 生成C#协议
        if (generateLanguages.contains(CodeLanguage.CSharp)) {
            GenerateCsUtils.init(generateOperation);
            GenerateCsUtils.createProtocolManager();
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateCsUtils.createCsProtocolFile((ProtocolRegistration) protocolRegistration);
            }
        }

        // 生成Javascript协议
        if (generateLanguages.contains(CodeLanguage.JavaScript)) {
            GenerateJsUtils.init(generateOperation);
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateJsUtils.createJsProtocolFile((ProtocolRegistration) protocolRegistration);
            }
            GenerateJsUtils.createProtocolManager(allSortedGenerateProtocols);
        }

        // 生成TypeScript协议
        if (generateLanguages.contains(CodeLanguage.TypeScript)) {
            GenerateTsUtils.init(generateOperation);
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateTsUtils.createTsProtocolFile((ProtocolRegistration) protocolRegistration);
            }
            GenerateTsUtils.createProtocolManager(allSortedGenerateProtocols);
        }

        // 生成Lua协议
        if (generateLanguages.contains(CodeLanguage.Lua)) {
            GenerateLuaUtils.init(generateOperation);
            GenerateLuaUtils.createProtocolManager(allSortedGenerateProtocols);
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateLuaUtils.createLuaProtocolFile((ProtocolRegistration) protocolRegistration);
            }
        }

        // 生成GdScript协议
        if (generateLanguages.contains(CodeLanguage.GdScript)) {
            GenerateGdUtils.init(generateOperation);
            GenerateGdUtils.createProtocolManager(allSortedGenerateProtocols);
            for (var protocolRegistration : allSortedGenerateProtocols) {
                GenerateGdUtils.createGdProtocolFile((ProtocolRegistration) protocolRegistration);
            }
        }

        // 生成Protobuf协议
        if (generateLanguages.contains(CodeLanguage.Protobuf)) {
            GenerateProtobufUtils.init(generateOperation);
            GenerateProtobufUtils.createProtocolManager();
            GenerateProtobufUtils.createProtocols();
        }

        // 预留参数，以后可能会用，比如给Lua修改一个后缀名称
        var protocolParam = generateOperation.getProtocolParam();
    }

}
