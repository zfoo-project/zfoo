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

package com.zfoo.util.math.lexer;

import com.zfoo.protocol.util.StringUtils;

import java.util.Set;

/**
 * 词法分析器
 *
 * @author godotg
 * @version 3.0
 */
public class LexicalAnalysis {
    private static final Set<String> KEY_WORDS = Set.of("break", "include", "begin", "end", "if", "else", "while", "switch");


    //判断是否是关键字
    private boolean isKey(String str) {
        return KEY_WORDS.contains(str);
    }

    //判断是否是字母
    boolean isLetter(char letter) {
        return StringUtils.isEnglishChar(letter);
    }

    //判断是否是数字
    boolean isDigit(char digit) {
        return Character.isDigit(digit);
    }

    //词法分析
    void analyze(char[] chars) {
        for (var i = 0; i < chars.length; i++) {
            var ch = chars[i];
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                continue;
            }
            var arr = "";

            if (isLetter(ch)) {
                while (isLetter(ch) || isDigit(ch)) {
                    arr += ch;
                    ch = chars[++i];
                }
                //回退一个字符
                i--;
                if (isKey(arr)) {
                    //关键字
                    System.out.println(arr + "\t4" + "\t关键字");
                } else {
                    //标识符
                    System.out.println(arr + "\t4" + "\t标识符");
                }
            } else if (isDigit(ch) || (ch == '.')) {
                while (isDigit(ch) || (ch == '.' && isDigit(chars[++i]))) {
                    if (ch == '.') {
                        i--;
                    }
                    arr = arr + ch;
                    ch = chars[++i];
                }
                //属于无符号常数
                System.out.println(arr + "\t5" + "\t常数");
            } else {
                switch (ch) {
                    //运算符
                    case '+':
                        System.out.println(ch + "\t2" + "\t运算符");
                        break;
                    case '-':
                        System.out.println(ch + "\t2" + "\t运算符");
                        break;
                    case '*':
                        System.out.println(ch + "\t2" + "\t运算符");
                        break;
                    case '/':
                        System.out.println(ch + "\t2" + "\t运算符");
                        break;
                    //分界符
                    case '(':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    case ')':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    case '[':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    case ']':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    case ';':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    case '{':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    case '}':
                        System.out.println(ch + "\t3" + "\t分界符");
                        break;
                    // 字符串
                    case '"':
                        do {
                            arr += ch;
                            ch = chars[++i];
                        } while (ch != '"');
                        arr += ch;

                        System.out.println(arr + "\t3" + "\t字符串");
                        break;
                    //运算符
                    case '=': {
                        ch = chars[++i];
                        if (ch == '=') {
                            System.out.println("==" + "\t2" + "\t运算符");
                        } else {
                            System.out.println("=" + "\t2" + "\t运算符");
                            i--;
                        }
                    }
                    break;
                    case ':': {
                        ch = chars[++i];
                        if (ch == '=') {
                            System.out.println(":=" + "\t2" + "\t运算符");
                        } else {
                            System.out.println(":" + "\t2" + "\t运算符");
                            i--;
                        }
                    }
                    break;
                    case '>': {
                        ch = chars[++i];
                        if (ch == '=') {
                            System.out.println(">=" + "\t2" + "\t运算符");
                        } else {
                            System.out.println(">" + "\t2" + "\t运算符");
                            i--;
                        }
                    }
                    break;
                    case '<': {
                        ch = chars[++i];
                        if (ch == '=') {
                            System.out.println("<=" + "\t2" + "\t运算符");
                        } else {
                            System.out.println("<" + "\t2" + "\t运算符");
                            i--;
                        }
                    }
                    break;
                    //无识别
                    default:
                        System.out.println(ch + "\t6" + "\t无识别符");
                }
            }
        }
    }
}
