/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.zfoo.protocol.exception.ExceptionUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class JsonUtils {

    /**
     * 适用于任何场景下的json转换，只要在各个类方法中不调用configure方法，则MAPPER都是线程安全的
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * 使用字节码增强技术，只能转换POJO对象
     */
    private static final ObjectMapper MAPPER_TURBO = new ObjectMapper();

    static {
        //序列化
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //序列化枚举是以toString()来输出，默认false，即默认以name()来输出
        // MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);


        //反序列化
        //当反序列化有未知属性则抛异常，true打开这个设置
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        MAPPER_TURBO.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER_TURBO.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER_TURBO.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        MAPPER_TURBO.registerModule(new AfterburnerModule());
    }

    public static <T> T string2Object(String json, Class<T> clazz) {//json=json string
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为对象[class:{}]时异常[error:{}]"
                    , json, clazz, ExceptionUtils.getStackTrace(e)));
        }
    }

    public static String object2String(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将对象[object:{}]转换为json字符串时异常[error:{}]"
                    , object, e.getMessage()));
        }
    }

    /**
     * 涡轮增压，会字节码增强，慎用
     */
    public static <T> T string2ObjectTurbo(String json, Class<T> clazz) {
        try {
            return MAPPER_TURBO.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为对象[class:{}]时异常[error:{}]"
                    , json, clazz, ExceptionUtils.getStackTrace(e)));
        }
    }

    /**
     * 涡轮增压，会字节码增强，慎用
     */
    public static String object2StringTurbo(Object object) {
        try {
            return MAPPER_TURBO.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将对象[object:{}]转换为json字符串时异常[error:{}]", object, e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> string2List(String json, Class<T> clazz) {
        CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        try {
            return (List<T>) MAPPER.readValue(json, collectionType);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为list异常[error:{}]", json, e.getMessage()));
        }
    }

    //元素不可重复
    @SuppressWarnings("unchecked")
    public static <T> Set<T> string2Set(String json, Class<T> clazz) {
        CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(HashSet.class, clazz);
        try {
            return (Set<T>) MAPPER.readValue(json, collectionType);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为Set[set:{}]异常[error:{}]"
                    , json, clazz, ExceptionUtils.getStackTrace(e)));
        }
    }

    public static <C extends Collection<T>, T> C string2Collection(String json, Class<C> collectionType, Class<T> elementType) {
        try {
            CollectionType e = MAPPER.getTypeFactory().constructCollectionType(collectionType, elementType);
            return MAPPER.readValue(json, e);
        } catch (IOException e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为Collection[collection:{}]异常[error:{}]"
                    , json, collectionType, ExceptionUtils.getStackTrace(e)));
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> string2Map(String json, Class<K> kClazz, Class<V> vClazz) {
        MapType mapType = MAPPER.getTypeFactory().constructMapType(HashMap.class, kClazz, vClazz);
        try {
            return (Map<K, V>) MAPPER.readValue(json, mapType);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为map[key:{},value:{}]异常[error:{}]"
                    , json, kClazz, vClazz, ExceptionUtils.getStackTrace(e)));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] string2Array(String json, Class<T> clazz) {
        ArrayType arrayType = MAPPER.getTypeFactory().constructArrayType(clazz);
        try {
            return (T[]) MAPPER.readValue(json, arrayType);
        } catch (Exception e) {
            throw new RuntimeException(StringUtils.format("将json字符串[{}]转换为array[element:{}]异常[error:{}]"
                    , json, clazz, ExceptionUtils.getStackTrace(e)));
        }
    }

    /**
     * 在js中获取属性名为nodeName的节点
     * <p>
     * 树模型是基于流式操作
     *
     * @param json     json string
     * @param nodeName 节点名称
     * @return 节点名称为nodeName的json节点，没有返回空
     */
    public static JsonNode getNode(String json, String nodeName) {
        try {
            Queue<JsonNode> queue = new ArrayDeque<>();
            JsonNode rootNode = MAPPER.readTree(json);//将Json串以树状结构读入内存
            JsonNode resultNode;
            queue.add(rootNode);
            while (!queue.isEmpty()) {//深度优先遍历算法
                JsonNode pollNode = queue.poll();
                resultNode = pollNode.get(nodeName);
                if (resultNode != null) {
                    return resultNode;
                }
                Iterator<JsonNode> iterator = pollNode.elements();
                while (iterator.hasNext()) {//循环遍历子节点下的信息
                    JsonNode node = iterator.next();
                    queue.add(node);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为jsonTree[nodeName:{}]异常[error:{}]"
                    , json, nodeName, ExceptionUtils.getStackTrace(e)));
        }
        return null;
    }

    public static Map<String, String> getJsonMap(String json) {
        try {
            Map<String, String> jsonMap = new HashMap<>();
            Queue<JsonNode> queue = new ArrayDeque<>();
            JsonNode rootNode = MAPPER.readTree(json);//将Json串以树状结构读入内存
            JsonNode resultNode;
            queue.add(rootNode);
            while (!queue.isEmpty()) {//深度优先遍历算法
                var pollNode = queue.poll();
                var iterator = pollNode.fields();
                while (iterator.hasNext()) {//循环遍历子节点下的信息
                    var node = iterator.next();
                    var filed = node.getKey();
                    var value = node.getValue().asText();
                    jsonMap.put(filed, value);
                }
            }
            return jsonMap;
        } catch (IOException e) {
            throw new RuntimeException(StringUtils.format("将json字符串[json:{}]转换为jsonMap异常[error:{}]", json, ExceptionUtils.getStackTrace(e)));
        }
    }
}
