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

package com.zfoo.net.packet;

import com.zfoo.protocol.ProtocolManager;

/**
 * 所有协议类都必须实现这个接口，协议类必须是简单的javabean，不能继承任何其它的类，但是可以继承接口
 * <p>
 * 现在IPacket的接口只是一个标识接口，继承IPacket的设计主要是为了让代码更优雅，容易理解一点，改为只继承Object也并没有很大工作量
 * 继承IPacket的设计还有跨语言层面上的考虑，极大的简化了实现其它语言的序列化和反序列化难度，统一了其它语言的代码实现
 * <p>
 * 为了防止代码里Object满天飞，避免协议层和po层混用对象造成一些潜在的并发问题，zfoo强制要求协议类必须实现IPacket接口
 *
 * @author godotg
 * @version 3.0
 */
public interface IPacket {

    /**
     * 这个类的协议号，重写这个方法，使用多态获取协议号，可以微弱的提高一点性能
     * <p>
     * @return 协议号Id
     */
    default short protocolId() {
        return ProtocolManager.protocolId(this.getClass());
    }

}
