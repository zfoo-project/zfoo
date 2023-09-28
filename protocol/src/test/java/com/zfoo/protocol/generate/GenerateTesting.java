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
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

/**
 * @author godotg
 */
@Ignore
public class GenerateTesting {

    @Test
    public void generateAllProtocols() {
        var op = GenerateOperation.NO_OPERATION;
        op.setFoldProtocol(true);

        var generateLanguages = op.getGenerateLanguages();

        // generate the jsProtocol folder and its corresponding js protocol file in the current protocol directory
        generateLanguages.add(CodeLanguage.Cpp);
        generateLanguages.add(CodeLanguage.Go);
        generateLanguages.add(CodeLanguage.JavaScript);
        generateLanguages.add(CodeLanguage.TypeScript);
        generateLanguages.add(CodeLanguage.Lua);
        generateLanguages.add(CodeLanguage.CSharp);
        generateLanguages.add(CodeLanguage.GdScript);
        generateLanguages.add(CodeLanguage.Python);

        // Protobuf needs to specify protobuf.xml to generate a protocol
        op.setProtocolParam("protobuf=protobuf.xml");
        generateLanguages.add(CodeLanguage.Protobuf);

        // Initialize and then generate the protocol
        ProtocolManager.initProtocolAuto(Set.of(ComplexObject.class, NormalObject.class, SimpleObject.class, EmptyObject.class, VeryBigObject.class), op);
    }

}
