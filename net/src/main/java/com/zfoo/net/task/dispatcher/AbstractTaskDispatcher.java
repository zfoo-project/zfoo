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
 *
 */

package com.zfoo.net.task.dispatcher;

import com.zfoo.protocol.util.StringUtils;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class AbstractTaskDispatcher implements ITaskDispatcher {

    public static ITaskDispatcher valueOf(String taskDispatchName) {
        switch (taskDispatchName) {
            case "random":
                return new RandomTaskDispatcher();
            case "sessionId":
                return new SessionIdTaskDispatcher();
            case "consistent-hash":
                return new ConsistentHashTaskDispatcher();
            default:
                throw new RuntimeException(StringUtils.format("没有找到对应的taskDispatch[{}]", taskDispatchName));
        }
    }

}
