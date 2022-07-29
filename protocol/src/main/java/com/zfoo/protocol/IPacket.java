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

package com.zfoo.protocol;

/**
 * 所有协议类都必须实现这个接口
 *
 * @author godotg
 * @version 3.0
 */
public interface IPacket {

    /**
     * 这个类的协议号
     * <p>
     * 子类可以不用重写这个方法，也能够通过反射自动获取到PROTOCOL_ID这个协议号，序列化一次对象只会调用一次，性能损失很小
     * <p>
     * 重写这个方法，使用多态获取协议号，可以提高一点性能
     *
     * @return 协议号Id
     */
    default short protocolId() {
        return ProtocolManager.protocolId(this.getClass());
    }

}
