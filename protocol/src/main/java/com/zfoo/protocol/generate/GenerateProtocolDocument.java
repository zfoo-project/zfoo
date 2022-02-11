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

package com.zfoo.protocol.generate;

import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * 生成协议的时候，协议的文档注释和字段注释会使用这个类
 *
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class GenerateProtocolDocument {

    // 临时变量，启动完成就会销毁，协议的文档，外层map的key为协议类；pair的key为总的注释，value为属性字段的注释，value表示的map的key为属性名称
    // 比如在Test中的ComplexObject生成的pari是如下格式
    /**
     * key docTitle:
     * // 复杂的对象
     * // 包括了各种复杂的结构，数组，List，Set，Map
     * //
     * // @author jaysunxiao
     * // @version 1.0
     * <p>
     * value aa:
     * // byte的包装类型
     * // 优先使用基础类型，包装类型会有装箱拆箱
     */
    private static Map<Short, Pair<String, Map<String, String>>> protocolDocumentMap = new HashMap<>();


    public static void clear() {
        protocolDocumentMap.clear();
        protocolDocumentMap = null;
    }


    /**
     * 此方法仅在生成协议的时候调用，一旦运行，不能调用
     */
    public static Pair<String, Map<String, String>> getProtocolDocument(short protocolId) {
        AssertionUtils.notNull(protocolDocumentMap, "[{}]已经初始完成，初始化完成过后不能调用getProtocolDocument", GenerateProtocolDocument.class.getSimpleName());

        var protocolDocument = protocolDocumentMap.get(protocolId);
        if (protocolDocument == null) {
            return new Pair<>(StringUtils.EMPTY, Collections.emptyMap());
        }
        return protocolDocument;
    }


    public static void initProtocolDocument(List<IProtocolRegistration> protocolRegistrations) {
        AssertionUtils.notNull(protocolDocumentMap, "[{}]已经初始完成，初始化完成过后不能调用initProtocolDocument", GenerateProtocolDocument.class.getSimpleName());

        // 文件的注释生成
        var proAbsFile = new File(FileUtils.getProAbsPath());
        var list = FileUtils.getAllReadableFiles(proAbsFile.getParentFile() == null ? proAbsFile : proAbsFile.getParentFile())
                .stream()
                .filter(it -> it.getName().endsWith(".java"))
                .collect(Collectors.toList());

        for (var protocolRegistration : protocolRegistrations) {
            var protocolClazz = protocolRegistration.protocolConstructor().getDeclaringClass();
            var protocolClazzName = protocolClazz.getName();

            var protocolFile = list.stream()
                    .filter(it -> it.getAbsolutePath().replace(StringUtils.SLASH, StringUtils.PERIOD).replace(StringUtils.BACK_SLASH, StringUtils.PERIOD).endsWith(StringUtils.format("{}.java", protocolClazzName)))
                    .findFirst();

            // 如果搜索不到协议文件则直接返回
            if (protocolFile.isEmpty()) {
                continue;
            }

            var docFieldMap = new HashMap<String, String>();
            var docTitle = StringUtils.EMPTY;

            var protocolStringList = FileUtils.readFileToStringList(protocolFile.get())
                    .stream()
                    .dropWhile(it -> !it.startsWith("package")) // 过滤掉package之上的版权信息
                    .collect(Collectors.toList());

            // 搜索包名，报名不匹配则直接返回
            var protocolClassTitle = StringUtils.format("public class {}", protocolClazz.getSimpleName());
            if (protocolStringList.stream().noneMatch(it -> it.contains(protocolClassTitle))) {
                continue;
            }

            protocolStringList = protocolStringList.stream()
                    .dropWhile(it -> !it.startsWith("package"))
                    .collect(Collectors.toList());

            var docBuilder = new StringBuilder();
            var docTitleBuilder = new StringBuilder();
            for (var line : protocolStringList) {
                var startLineStr = line.trim();

                // 排除java的包头
                if (startLineStr.startsWith("package") || startLineStr.startsWith("import")) {
                    continue;
                }


                if (startLineStr.startsWith("public class ")) {
                    if (docTitleBuilder != null) {
                        docTitle = docTitleBuilder.toString();
                        docTitle = docTitle.replace("/**", StringUtils.EMPTY);
                        docTitle = docTitle.replace(" */", StringUtils.EMPTY);
                        docTitle = docTitle.replace(" *", "//");
                        docTitle = docTitle.trim();
                        docBuilder = new StringBuilder();
                        docTitleBuilder = null;
                    }
                } else {
                    if (docTitleBuilder != null) {
                        docTitleBuilder.append(line).append(LS);
                    }
                }

                // 保留注释
                if (startLineStr.startsWith("*/")) {
                    continue;
                }

                if (startLineStr.startsWith("//") || startLineStr.startsWith("*")) {
                    startLineStr = startLineStr.replaceFirst("//", StringUtils.EMPTY);
                    startLineStr = startLineStr.replaceFirst("\\*", StringUtils.EMPTY);
                    docBuilder.append("//").append(startLineStr).append(LS);
                    continue;
                }

                if (startLineStr.startsWith("private static ")) {
                    continue;
                }

                if (startLineStr.contains(" transient ")) {
                    continue;
                }

                if (startLineStr.startsWith("public void set") || startLineStr.startsWith("public bool equals")
                        || startLineStr.startsWith("public int hashCode") || startLineStr.startsWith("@Override")) {
                    continue;
                }

                if (startLineStr.endsWith("{") || startLineStr.startsWith("return ") || startLineStr.startsWith("}")) {
                    continue;
                }

                if (!startLineStr.endsWith(";")) {
                    continue;
                }
                if (!(startLineStr.startsWith("private ") || startLineStr.startsWith("public "))) {
                    continue;
                }

                var fieldName = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(startLineStr, StringUtils.SPACE), StringUtils.SEMICOLON).trim();
                docFieldMap.put(fieldName, docBuilder.toString());
                docBuilder = new StringBuilder();
            }

            protocolDocumentMap.put(protocolRegistration.protocolId(), new Pair<>(docTitle, docFieldMap));
        }
    }

}
