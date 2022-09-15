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

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.anno.Note;
import com.zfoo.protocol.registration.anno.Protocol;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成协议的时候，协议的文档注释和字段注释会使用这个类
 *
 * @author godotg
 * @version 3.0
 */
public abstract class GenerateProtocolNote {

    // 临时变量，启动完成就会销毁，协议的文档，外层map的key为协议类；pair的key为总的注释，value为属性字段的注释，value表示的map的key为属性名称
    // 比如在Test中的ComplexObject生成的pari是如下格式
    /**
     * key docTitle:
     * // 复杂的对象
     * // 包括了各种复杂的结构，数组，List，Set，Map
     * //
     * // @author godotg
     * // @version 1.0
     * <p>
     * value aa:
     * // byte的包装类型
     * // 优先使用基础类型，包装类型会有装箱拆箱
     */
    private static Map<Short, Pair<String, Map<String, String>>> protocolNoteMap = new HashMap<>();


    public static void clear() {
        protocolNoteMap.clear();
        protocolNoteMap = null;
    }

    public static String classNote(short protocolId, CodeLanguage language) {
        var protocolNote = protocolNoteMap.get(protocolId);
        var classNote = protocolNote.getKey();
        if (StringUtils.isBlank(classNote)) {
            return StringUtils.EMPTY;
        }

        switch (language) {
            case Cpp:
            case Go:
            case JavaScript:
            case TypeScript:
            case CSharp:
            case Protobuf:
                classNote = StringUtils.format("// {}", classNote);
                break;
            case Lua:
                classNote = StringUtils.format("-- {}", classNote);
                break;
            case GdScript:
                classNote = StringUtils.format("# {}", classNote);
                break;
            case Enhance:
            default:
                throw new RunException("无法识别的枚举类型[{}]", language);
        }
        return classNote;
    }

    public static String fieldNote(short protocolId, String fieldName, CodeLanguage language) {
        var protocolNote = protocolNoteMap.get(protocolId);
        var fieldNoteMap = protocolNote.getValue();
        var fieldNote = fieldNoteMap.get(fieldName);
        if (StringUtils.isBlank(fieldNote)) {
            return StringUtils.EMPTY;
        }
        switch (language) {
            case Cpp:
            case Go:
            case JavaScript:
            case TypeScript:
            case CSharp:
            case Protobuf:
                fieldNote = StringUtils.format("// {}", fieldNote);
                break;
            case Lua:
                fieldNote = StringUtils.format("-- {}", fieldNote);
                break;
            case GdScript:
                fieldNote = StringUtils.format("# {}", fieldNote);
                break;
            case Enhance:
            default:
                throw new RunException("无法识别的枚举类型[{}]", language);
        }
        return fieldNote;
    }

    public static void initProtocolNote(List<IProtocolRegistration> protocolRegistrations) {
        AssertionUtils.notNull(protocolNoteMap, "[{}]已经初始完成，初始化完成过后不能调用initProtocolDocument", GenerateProtocolNote.class.getSimpleName());

        for (var protocolRegistration : protocolRegistrations) {
            var protocolClazz = protocolRegistration.protocolConstructor().getDeclaringClass();
            var classNote = StringUtils.EMPTY;
            var protocolClass = protocolClazz.getDeclaredAnnotation(Protocol.class);
            if (protocolClass != null && StringUtils.isNotEmpty(protocolClass.note())) {
                classNote = StringUtils.trim(protocolClass.note());
            }

            var fieldNoteMap = new HashMap<String, String>();
            var registration = (ProtocolRegistration) protocolRegistration;
            for (var field : registration.getFields()) {
                var noteDescription = field.getDeclaredAnnotation(Note.class);
                if (noteDescription == null || StringUtils.isEmpty(noteDescription.value())) {
                    continue;
                }
                var fieldName = field.getName();
                var fieldNote = noteDescription.value();
                fieldNoteMap.put(fieldName, StringUtils.trim(fieldNote));
            }

            protocolNoteMap.put(protocolRegistration.protocolId(), new Pair<>(classNote, fieldNoteMap));
        }
    }

}
