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

import com.zfoo.protocol.serializer.protobuf.GeneratePbUtils;
import com.zfoo.protocol.serializer.protobuf.PbGenerateOperation;
import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class GenerateProtobufTesting {

    @Test
    public void generateClassFromProto() {
        var buildOption = new PbGenerateOperation();
        buildOption.setProtoPath("src\\test\\protobuf");
        buildOption.setOutputPath("zfoopb");
        buildOption.setJavaPackage("com.zfoo.protocol.generate.test");
        buildOption.setRecordClass(true);
//        buildOption.setAllArgsConstructor(true);
//        buildOption.getRecordExcludes().add("SimpleObject");
//        buildOption.setOneProtocol(true);
        GeneratePbUtils.create(buildOption);
    }
}
