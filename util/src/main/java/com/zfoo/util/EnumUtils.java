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

package com.zfoo.util;

import com.zfoo.protocol.util.AssertionUtils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class EnumUtils {

    public static <E extends Enum<E>> boolean isInEnums(String targetEnumName, E[] sourceEnums) {
        AssertionUtils.notNull(targetEnumName, sourceEnums);
        return Arrays.stream(sourceEnums).anyMatch(sourceEnum -> sourceEnum.name().equals(targetEnumName));
    }


    public static <E> Set<E> enumerationToSet(Enumeration<E> enumeration) {
        var set = new HashSet<E>();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                set.add(enumeration.nextElement());
            }
        }
        return set;
    }

}
