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

package com.zfoo.protocol.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zfoo.protocol.util.model.User;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author godotg
 */


@Ignore
public class ReflectUtilTesting {

    @Test
    public void testGetFieldsByAnnotation() {
        Field[] fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(User.class, JsonIgnore.class);
        System.out.println(fields.length);
    }

    @Test
    public void testFilterFieldsInClass() {
        ReflectionUtils.filterFieldsInClass(User.class, new Predicate<Field>() {
            @Override
            public boolean test(Field field) {
                return field != null;
            }
        }, new Consumer<Field>() {
            @Override
            public void accept(Field field) {
                System.out.println(field.getName());
            }
        });
    }

}
