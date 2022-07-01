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

package com.zfoo.net.config.model;

import com.zfoo.protocol.registration.ProtocolModule;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ProviderModule {

    private String provider;

    private ProtocolModule protocolModule;

    public ProviderModule(String provider, ProtocolModule protocolModule) {
        this.provider = provider;
        this.protocolModule = protocolModule;
    }

    public ProviderModule(String provider, String protocolModule) {
        this.provider = provider;
        this.protocolModule = new ProtocolModule((byte) 0, protocolModule);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ProtocolModule getProtocolModule() {
        return protocolModule;
    }

    public void setProtocolModule(ProtocolModule protocolModule) {
        this.protocolModule = protocolModule;
    }
}
