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
import com.zfoo.protocol.registration.ProtocolModule;
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
public class RegisterVO {

    private static final Logger logger = LoggerFactory.getLogger(RegisterVO.class);

    private static final String uuid = UuidUtils.getUUID();

    private String id;

    // 服务提供者配置
    private ProviderConfig providerConfig;
    // 服务消费者配置
    private ConsumerConfig consumerConfig;

    public static boolean providerHasConsumer(RegisterVO providerVO, RegisterVO consumerVO) {
        if (Objects.isNull(providerVO) || Objects.isNull(providerVO.providerConfig) || CollectionUtils.isEmpty(providerVO.providerConfig.getProviders())
                || Objects.isNull(consumerVO) || Objects.isNull(consumerVO.consumerConfig) || CollectionUtils.isEmpty(consumerVO.consumerConfig.getConsumers())) {
            return false;
        }
        for (var provider : providerVO.getProviderConfig().getProviders()) {
            if (consumerVO.getConsumerConfig().getConsumers().stream().anyMatch(it -> it.matchProvider(provider))) {
                return true;
            }
        }
        return false;
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

            for (var i = 1; i < splits.length; i++) {
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
                .map(it -> new ProviderModule(new ProtocolModule(Byte.parseByte(it[0]), it[1]), it[2]))
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
                .map(it -> new ConsumerModule(new ProtocolModule(Byte.parseByte(it[0]), it[1]), it[2], it[3]))
                .toList();
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

        // 模块模块名
        builder.append(id);

        // 服务提供者相关配置信息
        if (Objects.nonNull(providerConfig)) {
            var providerAddress = providerConfig.getAddress();
            if (StringUtils.isBlank(providerAddress)) {
                throw new RuntimeException(StringUtils.format("providerConfig的address不能为空"));
            }
            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);
            // 服务提供者地址
            builder.append(providerAddress);

            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);
            var providerModules = providerConfig.getProviders().stream()
                    .map(it -> StringUtils.joinWith(StringUtils.HYPHEN, it.getProtocolModule().getId(), it.getProtocolModule().getName(), it.getProvider()))
                    .toList();

            // 服务提供者模块信息列表
            builder.append(StringUtils.format("provider:[{}]"
                    , StringUtils.joinWith(StringUtils.COMMA + StringUtils.SPACE, providerModules.toArray())));
        }

        // 服务消费者相关信息
        if (Objects.nonNull(consumerConfig)) {
            builder.append(StringUtils.SPACE).append(StringUtils.VERTICAL_BAR).append(StringUtils.SPACE);

            var consumerModules = consumerConfig.getConsumers().stream()
                    .map(it -> StringUtils.joinWith(StringUtils.HYPHEN, it.getProtocolModule().getId(), it.getProtocolModule().getName(), it.getLoadBalancer(), it.getConsumer()))
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
        RegisterVO that = (RegisterVO) o;
        return Objects.equals(id, that.id) && Objects.equals(providerConfig, that.providerConfig)
                && Objects.equals(consumerConfig, that.consumerConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerConfig, consumerConfig);
    }
}
