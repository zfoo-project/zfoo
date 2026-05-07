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
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.zfoo.protocol.exception.RunException;

import java.io.IOException;
import java.util.*;

/**
 * @author godotg
 */
public abstract class JsonUtils {

    /**
     * For all JSON conversion use cases; thread-safe as long as configure() is not called on the instance
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * Turbo-charged Jackson using bytecode enhancement
     */
    public static final ObjectMapper MAPPER_TURBO = new ObjectMapper();

    static {
        // Serialization
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Serialize enums using toString(); default is false (uses name())
        // MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);


        // Deserialization
        // Throw exception on unknown properties; set to true to enable
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        // Pretty-print output
        DefaultPrettyPrinter prettyPrinter = (DefaultPrettyPrinter) MAPPER.getSerializationConfig().getDefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(StringUtils.TAB_ASCII, FileUtils.LS);
        prettyPrinter.indentObjectsWith(indenter);

        MAPPER_TURBO.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER_TURBO.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER_TURBO.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        MAPPER_TURBO.registerModule(new AfterburnerModule());
    }

    public static <T> T string2Object(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RunException("convert json:[{}] to class:[{}] exception", json, clazz, e);
        }
    }

    // Compact output
    public static String object2String(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RunException("convert class:[{}] to json exception", object.getClass(), e);
        }
    }

    // Format / pretty-print / elegant output
    public static String object2StringPrettyPrinter(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RunException("convert class:[{}] to json exception", object.getClass(), e);
        }
    }

    /**
     * Turbo-charges JSON conversion performance via bytecode enhancement
     */
    public static <T> T string2ObjectTurbo(String json, Class<T> clazz) {
        try {
            return MAPPER_TURBO.readValue(json, clazz);
        } catch (Exception e) {
            throw new RunException("convert json:[{}] to class:[{}] exception", json, clazz, e);
        }
    }

    /**
     * Turbo-charges JSON conversion performance via bytecode enhancement
     */
    public static String object2StringTurbo(Object object) {
        try {
            return MAPPER_TURBO.writeValueAsString(object);
        } catch (Exception e) {
            throw new RunException("convert class:[{}] to json exception", object.getClass(), e);
        }
    }

    public static <T> List<T> string2List(String json, Class<T> clazz) {
        var collectionType = MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        try {
            return MAPPER.readValue(json, collectionType);
        } catch (Exception e) {
            throw new RunException("convert json:[{}] to List:[{}] exception", json, clazz, e);
        }
    }

    // Elements must be unique
    public static <T> Set<T> string2Set(String json, Class<T> clazz) {
        var collectionType = MAPPER.getTypeFactory().constructCollectionType(HashSet.class, clazz);
        try {
            return MAPPER.readValue(json, collectionType);
        } catch (Exception e) {
            throw new RunException("convert json:[{}] to Set:[{}] exception", json, clazz, e);
        }
    }

    public static <C extends Collection<T>, T> C string2Collection(String json, Class<C> collectionType, Class<T> elementType) {
        try {
            var ct = MAPPER.getTypeFactory().constructCollectionType(collectionType, elementType);
            return MAPPER.readValue(json, ct);
        } catch (IOException e) {
            throw new RunException("convert json:[{}] to Collection:[{}] exception", json, collectionType, e);
        }
    }

    public static <K, V> Map<K, V> string2Map(String json, Class<K> kClazz, Class<V> vClazz) {
        var mapType = MAPPER.getTypeFactory().constructMapType(HashMap.class, kClazz, vClazz);
        try {
            return MAPPER.readValue(json, mapType);
        } catch (Exception e) {
            throw new RunException("convert json:[{}] to Map key:[{}] value:[{}] exception", json, kClazz, vClazz, e);
        }
    }

    public static <T> T[] string2Array(String json, Class<T> clazz) {
        var arrayType = MAPPER.getTypeFactory().constructArrayType(clazz);
        try {
            return MAPPER.readValue(json, arrayType);
        } catch (Exception e) {
            throw new RunException("convert json:[{}] to Array:[{}] exception", json, clazz, e);
        }
    }

    /**
     * Get the node whose attribute name is nodeName
     * <p>
     * The tree model is based on streaming operations
     *
     * @param json     json string
     * @param nodeName node name
     * @return the JSON node with name nodeName; null if not found
     */
    public static JsonNode getNode(String json, String nodeName) {
        try {
            var queue = new ArrayDeque<JsonNode>();
            // Read JSON string into memory as a tree
            var rootNode = MAPPER.readTree(json);
            queue.add(rootNode);
            // Depth-first traversal
            while (!queue.isEmpty()) {
                var pollNode = queue.poll();
                var resultNode = pollNode.get(nodeName);
                if (resultNode != null) {
                    return resultNode;
                }
                var iterator = pollNode.elements();
                // Iterate over child nodes
                while (iterator.hasNext()) {
                    var node = iterator.next();
                    queue.add(node);
                }
            }
        } catch (IOException e) {
            throw new RunException("convert json:[{}] to JsonTree:[{}] exception", json, nodeName, e);
        }
        return null;
    }

    public static Map<String, String> getJsonMap(String json) {
        try {
            var jsonMap = new HashMap<String, String>();
            var queue = new ArrayDeque<JsonNode>();
            // Read JSON string into memory as a tree
            var rootNode = MAPPER.readTree(json);
            queue.add(rootNode);
            // Depth-first traversal
            while (!queue.isEmpty()) {
                var pollNode = queue.poll();
                var iterator = pollNode.fields();
                // Iterate over child nodes
                while (iterator.hasNext()) {
                    var node = iterator.next();
                    var field = node.getKey();
                    var value = node.getValue().toString();
                    jsonMap.put(field, value);
                }
            }
            return jsonMap;
        } catch (IOException e) {
            throw new RunException("convert json:[{}] to JsonMap exception", json, e);
        }
    }
}
