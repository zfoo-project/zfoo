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

package com.zfoo.net.core.gateway;

/**
 * 网关负载均衡使用计算一致性hash的参数，如果packet继承了这个接口，则网关的一致性hash负载均衡优先使用这个接口计算一致性hash；
 *
 * @author godotg
 */
public interface IGatewayLoadBalancer {

    Object loadBalancerConsistentHashObject();

}
