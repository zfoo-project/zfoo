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

package com.zfoo.protocol.serializer;

import java.util.concurrent.atomic.AtomicInteger;

import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class GenerateUtils {

    public static AtomicInteger index = new AtomicInteger();

    public static StringBuilder addTab(StringBuilder builder, int deep) {
        builder.append(TAB.repeat(Math.max(0, deep)));
        return builder;
    }

    public static void clear() {
        index = null;
    }

}
