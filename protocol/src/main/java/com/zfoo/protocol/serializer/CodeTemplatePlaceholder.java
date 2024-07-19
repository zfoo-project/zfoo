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

package com.zfoo.protocol.serializer;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author godotg
 */
public enum CodeTemplatePlaceholder {

    protocol_root_path("${protocol_root_path}"),

    protocol_imports("${protocol_imports}"),

    protocol_class("${protocol_class}"),

    protocol_registration("${protocol_registration}"),

    protocol_manager_registrations("${protocol_manager_registrations}"),

    protocol_note("${protocol_note}"),

    protocol_name("${protocol_name}"),

    protocol_id("${protocol_id}"),

    protocol_field_definition("${protocol_field_definition}"),

    protocol_write_serialization("${protocol_write_serialization}"),

    protocol_read_deserialization("${protocol_read_deserialization}"),

    protocol_json("${protocol_json}"),

    protocol_to_string("${protocol_to_string}"),

    ;
    public final String placeholder;

    private CodeTemplatePlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }


    public static String formatTemplate(String template, Map<CodeTemplatePlaceholder, String> placeholderMap) {
        var lines = template.split(FileUtils.LS_REGEX);
        var formatLines = new ArrayList<String>();
        for (var line : lines) {
            var lineTrim = StringUtils.trim(line);
            var startPlaceholderOptional = Arrays.stream(CodeTemplatePlaceholder.values())
                    .filter(it -> lineTrim.startsWith(it.placeholder))
                    .findAny();
            var formatLine = line;
            // 形如 space ${protocol_class} 的占位符多了几个space，在生成代码的时候将每一行都添加相等的space，以此来对齐生成的多行代码块
            if (startPlaceholderOptional.isPresent()) {
                var startPlaceholder = startPlaceholderOptional.get();
                // calculate the tab length
                var startSpace = StringUtils.substringBeforeFirst(line, startPlaceholder.placeholder);
                var startPlaceholderValue = placeholderMap.get(startPlaceholder);
                if (startPlaceholderValue == null) {
                    throw new RunException("placeholder:[{}] not exist, and add [{}] to your placeholderMap", startPlaceholder, startPlaceholder);
                }
                var startPlaceholderValueLines = Arrays.stream(startPlaceholderValue.split(FileUtils.LS_REGEX)).map(it -> startSpace + it).toList();

                // add tab length to start placeholder
                var newStartPlaceholderValue = StringUtils.joinWith(FileUtils.LS, startPlaceholderValueLines.toArray());

                formatLine = newStartPlaceholderValue + StringUtils.substringAfterFirst(line, startPlaceholder.placeholder);
            }

            for (var codeTemplatePlaceholder : CodeTemplatePlaceholder.values()) {
                if (formatLine.contains(codeTemplatePlaceholder.placeholder)) {
                    var placeholder = placeholderMap.get(codeTemplatePlaceholder);
                    if (placeholder == null) {
                        throw new RunException("placeholder:[{}] not exist, and add [{}] to your placeholderMap", codeTemplatePlaceholder, codeTemplatePlaceholder);
                    }
                    formatLine = formatLine.replace(codeTemplatePlaceholder.placeholder, placeholder);
                }
            }

            formatLines.add(formatLine);
        }
        return StringUtils.joinWith(FileUtils.LS, formatLines.toArray());
    }

}
