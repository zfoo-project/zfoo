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

package com.zfoo.protocol.protobuf;

import com.zfoo.protocol.serializer.protobuf.GeneratePbUtils;
import com.zfoo.protocol.serializer.protobuf.PbBuildOption;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Test;


public class AllTypeBuilderTest {

    @Test
    public void testAllTypeTest() {
        var buildOption = new PbBuildOption();
        buildOption.setProtoPath("D:\\github\\zfoo\\protocol\\src\\test\\resources\\proto");
//        buildOption.setProtoPath("D:\\github\\zoo\\app\\zgame\\zgame-common\\src\\main\\resources\\proto");
        buildOption.setOutputPath("D:\\github\\zfoo\\protocol\\src\\test\\tmpsrc/");

        GeneratePbUtils.create(buildOption);
        ThreadUtils.sleep(1000);
    }
}
