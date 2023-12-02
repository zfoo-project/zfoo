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

package com.zfoo.protocol.serializer.protobuf.codegen;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.PbBuildOption;
import com.zfoo.protocol.serializer.protobuf.builder.JavaBuilder;
import com.zfoo.protocol.serializer.protobuf.wire.Option;
import com.zfoo.protocol.serializer.protobuf.wire.ProtoMessage;
import com.zfoo.protocol.serializer.protobuf.wire.parser.Proto;
import com.zfoo.protocol.util.FileUtils;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IfaceGenerator {
    private final File srcPath;
    private final List<Proto> files;

    public IfaceGenerator(File srcPath, List<Proto> files) {
        this.srcPath = srcPath;
        this.files = files;
    }


    public void generate() {
        List<Proto> protos = files;
        Map<String, Proto> allProtos = new HashMap<>();
        for (Proto proto : protos) {
            allProtos.put(proto.getName(), proto);
        }
        protos.forEach(p -> {
            generateProtoItem(p, allProtos);
        });
    }

    private void generateProtoItem(Proto proto, Map<String, Proto> protos) {
        List<Option> options = proto.getOptions();
        Map<String, String> protoOptions = new HashMap<>();
        if (options != null) {
            options.forEach(o -> protoOptions.put(o.getName(), o.getValue()));
        }

        PbBuildOption buildOps = new PbBuildOption();

        generateDtoMessage(proto, buildOps, protos);
    }

    public void generateDtoMessage(Proto proto, PbBuildOption buildOps, Map<String, Proto> protos) {
        JavaBuilder builder = new JavaBuilder();

        Map<String, String> msgComments = new HashMap<>();

        List<ProtoMessage> msgs = proto.getMessages();
        if (CollectionUtils.isNotEmpty(msgs)) {
            String msgPath = srcPath + File.separator;
            msgs.stream()
                    .sorted(Comparator.comparing(ProtoMessage::getName))
                    .forEach(it -> {
                        StringBuilder mc = new StringBuilder();
                        if (it.getComment() != null) {
                            mc.append(it.getComment());
                        } else {
                            if (it.getComment() != null && it.getComment().getLines() != null) {
                                it.getComment().getLines().forEach(c -> mc.append(c));
                            }
                        }
                        msgComments.put(it.getName(), mc.toString());
                        var code = builder.buildMessage(proto, it, 1, null, buildOps, protos);
                        var filePath = msgPath + File.separator + it.getName() + ".java";
                        FileUtils.writeStringToFile(new File(filePath), code, false);
                    });

        }
    }
}
