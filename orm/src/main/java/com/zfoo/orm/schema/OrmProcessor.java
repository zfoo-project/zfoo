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
import com.zfoo.orm.model.anno.EntityCachesInjection;
import com.zfoo.orm.model.cache.IEntityCaches;
import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * FactoryBean，在某些情况下，实例化Bean非常复杂，如果按照传统的方式，则需要在bean标签中配置大量的信息，
 * 配置方式的灵活性是受到限制的，这时采用编码的方式可能会获得一个简单的方案
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class OrmProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (OrmContext.getOrmContext() == null) {
            return bean;
        }

        ReflectionUtils.filterFieldsInClass(bean.getClass()
                , new Predicate<Field>() {
                    @Override
                    public boolean test(Field field) {
                        if (!field.isAnnotationPresent(EntityCachesInjection.class)) {
                            return false;
                        }
                        return true;
                    }
                }
                , new Consumer<Field>() {
                    @Override
                    public void accept(Field field) {
                        Type type = field.getGenericType();

                        if (!(type instanceof ParameterizedType)) {
                            throw new RuntimeException(StringUtils.format("变量[{}]的类型不是泛型类", field.getName()));
                        }

                        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                        Class<? extends IEntity<?>> clazz = (Class<? extends IEntity<?>>) types[1];
                        IEntityCaches<?, ?> entityCaches = OrmContext.getOrmManager().getEntityCaches(clazz);

                        if (entityCaches == null) {
                            throw new RuntimeException(StringUtils.format("实体缓存对象[entityCaches:{}]不存在", clazz));
                        }

                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, bean, entityCaches);
                        entityCaches.setUsable(true);
                    }
                });

        return bean;
    }
}
