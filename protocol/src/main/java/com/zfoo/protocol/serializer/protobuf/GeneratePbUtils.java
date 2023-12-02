/*
 * Copyright 2021 The edap Project
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

package com.zfoo.protocol.serializer.protobuf;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.codegen.IfaceGenerator;
import com.zfoo.protocol.serializer.protobuf.wire.parser.Proto;
import com.zfoo.protocol.serializer.protobuf.wire.parser.ProtoParser;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeneratePbUtils {

    public static void create(PbBuildOption buildOption) {
        var protoPathFile = new File(buildOption.getProtoPath());
        if (!protoPathFile.exists()) {
            throw new RuntimeException(StringUtils.format("proto path:[{}] not exist", buildOption.getProtoPath()));
        }

        var protoFiles = FileUtils.getAllReadableFiles(protoPathFile)
                .stream()
                .filter(it -> it.getName().toLowerCase().endsWith(".proto"))
                .toList();

        if (CollectionUtils.isEmpty(protoFiles)) {
            throw new RuntimeException(StringUtils.format("There are no proto files to build in proto path:[{}]", buildOption.getProtoPath()));
        }

        var srcPath = new File(buildOption.getOutputPath());

        var protos = parseProtoFile(protoFiles);
        IfaceGenerator ifaceGenerator = new IfaceGenerator(srcPath, protos);
        ifaceGenerator.generate();
    }

    public static List<Proto> parseProtoFile(List<File> protoFiles) {
        var protos = new ArrayList<Proto>();
        for (var protoFile : protoFiles) {
            var strs = FileUtils.readFileToStringList(protoFile)
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .toArray();
            var protoString = StringUtils.joinWith(FileUtils.LS, strs);
            if (StringUtils.isBlank(protoString)) {
                continue;
            }
            ProtoParser parser = new ProtoParser(protoString);
            Proto proto = parser.parse();
            proto.setFile(protoFile);
            protos.add(proto);
        }
        return protos;
    }

}
