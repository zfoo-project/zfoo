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

package com.zfoo.net.consumer.registry;

import com.zfoo.net.config.model.ConsumerConfig;
import com.zfoo.net.config.model.ConsumerModule;
import com.zfoo.net.config.model.ProviderConfig;
import com.zfoo.net.config.model.ProviderModule;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.registration.ProtocolModule;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.security.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class RegisterVO {

    private static final Logger logger = LoggerFactory.getLogger(RegisterVO.class);

    private static final String uuid = IdUtils.getUUID();

    private String id;
    private ProviderConfig providerConfig;
    private ConsumerConfig consumerConfig;


    public static boolean providerHasConsumerModule(RegisterVO provider, RegisterVO consumer) {
        if (Objects.isNull(provider) || Objects.isNull(provider.providerConfig) || CollectionUtils.isEmpty(provider.providerConfig.getProviders())
                || Objects.isNull(consumer) || Objects.isNull(consumer.consumerConfig) || CollectionUtils.isEmpty(consumer.consumerConfig.getConsumers())) {
            return false;
        }

        return provider.getProviderConfig().getProviders().stream().anyMatch(it -> consumer.getConsumerConfig().getConsumers().contains(it));
    }

    public static RegisterVO valueOf(String id, ProviderConfig providerConfig, ConsumerConfig consumerConfig) {
        RegisterVO config = new RegisterVO();
        config.id = id;
        config.providerConfig = providerConfig;
        config.consumerConfig = consumerConfig;
        return config;
    }

    @Nullable
    public static RegisterVO parseString(String str) {
        try {
            var vo = new RegisterVO();
            var splits = str.split("\\|");

            vo.id = splits[0].trim();

            String providerAddress = null;

            for (int i = 1; i < splits.length; i++) {
                var s = splits[i].trim();
                if (s.startsWith("provider")) {
                    var providerModules = parseProviderModules(s);
                    vo.providerConfig = ProviderConfig.valueOf(providerAddress, providerModules);
                } else if (s.startsWith("consumer")) {
                    var consumerModules = parseConsumerModules(s);
                    vo.consumerConfig = ConsumerConfig.valueOf(consumerModules);
                } else {
                    providerAddress = s;
                }
            }

            return vo;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getMessage(e));
            return null;
        }
    }


    private static List<ProviderModule> parseProviderModules(String str) {
        var moduleSplits = StringUtils.substringBeforeLast(
                StringUtils.substringAfterFirst(str, StringUtils.LEFT_SQUARE_BRACKET)
                , StringUtils.RIGHT_SQUARE_BRACKET).split(StringUtils.COMMA);

        var modules = Arrays.stream(moduleSplits)
                .map(it -> it.trim())
                .map(it -> it.split(StringUtils.HYPHEN))
                .map(it -> new ProviderModule(StringUtils.trim(it[1]), ProtocolManager.moduleByModuleName(StringUtils.trim(it[0]))))
                .collect(Collectors.toList());
        return modules;
    }

    private static List<ConsumerModule> parseConsumerModules(String str) {
        var moduleSplits = StringUtils.substringBeforeLast(
                StringUtils.substringAfterFirst(str, StringUtils.LEFT_SQUARE_BRACKET)
                , StringUtils.RIGHT_SQUARE_BRACKET).split(StringUtils.COMMA);

        var modules = Arrays.stream(moduleSplits)
                .map(it -> it.trim())
                .map(it -> it.split(StringUtils.HYPHEN))
                .map(it -> new ConsumerModule(StringUtils.trim(it[0]), StringUtils.trim(it[1])))
                .collect(Collectors.toList());
        return modules;
    }

    public String toProviderString() {
        return toString();
    }

    public String toConsumerString() {
        return this +
                StringUtils.SPACE + StringUtils.VERTICAL_BAR + StringUtils.SPACE +
                uuid;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(id);

        if (Objects.nonNull(providerConfig)) {
            var providerAddress = providerConfig.getAddress();
            if (StringUtils.isBlank(providerAddress)) {
                throw new RuntimeException(StringUtils.format("providerConfig的address不能为空"));
            }
            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);
            builder.append(providerAddress);

            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);
            var providerModules = providerConfig.getProviders().stream()
                    .map(it -> StringUtils.joinWith(StringUtils.HYPHEN, it.getProtocolModule().getName(), it.getProvider()))
                    .collect(Collectors.toList());
            builder.append(StringUtils.format("provider:[{}]"
                    , StringUtils.joinWith(StringUtils.COMMA + StringUtils.SPACE, providerModules.toArray())));
        }

        if (Objects.nonNull(consumerConfig)) {
            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);

            var consumerModules = consumerConfig.getConsumers().stream()
                    .map(it -> StringUtils.joinWith(StringUtils.HYPHEN, it.getConsumer(), it.getLoadBalancer()))
                    .collect(Collectors.toList());
            builder.append(StringUtils.format("consumer:[{}]"
                    , StringUtils.joinWith(StringUtils.COMMA + StringUtils.SPACE, consumerModules.toArray())));
        }

        return builder.toString();
    }


    public String getId() {
        return id;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterVO that = (RegisterVO) o;
        return Objects.equals(id, that.id) && Objects.equals(providerConfig, that.providerConfig)
                && Objects.equals(consumerConfig, that.consumerConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerConfig, consumerConfig);
    }
}
