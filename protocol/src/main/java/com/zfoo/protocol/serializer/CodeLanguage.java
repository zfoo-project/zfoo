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

import com.zfoo.protocol.serializer.cpp.CodeGenerateCpp;
import com.zfoo.protocol.serializer.csharp.CodeGenerateCsharp;
import com.zfoo.protocol.serializer.lua.CodeGenerateLua;

/**
 * @author godotg
 */
public enum CodeLanguage {

    /**
     * Javassist字节码增强
     */
    Enhance(1, null),

    Cpp(1 << 1, CodeGenerateCpp.class),

    Go(1 << 2, null),

    JavaScript(1 << 3, null),

    ES(1 << 4, null),

    TypeScript(1 << 5, null),

    Lua(1 << 10, CodeGenerateLua.class),

    CSharp(1 << 11, CodeGenerateCsharp.class),

    GdScript(1 << 12, null),

    Python(1 << 13, null),

    Protobuf(1 << 30, null);

    public final int id;
    public final Class<? extends ICodeGenerate> codeGenerateClass;

    private CodeLanguage(int id, Class<? extends ICodeGenerate> codeGenerateClass) {
        this.id = id;
        this.codeGenerateClass = codeGenerateClass;
    }

}
