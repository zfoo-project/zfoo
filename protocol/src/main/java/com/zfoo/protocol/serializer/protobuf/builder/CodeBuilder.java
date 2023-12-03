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

package com.zfoo.protocol.serializer.protobuf.builder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 简易的代码构建器,方便进行行内的占位符,占位符采用$key$的形式方便预生成代码的阅读.
 * 占位符采用下标的方式赋值$name$中间的字符会被忽略
 */
public class CodeBuilder {
    private final StringBuilder code;
    private static final String INDENT_CHARS = "    ";
    private static final String SHORT_INDENT_CHARS = "\t";
    private static final String LINE_SPERATOR = "\n";
    private final BlockingQueue<String> prepares;
    private final BlockingQueue<String> args;

    /**
     * 使用紧凑的缩进，紧凑缩进使用一个"\t"tab键缩进的方式
     */
    private boolean compactIdentation;

    public CodeBuilder() {
        code = new StringBuilder();
        prepares = new ArrayBlockingQueue<>(40);
        args = new ArrayBlockingQueue<>(40);
        compactIdentation = false;
    }

    /**
     * 向现有代码中缩进指定的级别
     *
     * @param level 缩进的级别
     * @return
     */
    public CodeBuilder t(int level) {
        flush();
        for (int i = 0; i < level; i++) {
            if (!isCompactIdentation()) {
                code.append(INDENT_CHARS);
            } else {
                code.append(SHORT_INDENT_CHARS);
            }
        }
        return this;
    }

    /**
     * 为占位符的表达式中占位符进行赋值
     *
     * @param arg 占位符赋值的字符串数组
     * @return
     */
    public CodeBuilder arg(String... arg) {
        if (prepares.isEmpty()) {
            return this;
        }
        for (String a : arg) {
            args.offer(a);
        }
        if (args.size() >= prepares.size() - 1) {
            flush();
        }
        return this;
    }

    /**
     * 向代码中追加字符串
     *
     * @param str 需要追加的字符串
     * @return
     */
    public CodeBuilder c(String str) {
        flush();
        code.append(str);
        return this;
    }

    /**
     * 指定需要替换占位符的字符串,占位符使用"$$"包含,"$"内部的字符不会输出一个"$$"作为一个
     * 占位符,如果需要输出"$"则使用"\"进行转义
     *
     * @param expression 需要替换占位符的表达式
     * @return
     */
    public CodeBuilder e(String expression) {
        if (expression == null || expression.isEmpty()) {
            return this;
        }
        flush();
        char c;
        boolean isStarted = false;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            c = expression.charAt(i);
            if (c == '$') {
                isStarted = dealTmpData(tmp, c, isStarted);
            } else {
                tmp.append(c);
            }
        }
        prepares.offer(tmp.toString());
        return this;
    }

    /**
     * 当出现$占位符时,判断解析状态并返回下一个字符的解析状态
     *
     * @param tmp       临时保存的数据
     * @param c         当前的字符
     * @param isStarted 当前解析的状态
     * @return
     */
    private boolean dealTmpData(StringBuilder tmp, char c, final boolean isStarted) {
        boolean start = isStarted;
        if (tmp.length() == 0) {
            if (isStarted) {
                start = false;
                tmp.delete(0, tmp.length());
            } else {
                prepares.offer(tmp.toString());
                start = true;
                tmp.delete(0, tmp.length());
            }
        } else {
            if (tmp.charAt(tmp.length() - 1) != '\\') {
                if (isStarted) {
                    start = false;
                    tmp.delete(0, tmp.length());
                } else {
                    prepares.offer(tmp.toString());
                    start = true;
                    tmp.delete(0, tmp.length());
                }
            } else {
                tmp.setCharAt(tmp.length() - 1, c);
            }
        }
        return start;
    }

    @Override
    public String toString() {
        flush();
        return code.toString();
    }

    /**
     * 刷新占位符替换后的字符串到现有代码中,当给定的变量个数多余占位符的个数时多余的变量
     * 不会被输出
     *
     * @return
     */
    public CodeBuilder flush() {
        if (prepares.isEmpty()) {
            args.clear();
            return this;
        }
        int count = prepares.size();
        String str;
        for (int i = 0; i < count - 1; i++) {
            str = prepares.poll();
            if (str != null) {
                code.append(str);
            }
            String val = args.poll();
            if (val != null) {
                code.append(val);
            }
        }
        str = prepares.poll();
        while (str != null) {
            code.append(str);
            str = prepares.poll();
        }
        args.clear();
        return this;
    }

    /**
     * 向代码中追加指定个数的换行符
     *
     * @param rows 换行符的个数
     * @return
     */
    public CodeBuilder ln(int rows) {
        flush();
        code.append(LINE_SPERATOR);
        if (rows > 1) {
            for (int i = 1; i < rows; i++) {
                code.append(LINE_SPERATOR);
            }
        }
        return this;
    }

    /**
     * 向代码中追加一个换行符,如果有没有输出的占位符的字符串则会先追加占位符的字符串
     *
     * @return
     */
    public CodeBuilder ln() {
        return ln(1);
    }

    /**
     * 使用紧凑的缩进，紧凑缩进使用一个"\t"tab键缩进的方式
     */
    public boolean isCompactIdentation() {
        return compactIdentation;
    }

}