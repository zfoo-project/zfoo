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
import com.zfoo.net.config.manager.ConfigManager;
import com.zfoo.net.config.model.*;
import com.zfoo.net.consumer.Consumer;
import com.zfoo.net.packet.service.PacketService;
import com.zfoo.net.router.Router;
import com.zfoo.net.session.manager.SessionManager;
import com.zfoo.protocol.registration.ProtocolModule;
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
 * @author jaysunxiao
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
        clazz = SessionManager.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }


    private void parseNetConfig(Element element, ParserContext parserContext) {
        var clazz = NetConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("id", "id", builder, element, parserContext);
        resolvePlaceholder("protocol-location", "protocolLocation", builder, element, parserContext);
        resolvePlaceholder("javascript-protocol", "javascriptProtocol", builder, element, parserContext);
        resolvePlaceholder("typescript-protocol", "typescriptProtocol", builder, element, parserContext);
        resolvePlaceholder("csharp-protocol", "csharpProtocol", builder, element, parserContext);
        resolvePlaceholder("lua-protocol", "luaProtocol", builder, element, parserContext);
        resolvePlaceholder("gdscript-protocol", "gdscriptProtocol", builder, element, parserContext);
        resolvePlaceholder("cpp-protocol", "cppProtocol", builder, element, parserContext);
        resolvePlaceholder("protobuf-protocol", "protobufProtocol", builder, element, parserContext);
        resolvePlaceholder("fold-protocol", "foldProtocol", builder, element, parserContext);
        resolvePlaceholder("protocol-path", "protocolPath", builder, element, parserContext);
        resolvePlaceholder("protocol-param", "protocolParam", builder, element, parserContext);

        var registryElement = DomUtils.getFirstChildElementByTagName(element, "registry");
        if (registryElement != null) {
            parseRegistryConfig(registryElement, parserContext);
            builder.addPropertyReference("registry", RegistryConfig.class.getCanonicalName());
        }

        var monitorElement = DomUtils.getFirstChildElementByTagName(element, "monitor");
        if (monitorElement != null) {
            parseMonitorConfig(monitorElement, parserContext);
            builder.addPropertyReference("monitor", MonitorConfig.class.getCanonicalName());
        }

        var providerElement = DomUtils.getFirstChildElementByTagName(element, "provider");
        if (providerElement != null) {
            builder.addPropertyReference("provider", ProviderConfig.class.getCanonicalName());
            parseProviderConfig(providerElement, parserContext);
        }

        var consumerElement = DomUtils.getFirstChildElementByTagName(element, "consumer");
        if (consumerElement != null) {
            parseConsumerConfig(consumerElement, parserContext);
            builder.addPropertyReference("consumer", ConsumerConfig.class.getCanonicalName());
        }

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

        resolvePlaceholder("task-dispatch", "taskDispatch", builder, element, parserContext);
        resolvePlaceholder("thread", "thread", builder, element, parserContext);
        resolvePlaceholder("address", "address", builder, element, parserContext);

        var providerModules = parseModules("provider", element, parserContext);
        builder.addPropertyValue("modules", providerModules);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void parseConsumerConfig(Element element, ParserContext parserContext) {
        var clazz = ConsumerConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        var consumerModules = parseModules("consumer", element, parserContext);
        builder.addPropertyValue("modules", consumerModules);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private ManagedList<BeanDefinitionHolder> parseModules(String param, Element element, ParserContext parserContext) {
        var moduleElementList = DomUtils.getChildElementsByTagName(element, "module");
        var modules = new ManagedList<BeanDefinitionHolder>();
        var environment = parserContext.getReaderContext().getEnvironment();
        for (var i = 0; i < moduleElementList.size(); i++) {
            var addressElement = moduleElementList.get(i);
            var clazz = ProtocolModule.class;
            var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("name")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("load-balancer")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("group")));

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
