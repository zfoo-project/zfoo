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

package com.zfoo.boot;

import com.zfoo.protocol.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class BootAutoConfiguration implements EnvironmentPostProcessor {

    private static final String SPRING_EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var exclusionList = List.of(
                "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.mongo.MongoAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration"
        );

        var excludes = environment.getProperty(SPRING_EXCLUDE_PROPERTY);

        var exclusionListStr = StringUtils.joinWith(StringUtils.COMMA, exclusionList.toArray());
        if (StringUtils.isBlank(excludes)) {
            excludes = exclusionListStr;
        } else {
            excludes = excludes + StringUtils.COMMA + exclusionListStr;
        }

        var map = new HashMap<String, Object>();
        map.put(SPRING_EXCLUDE_PROPERTY, excludes);

        overrideValues(environment.getPropertySources(), map);
    }

    private static final String PROPERTY_SOURCE_NAME = "configurationProperties";

    public static void overrideValues(MutablePropertySources propertySources, Map<String, Object> propertyMap) {
        MapPropertySource target = null;
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
            if (source instanceof MapPropertySource) {
                target = (MapPropertySource) source;
                for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
                    target.getSource().put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            target = new MapPropertySource(PROPERTY_SOURCE_NAME, propertyMap);
        }
        if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.addFirst(target);
        }
    }
}
