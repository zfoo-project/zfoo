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

import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.StorageContext;
import com.zfoo.storage.model.anno.Id;
import com.zfoo.storage.model.anno.ResInjection;
import com.zfoo.storage.model.vo.Storage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Bean级生命周期接口和容器生命周期接口是个性和共性的辩证统一思想的体现，
 * 前者解决Bean个性化处理的问题，而后者解决容器中某些Bean共性处理的问题
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class ResInjectionProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (StorageContext.getStorageContext() == null) {
            return bean;
        }
        ReflectionUtils.filterFieldsInClass(bean.getClass(), null, new Consumer<Field>() {
            @Override
            public void accept(Field field) {
                ResInjection anno = field.getAnnotation(ResInjection.class);
                if (anno != null) {
                    injectStorage(bean, field, anno);
                }
            }
        });

        return bean;
    }

    private void injectStorage(Object bean, Field field, ResInjection anno) {
        Type type = field.getGenericType();

        if (!(type instanceof ParameterizedType)) {
            throw new RuntimeException(StringUtils.format("[bean:{}]类型声明不正确，不是泛型类", bean.getClass().getSimpleName()));
        }

        Type[] types = ((ParameterizedType) type).getActualTypeArguments();

        // @ResInjection
        // Storage<Integer, ActivityResource> resources;
        Class<?> keyClazz = (Class<?>) types[0];

        Class<?> resourceClazz = (Class<?>) types[1];

        Storage<?, ?> storage = StorageContext.getStorageManager().getStorage(resourceClazz);

        if (storage == null) {
            throw new RuntimeException(StringUtils.format("静态类资源[resource:{}]不存在", resourceClazz.getSimpleName()));
        }

        Field[] idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(resourceClazz, Id.class);
        if (idFields.length != 1) {
            throw new RuntimeException(StringUtils.format("静态类资源[resource:{}]配置没有注解id", resourceClazz.getSimpleName()));
        }

        if (!keyClazz.getSimpleName().toLowerCase().contains(idFields[0].getType().getSimpleName().toLowerCase())) {
            throw new RuntimeException(StringUtils.format("静态类资源[resource:{}]配置注解[id:{}]类型和泛型类型[type:{}]不匹配"
                    , resourceClazz.getSimpleName(), idFields[0].getType().getSimpleName(), keyClazz.getSimpleName()));
        }

        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, storage);
        storage.setUsable(true);
    }

}
