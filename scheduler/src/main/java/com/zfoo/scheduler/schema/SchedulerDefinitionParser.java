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
 * SchedulerContext注入到Spring容器中的时机分析：
 * 首先运行测试工程：com.zfoo.scheduler.ApplicationTest
 * 打断点可以发现，在解析application.xml时，这种自定义的解析是在DefaultBeanDefinitionDocumentReader.java类的parseBeanDefinitions中解析xml的"scheduler元素"时
 * 发现是自定义元素(是否是spring的命名空间为依据作为判断)，就会调用 parseCustomElement解析自定义标签从而调用到实现BeanDefinitionParser接口的parse方法，
 * 从而接着走自己的逻辑在spring容器中注入SchedulerContext这个bean对象。
 * <p>
 * 从而可以得出结论：在基于zfoo的SpringBoot工程中，由于压根不解析xml对象，注册bean也是通过@Configuration配置类注册bean的，
 * 因此这些实现BeanDefinitionParser接口的parse方法是都不会执行的。
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

        // 注册SchedulerContext
        clazz = SchedulerContext.class;
        name = StringUtils.uncapitalize(clazz.getName());
        builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());

        return builder.getBeanDefinition();
    }

}
