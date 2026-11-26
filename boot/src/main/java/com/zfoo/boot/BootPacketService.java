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

package com.zfoo.boot;

import com.zfoo.net.packet.PacketService;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;

class BootPacketService extends PacketService {

    @Override
    protected void initProtocols(ApplicationContext context, String protocolLocation, GenerateOperation operation) {
        if (StringUtils.isNotBlank(protocolLocation)) {
            super.initProtocols(context, protocolLocation, operation);
            return;
        }
        if (!AutoConfigurationPackages.has(context)) {
            throw new IllegalStateException("Set NetConfig.protocolLocation when no Boot base packages are registered");
        }
        initProtocolPackages(context, AutoConfigurationPackages.get(context), false, operation);
    }
}
