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

import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.StringUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * EN: Action class for creating protocol files
 * Configuration class for protocol file generation
 *
 * @author godotg
 */
public class GenerateOperation {

    /**
     * EN: No protocol file is created
     * Do not generate any protocol files
     */
    public static final GenerateOperation NO_OPERATION = new GenerateOperation();

    /**
     * EN: Generate one protocol file under a package
     * CN: Generate all protocol files under a package into a single file
     */
    private boolean mergeProtocol;

    /**
     * EN: Fold the protocol. The generated protocol file will maintain the same directory structure as the Java source file.
     * It will not be folded by default.
     * <p>
     * Fold protocols; generated files mirror the Java source directory structure (default: not folded)
     */
    private boolean foldProtocol;

    /**
     * EN: The path to generate the protocol, the default is the path of the currently running project
     * Output path for generated protocols; defaults to the current project path
     */
    private String protocolPath = StringUtils.EMPTY;

    /**
     * Reserved parameters
     */
    private String protocolParam;

    /**
     * EN: The protocol files that need to be generated (c#, js, ts... and other languages that need to generate the protocol are listed)
     * List of target languages (C#, JS, TS, etc.) for which to generate protocol files
     */
    private final Set<CodeLanguage> generateLanguages = EnumSet.noneOf(CodeLanguage.class);

    public String getProtocolPath() {
        return protocolPath;
    }

    public void setProtocolPath(String protocolPath) {
        this.protocolPath = protocolPath;
    }

    public boolean isMergeProtocol() {
        return mergeProtocol;
    }

    public void setMergeProtocol(boolean mergeProtocol) {
        this.mergeProtocol = mergeProtocol;
    }

    public boolean isFoldProtocol() {
        return foldProtocol;
    }

    public void setFoldProtocol(boolean foldProtocol) {
        this.foldProtocol = foldProtocol;
    }

    public String getProtocolParam() {
        return protocolParam;
    }

    public void setProtocolParam(String protocolParam) {
        this.protocolParam = protocolParam;
    }

    public Set<CodeLanguage> getGenerateLanguages() {
        return generateLanguages;
    }

}
