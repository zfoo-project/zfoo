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
 */
public class NetDefinitionParser implements BeanDefinitionParser {

    @Override
    public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
        Class<?> clazz;
        String name;
        BeanDefinitionBuilder builder;

        // Parse the NetConfig configuration
        parseNetConfig(element, parserContext);

        // Register NetContext
        clazz = NetContext.class;
        name = StringUtils.uncapitalize(clazz.getName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        // Register ConfigManager and wire in the NetConfig bean as its localConfig property
        clazz = ConfigManager.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        builder.addPropertyReference("localConfig", NetConfig.class.getCanonicalName());
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // Register PacketService
        clazz = PacketService.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // Register Router
        clazz = Router.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // Register Consumer
        clazz = Consumer.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        // Register SessionManager
        // serverSessionMap: sessions of clients connected to this node (server role)
        // clientSessionMap: sessions this node established to remote providers (client role)
        clazz = SessionManager.class;
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }


    /**
     * Parse the &lt;config&gt; element and register NetConfig as a Spring bean.
     *
     * @param element       the XML element to parse
     * @param parserContext the Spring parser context
     */
    private void parseNetConfig(Element element, ParserContext parserContext) {
        // The class to register; parse each custom XML attribute and map it to a NetConfig field
        var clazz = NetConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        // ----- config attribute parsing -----
        // Parse the node/application id
        resolvePlaceholder("id", "id", builder, element, parserContext);

        // Location of protocol.xml; writing just "protocol.xml" reads from the resources directory
        resolvePlaceholder("protocol-location", "protocolLocation", builder, element, parserContext);

        // Whether all protocols are merged into a single file
        resolvePlaceholder("merge-protocol", "mergeProtocol", builder, element, parserContext);
        // Whether the protocol file is folded/collapsed
        resolvePlaceholder("fold-protocol", "foldProtocol", builder, element, parserContext);
        // List of languages for which protocol code should be generated
        resolvePlaceholder("code-languages", "codeLanguages", builder, element, parserContext);

        resolvePlaceholder("protocol-path", "protocolPath", builder, element, parserContext);

        resolvePlaceholder("protocol-param", "protocolParam", builder, element, parserContext);

        // ----- registry element parsing (all above were <config> attributes) -----
        var registryElement = DomUtils.getFirstChildElementByTagName(element, "registry");
        if (registryElement != null) {
            parseRegistryConfig(registryElement, parserContext);
            builder.addPropertyReference("registry", RegistryConfig.class.getCanonicalName());
        }

        // ----- monitor element parsing -----
        var monitorElement = DomUtils.getFirstChildElementByTagName(element, "monitor");
        if (monitorElement != null) {
            parseMonitorConfig(monitorElement, parserContext);
            builder.addPropertyReference("monitor", MonitorConfig.class.getCanonicalName());
        }

        // ----- provider element parsing -----
        var providerElement = DomUtils.getFirstChildElementByTagName(element, "providers");
        if (providerElement != null) {
            parseProviderConfig(providerElement, parserContext);
            builder.addPropertyReference("provider", ProviderConfig.class.getCanonicalName());
        }

        // ----- consumer element parsing -----
        var consumerElement = DomUtils.getFirstChildElementByTagName(element, "consumers");
        if (consumerElement != null) {
            parseConsumerConfig(consumerElement, parserContext);
            builder.addPropertyReference("consumer", ConsumerConfig.class.getCanonicalName());
        }

        // Register NetConfig as a Spring bean
        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void parseRegistryConfig(Element element, ParserContext parserContext) {
        var clazz = RegistryConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("center", "center", builder, element, parserContext);
        resolvePlaceholder("path", "path", builder, element, parserContext);
        resolvePlaceholder("user", "user", builder, element, parserContext);
        resolvePlaceholder("password", "password", builder, element, parserContext);
        resolvePlaceholder("driver-class-name", "driverClassName", builder, element, parserContext);
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

        // Register ProviderConfig as a Spring bean
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
