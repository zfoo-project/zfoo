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

import com.zfoo.protocol.anno.Note;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

/**
 * EN: When generating the protocol, the document comments and field comments of the protocol will use this class
 * CN: 生成协议的时候，协议的文档注释和字段注释会使用这个类
 *
 * @author godotg
 */
public abstract class GenerateProtocolNote {

    // 临时变量，启动完成就会销毁，协议的文档，外层map的key为协议类；pair的key为总的注释，value为属性字段的注释，value表示的map的key为属性名称
    // 比如在Test中的ComplexObject生成的pari是如下格式
    /**
     * key is docTitle note:
     * // 复杂的对象
     * // 包括了各种复杂的结构，数组，List，Set，Map
     * //
     * // @author godotg
     * // @version 1.0
     * <p>
     * value is field note:
     * // byte的包装类型
     * // 优先使用基础类型，包装类型会有装箱拆箱
     */
    private static Map<Short, Pair<String, Map<String, String>>> protocolNoteMap = new HashMap<>();


    public static void clear() {
        protocolNoteMap.clear();
        protocolNoteMap = null;
    }

    public static String protocol_note(short protocolId, CodeLanguage codeLanguage) {
        var protocolNote = protocolNoteMap.get(protocolId);
        var classNote = protocolNote.getKey();
        if (StringUtils.isBlank(classNote)) {
            return StringUtils.EMPTY;
        }
        return formatNote(codeLanguage, classNote);
    }

    public static String classNote(short protocolId, CodeLanguage language, String tab, int deep) {
        var protocolNote = protocolNoteMap.get(protocolId);
        var classNote = protocolNote.getKey();
        if (StringUtils.isBlank(classNote)) {
            return StringUtils.EMPTY;
        }
        var multipleLineNotes = classNote.split(FileUtils.LS_REGEX);
        var notes = new ArrayList<String>();
        for(var oneLineNote : multipleLineNotes) {
            var formatFieldNote = formatNote(language, oneLineNote);
            notes.add(tab.repeat(Math.max(0, deep)) + formatFieldNote);
        }
        return StringUtils.joinWith(FileUtils.LS, notes.toArray());
    }

    public static List<String> fieldNotes(short protocolId, String fieldName, CodeLanguage language) {
        var protocolNote = protocolNoteMap.get(protocolId);
        var fieldNoteMap = protocolNote.getValue();
        var fieldNote = fieldNoteMap.get(fieldName);
        if (StringUtils.isBlank(fieldNote)) {
            return Collections.emptyList();
        }
        var multipleLineNotes = fieldNote.split(FileUtils.LS_REGEX);
        var notes = new ArrayList<String>();
        for(var oneLineNote : multipleLineNotes) {
            var formatFieldNote = formatNote(language, oneLineNote);
            notes.add(formatFieldNote);
        }
        return notes;
    }

    private static String formatNote(CodeLanguage language, String note) {
        switch (language) {
            case Cpp:
            case Go:
            case JavaScript:
            case EcmaScript:
            case TypeScript:
            case CSharp:
            case Protobuf:
                note = StringUtils.format("// {}", note);
                break;
            case Lua:
                note = StringUtils.format("-- {}", note);
                break;
            case Python:
            case GdScript:
                note = StringUtils.format("# {}", note);
                break;
            case Enhance:
            default:
                throw new RunException("unrecognized enum type [{}]", language);
        }
        return note;
    }

    public static void initProtocolNote(List<ProtocolRegistration> protocolRegistrations) {
        AssertionUtils.notNull(protocolNoteMap, "[{}] duplicate initialization", GenerateProtocolNote.class.getSimpleName());

        for (var registration : protocolRegistrations) {
            var protocolClazz = registration.protocolConstructor().getDeclaringClass();
            var classNote = StringUtils.EMPTY;
            var protocolClass = protocolClazz.getDeclaredAnnotation(Note.class);
            if (protocolClass != null && StringUtils.isNotEmpty(protocolClass.value())) {
                classNote = StringUtils.trim(protocolClass.value());
            }

            var fieldNoteMap = new HashMap<String, String>();
            for (var field : registration.getFields()) {
                var noteDescription = field.getDeclaredAnnotation(Note.class);
                if (noteDescription == null || StringUtils.isEmpty(noteDescription.value())) {
                    continue;
                }
                var fieldName = field.getName();
                var fieldNote = noteDescription.value();
                fieldNoteMap.put(fieldName, StringUtils.trim(fieldNote));
            }

            protocolNoteMap.put(registration.protocolId(), new Pair<>(classNote, fieldNoteMap));
        }
    }

}
