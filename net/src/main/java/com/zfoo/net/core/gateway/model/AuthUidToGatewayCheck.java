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

package com.zfoo.net.core.gateway.model;

import com.zfoo.net.packet.IPacket;
import com.zfoo.protocol.anno.Protocol;

/**
 * 网关登录成功过后，将uid授权给网关
 *
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 20)
public class AuthUidToGatewayCheck implements IPacket {

    private long uid;

    public static AuthUidToGatewayCheck valueOf(long uid) {
        var authUidToGateway = new AuthUidToGatewayCheck();
        authUidToGateway.uid = uid;
        return authUidToGateway;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
