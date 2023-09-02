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

package com.zfoo.protocol.collection.tree;


import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

/**
 * DFA（Deterministic Finite Automaton 确定有穷自动机）
 * DFA单词树（以下简称单词树），常用于在某大段文字中快速查找某几个关键词是否存在。<br>
 * 单词树使用group区分不同的关键字集合，不同的分组可以共享树枝，避免重复建树。<br>
 * 单词树使用树状结构表示一组单词。<br>
 * 例如：红领巾，红河构建树后为：<br>
 * <br>                红
 * <br>             /      \
 * <br>          领         河
 * <br>         /
 * <br>       巾
 * 其中每个节点都是一个WordTree对象，查找时从上向下查找。<br>
 */
public class DfaWordTree extends HashMap<Character, DfaWordTree> {


    /**
     * 敏感词字符末尾标识，用于标识单词末尾字符
     */
    private Set<Character> endCharacterSet = new HashSet<>();


    /**
     * 默认构造
     */
    public DfaWordTree() {
    }


    /**
     * 增加一组单词
     *
     * @param words 单词集合
     */
    public void addWords(Collection<String> words) {
        var wordSet = new HashSet<>(words);
        for (var word : wordSet) {
            addWord(word);
        }
    }

    /**
     * 添加单词，使用默认类型
     *
     * @param word 单词
     */
    public void addWord(String word) {
        DfaWordTree parent = null;
        DfaWordTree current = this;
        DfaWordTree child;
        char currentChar = 0;
        int length = word.length();
        for (var i = 0; i < length; i++) {
            currentChar = word.charAt(i);
            // 只处理合法字符
            if (!StringUtils.isStopChar(currentChar)) {
                child = current.get(currentChar);
                if (child == null) {
                    // 无子类，新建一个子节点后存放下一个字符
                    child = new DfaWordTree();
                    current.put(currentChar, child);
                }
                parent = current;
                current = child;
            }
        }
        if (null != parent && parent != this) {
            parent.setEnd(currentChar);
        }
    }

    //------------------------------------------------------------------------------- match

    /**
     * 指定文本是否包含树中的词
     *
     * @param text 被检查的文本
     * @return 是否包含
     */
    public boolean isMatch(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        var matchAll = matchAll(text, 1);
        return CollectionUtils.isNotEmpty(matchAll);
    }


    //------------------------------------------------------------------------------- match all

    /**
     * 找出所有匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text) {
        return matchAll(text, -1);
    }

    /**
     * 找出所有匹配的关键字
     *
     * @param text  被检查的文本
     * @param limit 限制匹配个数
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text, int limit) {
        return matchAll(text, limit, false, false);
    }

    /**
     * 找出所有匹配的关键字<br>
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]<br>
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           被检查的文本
     * @param limit          限制匹配个数
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text, int limit, boolean isDensityMatch, boolean isGreedMatch) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }

        var findWords = new ArrayList<String>();
        DfaWordTree current = this;
        int length = text.length();
        // 存放查找到的字符缓存。完整出现一个词时加到findedWords中，否则清空
        StringBuilder wordBuffer;
        char currentChar;
        for (int i = 0; i < length; i++) {
            wordBuffer = new StringBuilder();
            for (int j = i; j < length; j++) {
                currentChar = text.charAt(j);
                if (StringUtils.isStopChar(currentChar)) {
                    if (wordBuffer.length() > 0) {
                        // 做为关键词中间的停顿词被当作关键词的一部分被返回
                        wordBuffer.append(currentChar);
                    } else {
                        // 停顿词做为关键词的第一个字符时需要跳过
                        i++;
                    }
                    continue;
                } else if (!current.containsKey(currentChar)) {
                    // 非关键字符被整体略过，重新以下个字符开始检查
                    break;
                }
                wordBuffer.append(currentChar);
                if (current.isEnd(currentChar)) {
                    // 到达单词末尾，关键词成立，从此词的下一个位置开始查找
                    findWords.add(wordBuffer.toString());
                    if (limit > 0 && findWords.size() >= limit) {
                        // 超过匹配限制个数，直接返回
                        return findWords;
                    }
                    if (!isDensityMatch) {
                        // 如果非密度匹配，跳过匹配到的词
                        i = j;
                    }
                    if (!isGreedMatch) {
                        // 如果懒惰匹配（非贪婪匹配）。当遇到第一个结尾标记就结束本轮匹配
                        break;
                    }
                }
                current = current.get(currentChar);
                if (null == current) {
                    break;
                }
            }
            current = this;
        }
        return findWords;
    }


    /**
     * 是否末尾
     *
     * @param c 检查的字符
     * @return 是否末尾
     */
    private boolean isEnd(Character c) {
        return this.endCharacterSet.contains(c);
    }

    /**
     * 设置是否到达末尾
     *
     * @param c 设置结尾的字符
     */
    private void setEnd(Character c) {
        if (null != c) {
            this.endCharacterSet.add(c);
        }
    }
}
