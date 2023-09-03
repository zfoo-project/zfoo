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

package com.zfoo.protocol.jprotobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.zfoo.protocol.anno.Protocol;

import java.util.Map;

/**
 * ObjectC的测试类型
 *
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 104)
public class ObjectC {

    // int类型，在protobuf中叫int32
    @Protobuf(order = 1)
    public int a;

    // map类型
    // protobuf中的map的key只能为基础类型
    @Protobuf(order = 2)
    public Map<Integer, String> m;

    /**
     * 对象类型
     */
    @Protobuf(order = 3)
    public ObjectB objectB;

}
