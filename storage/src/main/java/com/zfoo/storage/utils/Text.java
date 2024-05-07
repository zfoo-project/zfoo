package com.zfoo.storage.utils;

import org.apache.logging.log4j.message.ParameterizedMessage;

import java.text.MessageFormat;
import java.util.Map;

public interface Text {
    /**
     * 字符串插值，format("{1}, {0}", "a", "b") // output: "b, a"
     *
     * @param pattern   模板，用 {index} 占位，index 从零开始
     * @param arguments 参数
     * @return .
     */
    static String format(String pattern, Object... arguments) {
        // Log4j2 有 Bug，参见：https://issues.apache.org/jira/browse/LOG4J2-3542
        // return new FormattedMessage(pattern, arguments).getFormattedMessage();

        try {
            MessageFormat format = new MessageFormat(pattern);
            if (format.getFormats().length > 0) return format.format(arguments);
        } catch (Exception ignored) {
            // Obviously, the message is not a proper pattern for MessageFormat.
        }
        return ParameterizedMessage.format(pattern, arguments);
    }

    /**
     * 字符串插值，format("{b}, {a}", Map.of("a", 1, "b:", 2)) // output: "b: 2, 1"
     *
     * @param pattern   模板，用 {key} 占位
     * @param arguments 参数
     * @return .
     */
    static String format(String pattern, Map<String, Object> arguments) {
        for (String key : arguments.keySet()) {
            String value = String.valueOf(arguments.get(key));
            if (key.endsWith(":")) {
                value = key + " " + value;
                key = key.substring(0, key.length() - 1);
            }
            pattern = pattern.replace("{" + key + "}", value);
        }
        return pattern;
    }

    static String hexIdHashCode(Object object) {
        return Integer.toHexString(System.identityHashCode(object));
    }
}
