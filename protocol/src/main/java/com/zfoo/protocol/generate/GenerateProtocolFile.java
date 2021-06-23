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
import com.zfoo.protocol.serializer.cs.GenerateCsUtils;
import com.zfoo.protocol.serializer.js.GenerateJsUtils;
import com.zfoo.protocol.serializer.lua.GenerateLuaUtils;
import com.zfoo.protocol.util.ReflectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author jaysunxiao
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

    public static void generate(IProtocolRegistration[] protocols, GenerateOperation generateOperation) throws IOException {

        // 如果没有需要生成的协议则直接返回
        var generateProtocolFlag = Arrays.stream(generateOperation.getClass().getDeclaredFields())
                .filter(it -> it.getName().startsWith("generate"))
                .peek(it -> ReflectionUtils.makeAccessible(it))
                .map(it -> ReflectionUtils.getField(it, generateOperation))
                .filter(it -> it instanceof Boolean)
                .anyMatch(it -> ((Boolean) it).booleanValue() == true);

        if (!generateProtocolFlag) {
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
        GenerateProtocolDocument.initProtocolDocument(allSortedGenerateProtocols);


        // 计算协议生成的路径
        if (generateOperation.isFoldProtocol()) {
            GenerateProtocolPath.initProtocolPath(allSortedGenerateProtocols);
        }

        // 生成C#协议
        if (generateOperation.isGenerateCsharpProtocol()) {
            GenerateCsUtils.init();
            GenerateCsUtils.createProtocolManager();
            allSortedGenerateProtocols.forEach(it -> GenerateCsUtils.createCsProtocolFile((ProtocolRegistration) it));
        }

        // 生成Javascript协议
        if (generateOperation.isGenerateJsProtocol()) {
            GenerateJsUtils.init();
            allSortedGenerateProtocols.forEach(it -> GenerateJsUtils.createJsProtocolFile((ProtocolRegistration) it));
            GenerateJsUtils.createProtocolManager(allSortedGenerateProtocols);
        }

        // 生成Lua协议
        if (generateOperation.isGenerateLuaProtocol()) {
            GenerateLuaUtils.init();
            GenerateLuaUtils.createProtocolManager(allSortedGenerateProtocols);
            allSortedGenerateProtocols.forEach(it -> GenerateLuaUtils.createLuaProtocolFile((ProtocolRegistration) it));
        }

        // 参数，以后可能会用，比如给Lua修改一个后缀名称
        var protocolParam = generateOperation.getProtocolParam();
    }

}
