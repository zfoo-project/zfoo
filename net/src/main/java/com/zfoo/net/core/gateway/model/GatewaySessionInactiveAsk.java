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

import com.zfoo.protocol.anno.Protocol;

/**
 * @author godotg
 */
@Protocol(id = 23)
public class GatewaySessionInactiveAsk {

    private String gatewayHostAndPort;

    private long sid;
    private long uid;

    public static GatewaySessionInactiveAsk valueOf(String gatewayHostAndPort, long sid, long uid) {
        var ask = new GatewaySessionInactiveAsk();
        ask.gatewayHostAndPort = gatewayHostAndPort;
        ask.sid = sid;
        ask.uid = uid;
        return ask;
    }

    public String getGatewayHostAndPort() {
        return gatewayHostAndPort;
    }

    public void setGatewayHostAndPort(String gatewayHostAndPort) {
        this.gatewayHostAndPort = gatewayHostAndPort;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
