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

package com.zfoo.net.schema;

import com.zfoo.net.NetContext;
import com.zfoo.net.config.ConfigManager;
import com.zfoo.net.config.model.*;
import com.zfoo.net.consumer.Consumer;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.router.Router;
import com.zfoo.net.session.SessionManager;
import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author godotg
 * @version 3.0
 */
public class NetDefinitionParser implements BeanDefinitionParser {

    @Override
    public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
        Class<?> clazz;
        String name;
        BeanDefinitionBuilder builder;

        // 解析NetConfig的配置
        parseNetConfig(element, parserContext);

        // 注册NetContext
        clazz = NetContext.class;
        name = StringUtils.uncapitalize(clazz.getName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        // 注册ConfigManager
        clazz = ConfigManager.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        // 把Spring容器中的NetConfig引用赋值给ConfigManager中的localConfig属性
        builder.addPropertyReference("localConfig", NetConfig.class.getCanonicalName());
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // 注册PacketService
        clazz = PacketService.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // 注册Router
        clazz = Router.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // 注册Consumer
        clazz = Consumer.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // 注册SessionManager
        // 里面的serverSessionMap 和 clientSessionMap 是根据自己是客户端还是服务器保存连接自己 和 自己连接 的网络节点信息
        clazz = SessionManager.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }


    /**
     * 解析config标签，并且注册NetConfig类到Spring容器
     *
     * @param element
     * @param parserContext
     */
    private void parseNetConfig(Element element, ParserContext parserContext) {
        // 要注册到Spring的类，下面都是进行解析自定义标签，把值赋值给NetConfig的属性
        var clazz = NetConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        // -----config属性解析-----
        // 解析id
        resolvePlaceholder("id", "id", builder, element, parserContext);

        // 协议protocol.xml文件的位置。 注意：直接写protocol.xml 则是从resources目录下读
        resolvePlaceholder("protocol-location", "protocolLocation", builder, element, parserContext);

        // 文件是否折叠
        resolvePlaceholder("fold-protocol", "foldProtocol", builder, element, parserContext);
        // 生成各种语言的协议列表
        resolvePlaceholder("code-languages", "codeLanguages", builder, element, parserContext);

        resolvePlaceholder("protocol-path", "protocolPath", builder, element, parserContext);

        resolvePlaceholder("protocol-param", "protocolParam", builder, element, parserContext);

        // -----注册中心解析-----
        // 上面解析的都是config标签的属性，这里开始解析registry元素
        var registryElement = DomUtils.getFirstChildElementByTagName(element, "registry");
        if (registryElement != null) {
            parseRegistryConfig(registryElement, parserContext);
            builder.addPropertyReference("registry", RegistryConfig.class.getCanonicalName());
        }

        // ----- monitor解析-----
        var monitorElement = DomUtils.getFirstChildElementByTagName(element, "monitor");
        if (monitorElement != null) {
            parseMonitorConfig(monitorElement, parserContext);
            builder.addPropertyReference("monitor", MonitorConfig.class.getCanonicalName());
        }

        // -----服务器生产者解析-----
        var providerElement = DomUtils.getFirstChildElementByTagName(element, "providers");
        if (providerElement != null) {
            parseProviderConfig(providerElement, parserContext);
            builder.addPropertyReference("provider", ProviderConfig.class.getCanonicalName());
        }

        var consumerElement = DomUtils.getFirstChildElementByTagName(element, "consumers");
        if (consumerElement != null) {
            parseConsumerConfig(consumerElement, parserContext);
            builder.addPropertyReference("consumer", ConsumerConfig.class.getCanonicalName());
        }

        // 注册NetConfig到Spring容器中
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void parseRegistryConfig(Element element, ParserContext parserContext) {
        var clazz = RegistryConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("center", "center", builder, element, parserContext);
        resolvePlaceholder("user", "user", builder, element, parserContext);
        resolvePlaceholder("password", "password", builder, element, parserContext);
        var addressMap = parseAddress(element, parserContext);
        builder.addPropertyValue("address", addressMap);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void parseMonitorConfig(Element element, ParserContext parserContext) {
        var clazz = MonitorConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("center", "center", builder, element, parserContext);
        resolvePlaceholder("user", "user", builder, element, parserContext);
        resolvePlaceholder("password", "password", builder, element, parserContext);
        var addressMap = parseAddress(element, parserContext);
        builder.addPropertyValue("address", addressMap);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void parseProviderConfig(Element element, ParserContext parserContext) {
        var clazz = ProviderConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("thread", "thread", builder, element, parserContext);
        resolvePlaceholder("address", "address", builder, element, parserContext);

        var providerModules = parseProviderModules("providers", element, parserContext);
        builder.addPropertyValue("providers", providerModules);

        // 注册Consumer到Spring容器中
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void parseConsumerConfig(Element element, ParserContext parserContext) {
        var clazz = ConsumerConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        var consumerModules = parseConsumerModules("consumers", element, parserContext);
        builder.addPropertyValue("consumers", consumerModules);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }


    private ManagedList<BeanDefinitionHolder> parseProviderModules(String param, Element element, ParserContext parserContext) {
        var moduleElementList = DomUtils.getChildElementsByTagName(element, "provider");
        var providers = new ManagedList<BeanDefinitionHolder>();
        var environment = parserContext.getReaderContext().getEnvironment();
        for (var i = 0; i < moduleElementList.size(); i++) {
            var addressElement = moduleElementList.get(i);
            var clazz = ProviderModule.class;
            var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("protocol-module")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("provider")));

            providers.add(new BeanDefinitionHolder(builder.getBeanDefinition(), StringUtils.format("{}.{}{}", clazz.getCanonicalName(), param, i)));
        }
        return providers;
    }

    private ManagedList<BeanDefinitionHolder> parseConsumerModules(String param, Element element, ParserContext parserContext) {
        var moduleElementList = DomUtils.getChildElementsByTagName(element, "consumer");
        var modules = new ManagedList<BeanDefinitionHolder>();
        var environment = parserContext.getReaderContext().getEnvironment();
        for (var i = 0; i < moduleElementList.size(); i++) {
            var addressElement = moduleElementList.get(i);
            var clazz = ConsumerModule.class;
            var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("protocol-module")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("load-balancer")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("consumer")));

            modules.add(new BeanDefinitionHolder(builder.getBeanDefinition(), StringUtils.format("{}.{}{}", clazz.getCanonicalName(), param, i)));
        }
        return modules;
    }

    private ManagedMap<String, String> parseAddress(Element element, ParserContext parserContext) {
        var addressElementList = DomUtils.getChildElementsByTagName(element, "address");
        var addressMap = new ManagedMap<String, String>();

        for (var addressElement : addressElementList) {
            var name = addressElement.getAttribute("name");
            var urlAttribute = addressElement.getAttribute("url");
            var url = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(urlAttribute);
            addressMap.put(name, url);
        }
        return addressMap;
    }

    private void resolvePlaceholder(String attributeName, String fieldName, BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        var attributeValue = element.getAttribute(attributeName);
        var environment = parserContext.getReaderContext().getEnvironment();
        var placeholder = environment.resolvePlaceholders(attributeValue);
        builder.addPropertyValue(fieldName, placeholder);
    }
}
