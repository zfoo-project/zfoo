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

package com.zfoo.scheduler.schema;

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.SchedulerContext;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Analysis of when SchedulerContext is injected into the Spring container:
 * When running the test project (com.zfoo.scheduler.ApplicationTest), breakpoints reveal that
 * during the parsing of application.xml, the custom parsing is triggered inside
 * DefaultBeanDefinitionDocumentReader#parseBeanDefinitions when the "scheduler" XML element is encountered.
 * Since it is recognized as a custom element (determined by whether it belongs to the Spring namespace),
 * parseCustomElement is called to handle the custom tag, which in turn invokes the parse() method
 * of the BeanDefinitionParser implementation, registering SchedulerContext as a bean in the Spring container.
 * <p>
 * Conclusion: In a SpringBoot project based on zfoo, XML files are not parsed at all;
 * beans are registered via @Configuration classes instead.
 * Therefore, these BeanDefinitionParser#parse() implementations are never executed.
 *
 * @author godotg
 */
public class SchedulerDefinitionParser implements BeanDefinitionParser {

    private final String SCHEDULER_ID = "id";

    @Override
    public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
        Class<?> clazz;
        String name;
        BeanDefinitionBuilder builder;

        // Register SchedulerContext
        clazz = SchedulerContext.class;
        name = StringUtils.uncapitalize(clazz.getName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }

}
