package com.zfoo.storage.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.utils.json.ArrayValue;
import com.zfoo.storage.utils.json.ObjectValue;
import com.zfoo.storage.utils.json.Value;
import org.springframework.lang.NonNull;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;


/**
 * JSON 工具
 */
public final class JACKSONUtils {
    /**
     * 将对象转化为 JSON 字符串
     *
     * @param object .
     * @return .
     */
    public static String jsonString(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return JSON_MAPPER.writeValueAsString(object);
        } catch (JacksonException e) {
            String message = Text.format("对象序列化为 JSON 字符串失败，reason: {}, class: {}, object: {}",
                                         e.getOriginalMessage(), object.getClass(), object);
            throw new IllegalArgumentException(message);
        }
    }

    public static byte[] jsonBytes(@NonNull Object object) {
        Objects.requireNonNull(object);
        try {
            return JSON_MAPPER.writeValueAsBytes(object);
        } catch (JacksonException e) {
            String message = Text.format("对象序列化为 JSON 字节组失败，reason: {}, class: {}, object: {}",
                                         e.getOriginalMessage(), object.getClass(), object);
            throw new IllegalArgumentException(message);
        }
    }

    public static ObjectValue parseObject(@NonNull String json) {
        return (ObjectValue) parse(json);
    }

    public static ArrayValue parseArray(@NonNull String json) {
        return (ArrayValue) parse(json);
    }

    public static Value parse(@NonNull String json) {
        return parse(json, Value.class);
    }

    public static <T> T parse(@NonNull String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException(Text.format("JSON.解析失败，参数不允许为空, json: {}, type: {}",
                                                           json,
                                                           type));
        } else {
            return parse(() -> JSON_MAPPER.createParser(json), json, type);
        }
    }

    public static <T> T parse(File file, Class<T> type) {
        if (!file.exists()) {
            throw new IllegalArgumentException(Text.format("JSON.解析失败，文件不存在, json: {}, type: {}",
                                                           file.getPath(),
                                                           type));
        } else {
            return parse(() -> JSON_MAPPER.createParser(file), file.getPath(), type);
        }
    }

    public static <T> T parse(URL url, Class<T> type) {
        return parse(() -> JSON_MAPPER.createParser(url), url.getPath(), type);
    }

    private static <T> T parse(Callable<JsonParser> createParser, String source, Class<T> type) {
        try (JsonParser parser = createParser.call()) {
            T value = parser.readValueAs(type);
            JsonToken token = parser.nextToken();   // 调用 nextToken 以触发 close
            if (parser.isClosed()) return value;    // json 彻底解析完毕，正常返回
            // json 中还有残留 token，报错
            com.fasterxml.jackson.core.TreeNode remain = parser.readValueAsTree();
            throw new JsonParseException(parser, Text.format("Unexpected token, value: {}, token: {}, remain: {}",
                                                             value, token, remain));
        } catch (Exception e) {
            throw new IllegalArgumentException(Text.format("JSON.解析失败，reason: {}, source: {}, type: {}",
                                                           e.getLocalizedMessage(), source, type));
        }
    }

    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return JSON_MAPPER.convertValue(fromValue, toValueType);
    }

    //#region 内部方法
    private static final Function<com.fasterxml.jackson.core.JsonFactory, ObjectMapper> MAPPER_FACTORY =
            jsonFactory -> new ObjectMapper(
                    jsonFactory)
                    // .enable(SerializationFeature.INDENT_OUTPUT)                                 // 美化输出
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)                          // 允许序列化空的POJO类（否则会抛出异常）
                    // .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)                    // 把 java.util.Date,
                    // Calendar输出为数字（时间戳）
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)                 //  在遇到未知属性的时候不抛出异常
                    // .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)          // 强制JSON 空字符串("")
                    // 转换为null对象值:
                    .enable(JsonParser.Feature.ALLOW_COMMENTS)                                  //  在JSON中允许C/C++ 样式的注释
                    // (非标准，默认禁用)
                    .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)                      // 允许没有引号的字段名（非标准）
                    .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)                             // 允许单引号（非标准）
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)       // 序列化所有 field
                    .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)     // 默认忽略所有 getter
                    .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)     // 默认忽略所有 setter
                    .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)  // 默认忽略所有 is_getter
            ;
    static final ObjectMapper JSON_MAPPER = MAPPER_FACTORY.apply(new MappingJsonFactory());

    // static final ObjectMapper YAML_MAPPER = MAPPER_FACTORY.apply(new YAMLFactory());
    //
    // static ObjectMapper getMapper(String extension) {
    //     return switch (extension.toLowerCase()) {
    //         case "yaml" -> YAML_MAPPER;
    //         case "json" -> JSON_MAPPER;
    //         default -> throw new IllegalArgumentException(Text.format("不支持的格式，extension: {}", extension));
    //     };
    // }
    //#endregion
}
