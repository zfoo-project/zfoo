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

package com.zfoo.storage.conversion;

import com.zfoo.storage.strategy.JsonToArrayConverter;
import com.zfoo.storage.strategy.JsonToMapConverter;
import com.zfoo.storage.strategy.StringToClassConverter;
import com.zfoo.storage.strategy.StringToDateConverter;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 * @version 3.0
 */
public class ConversionTest {
    private static final ConversionServiceFactoryBean csfb = new ConversionServiceFactoryBean();
    private static final Set<Object> converters = new HashSet<>();

    private static final StringToDateConverter std = new StringToDateConverter();
    private static final StringToClassConverter stcc = new StringToClassConverter();
    private static final JsonToMapConverter jtmc = new JsonToMapConverter();
    private static final JsonToArrayConverter jtac = new JsonToArrayConverter();

    static {
        converters.add(std);
        converters.add(stcc);
        converters.add(jtmc);
        converters.add(jtac);
        csfb.setConverters(converters);
        csfb.afterPropertiesSet();
    }


    @Test
    public void string2Integer() {
        ConversionService conversionService = csfb.getObject();
        Integer result = conversionService.convert("123", Integer.class);
        Assert.assertEquals(123, result.intValue());
    }

    @Test
    public void string2Map() {
        ConversionService conversionService = csfb.getObject();
        //Json to Map
        String str = "{\"1\":1,\"2\":2,\"3\":3}";

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        // 注意：第3个参数不能写成TypeDescriptor.valueOf(map.getClass())  而是要明确指定Map的key和value的类型
        map = (Map<Integer, Integer>) conversionService.convert(str, TypeDescriptor.valueOf(String.class), TypeDescriptor.map(map.getClass(), TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Integer.class)));

        Assert.assertEquals(3, map.size());
    }

    @Test
    public void string2Array() {
        ConversionService conversionService = csfb.getObject();
        String str = "[1,2,3]";

        Integer[] array = (Integer[]) conversionService.convert(str, TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Integer[].class));

        Assert.assertEquals(3, array.length);
    }

}
