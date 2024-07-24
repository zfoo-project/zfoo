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
import com.zfoo.protocol.packet.*;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author godotg
 */
@Ignore
public class GenerateTesting {

    @Test
    public void generateAllProtocols() throws IOException {
        var op = GenerateOperation.NO_OPERATION;
        op.setFoldProtocol(true);

        var generateLanguages = op.getGenerateLanguages();

        // generate the jsProtocol folder and its corresponding js protocol file in the current protocol directory
        generateLanguages.add(CodeLanguage.Cpp);
        generateLanguages.add(CodeLanguage.Rust);
        generateLanguages.add(CodeLanguage.Java);
        generateLanguages.add(CodeLanguage.Kotlin);
        generateLanguages.add(CodeLanguage.Scala);
        generateLanguages.add(CodeLanguage.Golang);
        generateLanguages.add(CodeLanguage.JavaScript);
        generateLanguages.add(CodeLanguage.EcmaScript);
        generateLanguages.add(CodeLanguage.TypeScript);
        generateLanguages.add(CodeLanguage.Lua);
        generateLanguages.add(CodeLanguage.Php);
        generateLanguages.add(CodeLanguage.CSharp);
        generateLanguages.add(CodeLanguage.GdScript);
        generateLanguages.add(CodeLanguage.Python);
        generateLanguages.add(CodeLanguage.Ruby);

        // Initialize and then generate the protocol
        ProtocolManager.initProtocolAuto(List.of(NormalObject.class, SimpleObject.class, EmptyObject.class), op);
        // copyFiles();
    }

    @Test
    public void copyFiles() throws IOException {
        var sourceDirectory = "D:\\Project\\zfoo\\protocol\\zfoophp";
        var targetDirectory = "D:\\github\\zfoo-php-support2\\zfoophp";
        FileUtils.deleteFile(new File(targetDirectory));
        FileUtils.copyDirectory(new File(sourceDirectory), new File(targetDirectory));
    }

}
