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
import com.zfoo.protocol.serializer.dart.CodeGenerateDart;
import com.zfoo.protocol.serializer.ecmascript.CodeGenerateEcmaScript;
import com.zfoo.protocol.serializer.gdscript.CodeGenerateGdScript;
import com.zfoo.protocol.serializer.golang.CodeGenerateGolang;
import com.zfoo.protocol.serializer.java.CodeGenerateJava;
import com.zfoo.protocol.serializer.javascript.CodeGenerateJavaScript;
import com.zfoo.protocol.serializer.kotlin.CodeGenerateKotlin;
import com.zfoo.protocol.serializer.lua.CodeGenerateLua;
import com.zfoo.protocol.serializer.php.CodeGeneratePhp;
import com.zfoo.protocol.serializer.python.CodeGeneratePython;
import com.zfoo.protocol.serializer.ruby.CodeGenerateRuby;
import com.zfoo.protocol.serializer.rust.CodeGenerateRust;
import com.zfoo.protocol.serializer.scala.CodeGenerateScala;
import com.zfoo.protocol.serializer.typescript.CodeGenerateTypeScript;

/**
 * @author godotg
 */
public enum CodeLanguage {

    /**
     * Javassist字节码增强
     */
    Enhance(1, null),

    Java(1 << 1, CodeGenerateJava.class),

    Kotlin(1 << 2, CodeGenerateKotlin.class),

    Scala(1 << 3, CodeGenerateScala.class),

    Dart(1 << 3, CodeGenerateDart.class),

    Cpp(1 << 7, CodeGenerateCpp.class),

    Rust(1 << 8, CodeGenerateRust.class),

    Golang(1 << 9, CodeGenerateGolang.class),

    JavaScript(1 << 10, CodeGenerateJavaScript.class),

    EcmaScript(1 << 11, CodeGenerateEcmaScript.class),

    TypeScript(1 << 12, CodeGenerateTypeScript.class),

    Lua(1 << 15, CodeGenerateLua.class),

    CSharp(1 << 18, CodeGenerateCsharp.class),

    GdScript(1 << 20, CodeGenerateGdScript.class),

    Python(1 << 22, CodeGeneratePython.class),

    Php(1 << 28, CodeGeneratePhp.class),

    Ruby(1 << 29, CodeGenerateRuby.class),

    Protobuf(1 << 30, null);

    public final int id;
    public final Class<? extends ICodeGenerate> codeGenerateClass;

    private CodeLanguage(int id, Class<? extends ICodeGenerate> codeGenerateClass) {
        this.id = id;
        this.codeGenerateClass = codeGenerateClass;
    }

}
