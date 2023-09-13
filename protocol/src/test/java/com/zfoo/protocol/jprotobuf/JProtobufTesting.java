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

package com.zfoo.protocol.jprotobuf;

import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.packet.ProtobufObject;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.JsonUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 */
@Ignore
public class JProtobufTesting {

    private static final Map<Integer, String> mapWithInteger = new HashMap<>(Map.of(Integer.MIN_VALUE, "a", -99, "b", 0, "c", 99, "d", Integer.MAX_VALUE, "e"));

    @Test
    public void deserializerTest() throws IOException {
        var simpleTypeCodec = ProtobufProxy.create(ObjectA.class);

        var obj = new ObjectA();
        obj.a = Integer.MAX_VALUE;
        obj.m = mapWithInteger;

        // 序列化
        byte[] bytes = simpleTypeCodec.encode(obj);
        // 反序列化
        var newObj = simpleTypeCodec.decode(bytes);

        // 反序列化到protobuf
        var newProtobufObj = ProtobufObject.ObjectA.parseFrom(bytes);
        System.out.println(JsonUtils.object2String(newObj));
    }


    @Test
    public void serializerTest() throws IOException {
        // 原生protobuf对象
        var protobufObjectB = ProtobufObject.ObjectB.newBuilder().setFlag(false).build();
        var protobufObjectA = ProtobufObject.ObjectA.newBuilder()
                .setA(Integer.MAX_VALUE)
                .putAllM(mapWithInteger)
                .setObjectB(protobufObjectB)
                .build();

        byte[] bytes = protobufObjectA.toByteArray();

        var simpleTypeCodec = ProtobufProxy.create(ObjectA.class);
        var newObj = simpleTypeCodec.decode(bytes);
        System.out.println(JsonUtils.object2String(newObj));
    }

    @Test
    public void generateTest() throws IOException {
        var op = GenerateOperation.NO_OPERATION;
        op.getGenerateLanguages().add(CodeLanguage.Protobuf);
        op.setFoldProtocol(true);
//        op.setProtocolParam("protobuf=protobuf.xml");

        ProtocolManager.initProtocol(Set.of(ObjectA.class, ObjectB.class, ObjectC.class), op);
    }

}
