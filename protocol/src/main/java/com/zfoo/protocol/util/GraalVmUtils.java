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

package com.zfoo.protocol.util;


import java.util.List;

/**
 * @author godotg
 */
public abstract class GraalVmUtils {

    private static final boolean GRAALVM;

    static {
        var properties = List.of(System.getProperty("java.vm.name", "")
                , System.getProperty("java.vm.version", "")
                , System.getProperty("java.vendor.version", "")
                , System.getProperty("java.vendor.url", "")
                , System.getProperty("org.graalvm.nativeimage.imagecode", "")
        );
        GRAALVM = properties.stream().map(it -> it.toLowerCase()).anyMatch(it -> it.contains("graalvm"));
    }

    public static boolean isGraalVM() {
        return GRAALVM;
    }

}
