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

package com.zfoo.net.core.gateway;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.gateway.GatewayToProviderRequest;
import com.zfoo.net.packet.gateway.GatewayToProviderResponse;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 * @version 3.0
 */
@Component
public class GatewayProviderController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayProviderController.class);

    /**
     * 注意：这里第2个请求参数以Request结尾，那么第3个参数必须是 GatewayAttachment类型(参加：PacketBus中扫描时的校验)
     */
    @PacketReceiver
    public void atGatewayToProviderRequest(Session session, GatewayToProviderRequest request, GatewayAttachment gatewayAttachment) {
        logger.info("provider receive [packet:{}] from client", JsonUtils.object2String(request));

        var response = new GatewayToProviderResponse();
        response.setMessage(StringUtils.format("Hello, this is the [provider:{}] response!", NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO().toString()));

        NetContext.getRouter().send(session, response, gatewayAttachment);
    }
}
