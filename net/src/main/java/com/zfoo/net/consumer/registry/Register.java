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
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author godotg
 */
public class Register {

    private static final Logger logger = LoggerFactory.getLogger(Register.class);

    private static final String LOCAL_UUID = UuidUtils.getUUID();

    private String id;

    // 服务提供者配置
    private ProviderConfig providerConfig;
    // 服务消费者配置
    private ConsumerConfig consumerConfig;

    public static boolean providerHasConsumer(Register providerRegister, Register consumerRegister) {
        if (Objects.isNull(providerRegister) || Objects.isNull(providerRegister.providerConfig) || CollectionUtils.isEmpty(providerRegister.providerConfig.getProviders())
                || Objects.isNull(consumerRegister) || Objects.isNull(consumerRegister.consumerConfig) || CollectionUtils.isEmpty(consumerRegister.consumerConfig.getConsumers())) {
            return false;
        }
        for (var provider : providerRegister.getProviderConfig().getProviders()) {
            if (consumerRegister.getConsumerConfig().getConsumers().stream().anyMatch(it -> it.getConsumer().equals(provider.getProvider()))) {
                return true;
            }
        }
        return false;
    }

    public static Register valueOf(String id, ProviderConfig providerConfig, ConsumerConfig consumerConfig) {
        Register register = new Register();
        register.id = id;
        register.providerConfig = providerConfig;
        register.consumerConfig = consumerConfig;
        return register;
    }

    @Nullable
    public static Register parseString(String str) {
        try {
            var register = new Register();
            var splits = str.split("\\|");

            register.id = splits[0].trim();

            String providerAddress = null;

            for (var i = 1; i < splits.length; i++) {
                var s = splits[i].trim();
                if (s.startsWith("provider")) {
                    var providerModules = parseProviderModules(s);
                    register.providerConfig = ProviderConfig.valueOf(providerAddress, providerModules);
                } else if (s.startsWith("consumer")) {
                    var consumerModules = parseConsumerModules(s);
                    register.consumerConfig = ConsumerConfig.valueOf(consumerModules);
                } else {
                    providerAddress = s;
                }
            }

            return register;
        } catch (Exception e) {
            logger.error("parse register error", e);
            return null;
        }
    }


    private static List<ProviderModule> parseProviderModules(String str) {
        str = StringUtils.substringAfterFirst(str, StringUtils.LEFT_SQUARE_BRACKET);
        str = StringUtils.substringBeforeLast(str, StringUtils.RIGHT_SQUARE_BRACKET);
        var moduleSplits = str.split(StringUtils.COMMA);
        var modules = Arrays.stream(moduleSplits)
                .map(it -> it.trim())
                .map(it -> it.split(StringUtils.HYPHEN))
                .map(it -> new ProviderModule(it[0], it[1]))
                .toList();
        return modules;
    }

    private static List<ConsumerModule> parseConsumerModules(String str) {
        var moduleSplits = StringUtils.substringBeforeLast(
                StringUtils.substringAfterFirst(str, StringUtils.LEFT_SQUARE_BRACKET)
                , StringUtils.RIGHT_SQUARE_BRACKET).split(StringUtils.COMMA);

        var modules = Arrays.stream(moduleSplits)
                .map(it -> it.trim())
                .map(it -> it.split(StringUtils.HYPHEN))
                .map(it -> new ConsumerModule(it[0], it[1]))
                .toList();
        return modules;
    }

    public String toProviderString() {
        return toString();
    }

    public String toConsumerString() {
        return this + StringUtils.SPACE + StringUtils.VERTICAL_BAR + StringUtils.SPACE + LOCAL_UUID;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();

        // 模块模块名
        builder.append(id);

        // 服务提供者相关配置信息
        if (Objects.nonNull(providerConfig)) {
            var providerAddress = providerConfig.getAddress();
            if (StringUtils.isBlank(providerAddress)) {
                throw new RuntimeException(StringUtils.format("The address of provider Config cannot be empty"));
            }
            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);
            // 服务提供者地址
            builder.append(providerAddress);

            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);
            var providerModules = providerConfig.getProviders().stream()
                    .map(it -> StringUtils.joinWith(StringUtils.HYPHEN, it.getProtocolModule(), it.getProvider()))
                    .toList();

            // 服务提供者模块信息列表
            builder.append(StringUtils.format("provider:[{}]"
                    , StringUtils.joinWith(StringUtils.COMMA + StringUtils.SPACE, providerModules.toArray())));
        }

        // 服务消费者相关信息
        if (Objects.nonNull(consumerConfig)) {
            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);

            var consumerModules = consumerConfig.getConsumers().stream()
                    .map(it -> StringUtils.joinWith(StringUtils.HYPHEN, it.getLoadBalancer(), it.getConsumer()))
                    .toList();

            // 服务消费者模块信息列表
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
        Register that = (Register) o;
        return Objects.equals(id, that.id) && Objects.equals(providerConfig, that.providerConfig)
                && Objects.equals(consumerConfig, that.consumerConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerConfig, consumerConfig);
    }
}
