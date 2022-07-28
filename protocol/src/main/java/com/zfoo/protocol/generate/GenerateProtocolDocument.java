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
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.anno.Description;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for (var protocolRegistration : protocolRegistrations) {
            var protocolClazz = protocolRegistration.protocolConstructor().getDeclaringClass();
            var docFieldMap = new HashMap<String, String>();
            var docTitle = StringUtils.EMPTY;
            var description = protocolClazz.getDeclaredAnnotation(Description.class);
            if (description != null) {
                var docTitleBuilder = new StringBuilder().append("//").append(description.value());
                docTitle = docTitleBuilder.toString();
            }

            var registration = (ProtocolRegistration) protocolRegistration;
            for (var field : registration.getFields()) {
                var fieldDescrption = field.getDeclaredAnnotation(Description.class);
                if (fieldDescrption == null) {
                    continue;
                }
                var docBuilder = new StringBuilder().append("//").append(fieldDescrption.value());
                var fieldName = field.getName();
                docFieldMap.put(fieldName, docBuilder.toString());
            }

            protocolDocumentMap.put(protocolRegistration.protocolId(), new Pair<>(docTitle, docFieldMap));
        }
    }

}
