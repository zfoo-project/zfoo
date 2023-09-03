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

import java.util.Map;

/**
 * 同步网关的session信息到push
 *
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 24)
public class GatewaySynchronizeSidAsk implements IPacket {

    private String gatewayHostAndPort;

    private Map<Long, Long> sidMap;

    public static GatewaySynchronizeSidAsk valueOf(String gatewayHostAndPort, Map<Long, Long> sidMap) {
        var ask = new GatewaySynchronizeSidAsk();
        ask.gatewayHostAndPort = gatewayHostAndPort;
        ask.sidMap = sidMap;
        return ask;
    }

    public Map<Long, Long> getSidMap() {
        return sidMap;
    }

    public void setSidMap(Map<Long, Long> sidMap) {
        this.sidMap = sidMap;
    }

    public String getGatewayHostAndPort() {
        return gatewayHostAndPort;
    }

    public void setGatewayHostAndPort(String gatewayHostAndPort) {
        this.gatewayHostAndPort = gatewayHostAndPort;
    }
}
