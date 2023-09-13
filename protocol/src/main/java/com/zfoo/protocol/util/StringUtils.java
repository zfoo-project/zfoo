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

import com.zfoo.protocol.collection.ArrayUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Operations on {@link String} that are null safe and thread safe.
 *
 * @author godotg
 */
public abstract class StringUtils {

    public static final String EMPTY = "";
    public static final String[] EMPTY_ARRAY = new String[0];

    public static final String SPACE = " ";
    public static final String SPACE_REGEX = "\\s+";

    public static final String TAB = "    ";
    public static final String TAB_ASCII = "\t";

    public static final String COMMA = ",";     // [com·ma || 'kɒmə] n.  逗点; 逗号
    public static final String COMMA_REGEX = ",|，";

    public static final String PERIOD = ".";    // 句号
    public static final String PERIOD_REGEX = "\\.";

    public static final String LEFT_SQUARE_BRACKET = "[";   // 左方括号

    public static final String RIGHT_SQUARE_BRACKET = "]";  // 右方括号

    public static final String COLON = ":";     // 冒号[co·lon || 'kəʊlən]
    public static final String COLON_REGEX = ":|：";

    public static final String SEMICOLON = ";";     // 分号['semi'kәulәn]
    public static final String SEMICOLON_REGEX = ";|；";

    public static final String QUOTATION_MARK = "\"";   // 引号[quo·ta·tion || kwəʊ'teɪʃn]

    public static final String ELLIPSIS = "...";    // 省略号

    public static final String EXCLAMATION_POINT = "!"; // 感叹号

    public static final String DASH = "-";      //  破折号

    public static final String QUESTION_MARK = "?"; // 问号

    public static final String HYPHEN = "-";    // 连接号，连接号与破折号的区别是，连接号的两头不用空格

    public static final String SLASH = "/";     // 斜线号

    public static final String EQUAL = "=";     // 等于号

    public static final String BACK_SLASH = "\\";   //反斜线号

    public static final String VERTICAL_BAR = "|";  // 竖线
    public static final String VERTICAL_BAR_REGEX = "\\|";

    public static final String SHARP = "#";
    public static final String SHARP_REGEX = "\\#";

    public static final String DOLLAR = "$";    // 美元符号

    public static final String EMPTY_JSON = "{}";


    public static final String MULTIPLE_HYPHENS = "-----------------------------------------------------------------------";


    public static final int INDEX_NOT_FOUND = -1;   // Represents a failed index search.

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

    /**
     * 用于随机选的数字
     */
    public static final String ARAB_NUMBER = "0123456789";
    /**
     * 用于随机选的字符
     */
    public static final String ENGLISH_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final Set<Character> ENGLISH_SET = Set.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');


    public static final Set<Character> STOP_WORD = Set.of(' ', '`', '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '/', '[', '{', ']', '}', '\\', '|', ';', ':', '\'', '"', ',', '<', '.', '>', '?', '·', '！', '￥', '，', '。', '、', 'ˇ', '＃', '％', '＆', '（', '）', '．', '／', '：', '；', '＜', '＝', '＞', '？', '〔', '〕', '＼', '＿', '｛', '｜', '｝', '—', '～', '‖', '…', '‘', '’', '“', '”', '〈', '〉', '《', '》', '「', '」', '『', '』', '〖', '〗', '【', '】', '±', '＋', '－', '×', '÷', '∧', '∨', '∏', '∪', '∩', '∈', '√', '⊥', '⊙', '∫', '∮', '≡', '≌', '≈', '∽', '∝', '≠', '≮', '≯', '≤', '≥', '∞', '∶', '∵', '∴', '∷', '♂', '♀', '°', '′', '〃', '℃', '＄', '¤', '￠', '￡', '‰', '§', '☆', '★', '〇', '○', '●', '◎', '◇', '◆', '□', '■', '△', '▽', '⊿', '▲', '▼', '◣', '◤', '◢', '◥', '▁', '▂', '▃', '▄', '▅', '▆', '▇', '█', '▉', '▊', '▋', '▌', '▍', '▎', '▏', '▓', '※', '→', '←', '↑', '↓', '↖', '↗', '↘', '↙', '〓', '①', '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨', '⑩', '⒈', '⒉', '⒊', '⒋', '⒌', '⒍', '⒎', '⒏', '⒐', '⒑', '⒒', '⒓', '⒔', '⒕', '⒖', '⒗', '⒘', '⒙', '⒚', '⒛', '⑴', '⑵', '⑶', '⑷', '⑸', '⑹', '⑺', '⑻', '⑼', '⑽', '⑾', '⑿', '⒀', '⒁', '⒂', '⒃', '⒄', '⒅', '⒆', '⒇', 'Ⅰ', 'Ⅱ', 'Ⅲ', 'Ⅳ', 'Ⅴ', 'Ⅵ', 'Ⅶ', 'Ⅷ', 'Ⅸ', 'Ⅹ', 'Ⅺ', 'Ⅻ', 'ⅰ', 'ⅱ', 'ⅲ', 'ⅳ', 'ⅴ', 'ⅵ', 'ⅶ', 'ⅷ', 'ⅸ', 'ⅹ', '０', '１', '２', '３', '４', '５', '６', '７', '８', '９', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'Ρ', '∑', 'Υ', 'Φ', 'Χ', 'Ψ', 'Ω', 'α', 'β', 'γ', 'δ', 'ε', 'ζ', 'η', 'θ', 'ι', 'κ', 'λ', 'μ', 'ν', 'ξ', 'ο', 'π', 'ρ', 'σ', 'τ', 'υ', 'φ', 'χ', 'ψ', 'ω', '＾', '﹊', '﹍', '╭', '╮', '╰', '╯', '々', '', '＠', '＊', '卐', '㎎', '㎏', '㎜', '㎝', '㎞', '㎡', '㏄', '㏎', '㏑', '㏒', '㏕', '´', '﹏');


