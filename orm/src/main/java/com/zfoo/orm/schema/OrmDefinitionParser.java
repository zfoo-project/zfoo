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

package com.zfoo.orm.schema;

import com.zfoo.orm.OrmContext;
import com.zfoo.orm.accessor.MongodbAccessor;
import com.zfoo.orm.config.CacheStrategy;
import com.zfoo.orm.config.HostConfig;
import com.zfoo.orm.config.OrmConfig;
import com.zfoo.orm.config.PersisterStrategy;
import com.zfoo.orm.manager.OrmManager;
import com.zfoo.orm.query.MongodbQuery;
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
public class OrmDefinitionParser implements BeanDefinitionParser {

    @Override
    public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {

        Class<?> clazz;
        String name;
        BeanDefinitionBuilder builder;

        // 解析OrmConfig的配置
        parseOrmConfig(element, parserContext);

        // 注册OrmContext
        clazz = OrmContext.class;
        name = StringUtils.uncapitalize(clazz.getSimpleName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        // 注册OrmManager
        clazz = OrmManager.class;
        name = StringUtils.uncapitalize(clazz.getSimpleName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        builder.addPropertyReference("ormConfig", OrmConfig.class.getCanonicalName());
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        // 注册MongodbAccessor
        clazz = MongodbAccessor.class;
        name = StringUtils.uncapitalize(clazz.getSimpleName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        // 注册MongodbQuery
        clazz = MongodbQuery.class;
        name = StringUtils.uncapitalize(clazz.getSimpleName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }

    private void parseOrmConfig(Element element, ParserContext parserContext) {
        var clazz = OrmConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("id", "id", builder, element, parserContext);
        resolvePlaceholder("entity-package", "entityPackage", builder, element, parserContext);

        parseHostConfig(DomUtils.getFirstChildElementByTagName(element, "host"), parserContext);
        builder.addPropertyReference("host", HostConfig.class.getCanonicalName());

        // 解析caches标签
        var caches = parseCacheStrategies(DomUtils.getFirstChildElementByTagName(element, "caches"), parserContext);
        builder.addPropertyValue("caches", caches);

        // 解析persisters标签
        var persisters = parsePersisterStrategies(DomUtils.getFirstChildElementByTagName(element, "persisters"), parserContext);
        builder.addPropertyValue("persisters", persisters);

        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }


    private void parseHostConfig(Element element, ParserContext parserContext) {
        // 解析host标签
        var clazz = HostConfig.class;
        var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        resolvePlaceholder("database", "database", builder, element, parserContext);
        resolvePlaceholder("user", "user", builder, element, parserContext);
        resolvePlaceholder("password", "password", builder, element, parserContext);

        var addressMap = parseAddress(element, parserContext);
        builder.addPropertyValue("address", addressMap);

        parserContext.getRegistry().registerBeanDefinition(clazz.getCanonicalName(), builder.getBeanDefinition());
    }

    private ManagedList<BeanDefinitionHolder> parseCacheStrategies(Element element, ParserContext parserContext) {
        var cacheStrategiesElementList = DomUtils.getChildElementsByTagName(element, "cache");
        var cacheStrategies = new ManagedList<BeanDefinitionHolder>();
        var environment = parserContext.getReaderContext().getEnvironment();
        for (var i = 0; i < cacheStrategiesElementList.size(); i++) {
            var addressElement = cacheStrategiesElementList.get(i);
            var clazz = CacheStrategy.class;
            var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("strategy")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("size")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("expire-millisecond")));

            cacheStrategies.add(new BeanDefinitionHolder(builder.getBeanDefinition(), StringUtils.format("{}.{}", clazz.getCanonicalName(), i)));
        }
        return cacheStrategies;
    }

    private ManagedList<BeanDefinitionHolder> parsePersisterStrategies(Element element, ParserContext parserContext) {
        var persisterStrategiesElementList = DomUtils.getChildElementsByTagName(element, "persister");
        var persisterStrategies = new ManagedList<BeanDefinitionHolder>();
        var environment = parserContext.getReaderContext().getEnvironment();
        for (var i = 0; i < persisterStrategiesElementList.size(); i++) {
            var addressElement = persisterStrategiesElementList.get(i);
            var clazz = PersisterStrategy.class;
            var builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);

            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("strategy")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("type")));
            builder.addConstructorArgValue(environment.resolvePlaceholders(addressElement.getAttribute("config")));

            persisterStrategies.add(new BeanDefinitionHolder(builder.getBeanDefinition(), StringUtils.format("{}.{}", clazz.getCanonicalName(), i)));
        }
        return persisterStrategies;
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
