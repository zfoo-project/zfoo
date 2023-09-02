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

package com.zfoo.storage.schema;

import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.StorageContext;
import com.zfoo.storage.config.StorageConfig;
import com.zfoo.storage.manager.StorageManager;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author godotg
 * @version 3.0
 */
public class StorageDefinitionParser implements BeanDefinitionParser {

    // 配置标签名称
    public static final String STORAGE = "storage";


    @Override
    public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
        // 解析StorageConfig的配置
        parseStorageConfig(element, parserContext);

        // 注册StorageContext
        registerBeanDefinition(parserContext);

        // 注册StorageManager
        var clazz = StorageManager.class;
        var name = StringUtils.uncapitalize(clazz.getName());
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        builder.addPropertyReference("storageConfig", StorageConfig.class.getCanonicalName());
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }

    private void parseStorageConfig(Element element, ParserContext parserContext) {
        var clazz = StorageConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        var scanElement = DomUtils.getFirstChildElementByTagName(element, "scan");
        if (scanElement == null) {
            throw new RuntimeException("The XML file is missing a [scan] element definition");
        }
        var resourceElement = DomUtils.getFirstChildElementByTagName(element, "resource");
        if (resourceElement == null) {
            throw new RuntimeException("The XML file is missing a [resource] element definition");
        }

        resolvePlaceholder("id", "id", builder, element, parserContext);
        resolvePlaceholder("package", "scanPackage", builder, scanElement, parserContext);
        resolvePlaceholder("writeable", "writeable", builder, scanElement, parserContext);
        resolvePlaceholder("recycle", "recycle", builder, scanElement, parserContext);
        resolvePlaceholder("location", "resourceLocation", builder, resourceElement, parserContext);

        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private void registerBeanDefinition(ParserContext parserContext) {
        var registry = parserContext.getRegistry();

        Class<?> clazz;
        String name;
        BeanDefinitionBuilder builder;

        // 注册StorageContext
        clazz = StorageContext.class;
        name = StringUtils.uncapitalize(clazz.getName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        registry.registerBeanDefinition(name, builder.getBeanDefinition());
    }

    private void resolvePlaceholder(String attributeName, String fieldName, BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
        var attributeValue = element.getAttribute(attributeName);
        var environment = parserContext.getReaderContext().getEnvironment();
        var placeholder = environment.resolvePlaceholders(attributeValue);
        builder.addPropertyValue(fieldName, placeholder);
    }
}