    // Empty checks
    //-----------------------------------------------------------------------

    /**
     * Checks if a CharSequence is empty ("") or null.
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * </pre>
     * It no longer trims the CharSequence.
     * That functionality is available in isBlank().
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * StringUtils.isBlank(null)=true
     * StringUtils.isBlank("")=true
     * StringUtils.isBlank("    ")=true
     * StringUtils.isBlank(" b ")=false
     *
     * @param cs 要检查的字符串
     * @return 是否为空的字符串
     */
    public static boolean isBlank(final CharSequence cs) {
        if (isEmpty(cs)) {
            return true;
        }
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static String trim(String str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return str.trim();
    }

    // Case conversion
    //-----------------------------------------------------------------------

    /**
     * 首字母小写(capitalize  ['kæpɪtlaɪz] vt.以大写字母写,【根】cap＝head（头）)
     *
     * @param str 被转换的字符串，可以为null
     * @return 首字母小写的字符串，如果str为null，则返回null
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    /**
     * Capitalize a {@code String}, changing the first letter to
     * upper case as per {@link Character#toUpperCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to capitalize, may be {@code null}
     * @return the capitalized {@code String}, or {@code null} if the supplied
     * string is {@code null}
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        if (capitalize) {
            sb.append(Character.toUpperCase(str.charAt(0)));
        } else {
            sb.append(Character.toLowerCase(str.charAt(0)));
        }
        sb.append(str.substring(1));
        return sb.toString();
    }


    // SubStringAfter/SubStringBefore
    //-----------------------------------------------------------------------

    /**
     * 从第一个分隔符开始分割
     * <p>Gets the substring before the first occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * A {@code null} separator will return the input string.</p>
     *
     * <p>If nothing is found, the string input is returned.</p>
     *
     * <pre>
     * StringUtils.substringBefore(null, *)      = null
     * StringUtils.substringBefore("", *)        = ""
     * StringUtils.substringBefore("abc", "a")   = ""
     * StringUtils.substringBefore("abcba", "b") = "a"
     * StringUtils.substringBefore("abc", "c")   = "ab"
     * StringUtils.substringBefore("abc", "d")   = "abc"
     * StringUtils.substringBefore("abc", "")    = ""
     * StringUtils.substringBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the first occurrence of the separator,
     * {@code null} if null String input
     */
    public static String substringBeforeFirst(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringBeforeLast(final String str, final String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 从第一个分隔符开始分割
     * <p>Gets the substring after the first occurrence of a separator.
     * The separator is not returned.</p>
     *
     * <p>A {@code null} string input will return {@code null}.
     * An empty ("") string input will return the empty string.
     * A {@code null} separator will return the empty string if the
     * input string is not {@code null}.</p>
     *
     * <p>If nothing is found, the empty string is returned.</p>
     *
     * <pre>
     * StringUtils.substringAfter(null, *)      = null
     * StringUtils.substringAfter("", *)        = ""
     * StringUtils.substringAfter(*, null)      = ""
     * StringUtils.substringAfter("abc", "a")   = "bc"
     * StringUtils.substringAfter("abcba", "b") = "cba"
     * StringUtils.substringAfter("abc", "c")   = ""
     * StringUtils.substringAfter("abc", "d")   = ""
     * StringUtils.substringAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the first occurrence of the separator,
     * {@code null} if null String input
     */
    public static String substringAfterFirst(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * <p>Gets the substring after the last occurrence of a separator.
     * The separator is not returned.</p>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the last occurrence of the separator,
     * {@code null} if null String input
     */
    public static String substringAfterLast(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return EMPTY;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }


    // Replacing
    //-----------------------------------------------------------------------

    /**
     * Replaces each substring of the source String that matches the given regular expression with the given
     * replacement using the {@link Pattern#DOTALL} option.
     * <p>
     * 在 DOTALL 模式中，表达式 .可以匹配任何字符，包括行结束符。默认情况下，此表达式不匹配行结束符。
     *
     * @param source      the source string
     * @param regex       the regular expression to which this string is to be matched
     * @param replacement the string to be substituted for each match
     * @return The resulting {@code String}
     */
    public static String replacePattern(final String source, final String regex, final String replacement) {
        return Pattern.compile(regex, Pattern.DOTALL).matcher(source).replaceAll(replacement);
    }


    // Joining
    //-----------------------------------------------------------------------

    public static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static byte[] bytes(final String str) {
        try {
            return str.getBytes(DEFAULT_CHARSET);
        } catch (Exception e) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public static String bytesToString(final byte[] bytes) {
        try {
            return new String(bytes, DEFAULT_CHARSET);
        } catch (Exception e) {
            return EMPTY;
        }
    }

    /**
     * Joins the elements of the provided varargs into a single String containing the provided elements.
     * No delimiter is added before or after the list.
     * null elements and separator are treated as empty Strings ("").
     *
     * <pre>
     * StringUtils.joinWith(",", {"a", "b"})        = "a,b"
     * StringUtils.joinWith(",", {"a", "b",""})     = "a,b,"
     * StringUtils.joinWith(",", {"a", null, "b"})  = "a,,b"
     * StringUtils.joinWith(null, {"a", "b"})       = "ab"
     * </pre>
     *
     * @param separator the separator character to use, null treated as ""
     * @param objects   the varargs providing the values to join together. {@code null} elements are treated as ""
     * @return the joined String.
     * @throws java.lang.IllegalArgumentException if a null varargs is provided
     */
    public static String joinWith(final String separator, final Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("Object varargs must not be null");
        }

        final String sanitizedSeparator = defaultString(separator, EMPTY);

        final StringBuilder result = new StringBuilder();

        final Iterator<Object> iterator = Arrays.asList(objects).iterator();
        while (iterator.hasNext()) {
            final String value = Objects.toString(iterator.next(), "");
            result.append(value);

            if (iterator.hasNext()) {
                result.append(sanitizedSeparator);
            }
        }

        return result.toString();
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     */
    public static String[] tokenize(String str, String delimiters) {
        if (isEmpty(str)) {
            return EMPTY_ARRAY;
        }
        if (isEmpty(delimiters)) {
            return new String[]{str};
        }
        var st = new StringTokenizer(str, delimiters);
        var tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            var token = trim(st.nextToken());
            if (isBlank(token)) {
                continue;
            }
            tokens.add(token);
        }
        return ArrayUtils.listToArray(tokens, String.class);
    }

    /**
     * 格式化字符串
     * 此方法只是简单将占位符 {} 按照顺序替换为参数
     * 例：
     * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b
     *
     * @param template 字符串模板
     * @param args     参数列表
     * @return 结果
     */
    public static String format(final String template, final Object... args) {
        if (isBlank(template) || ArrayUtils.isEmpty(args)) {
            return template;
        }

        // 初始化定义好的长度以获得更好的性能
        var builder = new StringBuilder(template.length() + 50);

        // 记录已经处理到的位置
        var readIndex = 0;
        for (int i = 0; i < args.length; i++) {
            // 占位符所在位置
            var placeholderIndex = template.indexOf(EMPTY_JSON, readIndex);
            // 剩余部分无占位符
            if (placeholderIndex == -1) {
                // 不带占位符的模板直接返回
                if (readIndex == 0) {
                    return template;
                }
                break;
            }

            builder.append(template, readIndex, placeholderIndex);
            builder.append(args[i]);
            readIndex = placeholderIndex + 2;
        }

        // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
        builder.append(template, readIndex, template.length());
        return builder.toString();
    }

    public static boolean isEnglishChar(char ch) {
        return ENGLISH_SET.contains(ch);
    }

    /**
     * 判断指定的词是否是不处理的词。
     * 如果参数为空，则返回true，因为空也属于不处理的字符。
     *
     * @param ch 指定的词
     * @return 是否是不处理的词
     */
    public static boolean isStopChar(char ch) {
        return Character.isWhitespace(ch) || STOP_WORD.contains(ch);
    }

}
