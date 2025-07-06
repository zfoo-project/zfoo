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
package com.zfoo.net.core.broker;

import com.zfoo.protocol.anno.Protocol;

/**
 * @author jaysunxiao
 */
@Protocol(id = 150)
public class BrokerRegisterAsk {

    // 不带gpu的服务器
    public static final int HOME = 1;
    // 带了gpu的服务器
    public static final int HOME_GPU = 2;

    private int brokerType;

    public BrokerRegisterAsk() {
    }

    public BrokerRegisterAsk(int brokerType) {
        this.brokerType = brokerType;
    }

    public int getBrokerType() {
        return brokerType;
    }

    public void setBrokerType(int brokerType) {
        this.brokerType = brokerType;
    }
}
