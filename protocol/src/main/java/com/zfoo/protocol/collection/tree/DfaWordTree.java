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
 * DFA (Deterministic Finite Automaton)
 * DFA word tree (a.k.a. word tree): commonly used to quickly scan large text for the presence of certain keywords.<br>
 * The word tree uses groups to distinguish different keyword sets; groups share branches to avoid rebuilding.<br>
 * The word tree uses a tree structure to represent a set of words.<br>
 * For example, after building a tree with 'red-scarf' and 'red-river':<br>
 * <br>                (red)
 * <br>             /      \
 * <br>          (scarf)    (river)
 * <br>         /
 * <br>       (-)
 * Each node is a WordTree object; search proceeds top-down.<br>
 */
public class DfaWordTree extends HashMap<Character, DfaWordTree> {


    /**
     * End-of-word marker; marks the last character of a word
     */
    private Set<Character> endCharacterSet = new HashSet<>();


    /**
     * Default constructor
     */
    public DfaWordTree() {
    }


    /**
     * Add a group of words
     *
     * @param words set of words
     */
    public void addWords(Collection<String> words) {
        var wordSet = new HashSet<>(words);
        for (var word : wordSet) {
            addWord(word);
        }
    }

    /**
     * Add a word using the default type
     *
     * @param word the word
     */
    public void addWord(String word) {
        DfaWordTree parent = null;
        DfaWordTree current = this;
        DfaWordTree child;
        char currentChar = 0;
        int length = word.length();
        for (var i = 0; i < length; i++) {
            currentChar = word.charAt(i);
            // Only process valid characters
            if (!StringUtils.isStopChar(currentChar)) {
                child = current.get(currentChar);
                if (child == null) {
                    // No children yet; create a new child node for the next character
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
     * Check if the given text contains any word in the tree
     *
     * @param text the text to check
     * @return true if a match was found
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
     * Find all matching keywords
     *
     * @param text the text to check
     * @return list of matched words
     */
    public List<String> matchAll(String text) {
        return matchAll(text, -1);
    }

    /**
     * Find all matching keywords
     *
     * @param text  the text to check
     * @param limit maximum number of matches
     * @return list of matched words
     */
    public List<String> matchAll(String text, int limit) {
        return matchAll(text, limit, false, false);
    }

    /**
     * Find all matching keywords<br>
     * Dense matching: if keywords are [ab, b] and text is 'abab', matches [ab, b, ab]<br>
     * Greedy (longest) matching: if keywords are [a, ab], matches [a, ab]
     *
     * @param text           the text to check
     * @param limit          maximum number of matches
     * @param isDensityMatch true to use dense matching
     * @param isGreedMatch   true to use greedy (longest) matching
     * @return list of matched words
     */
    public List<String> matchAll(String text, int limit, boolean isDensityMatch, boolean isGreedMatch) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }

        var findWords = new ArrayList<String>();
        DfaWordTree current = this;
        int length = text.length();
        // Buffer for characters found during search; flushed to findedWords on a complete match
        StringBuilder wordBuffer;
        char currentChar;
        for (int i = 0; i < length; i++) {
            wordBuffer = new StringBuilder();
            for (int j = i; j < length; j++) {
                currentChar = text.charAt(j);
                if (StringUtils.isStopChar(currentChar)) {
                    if (wordBuffer.length() > 0) {
                        // Pause words in the middle of a keyword are included as part of the match
                        wordBuffer.append(currentChar);
                    } else {
                        // Skip pause words that appear as the first character of a potential keyword
                        i++;
                    }
                    continue;
                } else if (!current.containsKey(currentChar)) {
                    // Non-keyword characters are skipped entirely; restart check from the next character
                    break;
                }
                wordBuffer.append(currentChar);
                if (current.isEnd(currentChar)) {
                    // End of word reached; keyword confirmed. Continue search from the next position
                    findWords.add(wordBuffer.toString());
                    if (limit > 0 && findWords.size() >= limit) {
                        // Exceeded match limit; return immediately
                        return findWords;
                    }
                    if (!isDensityMatch) {
                        // If not dense matching, skip over the matched word
                        i = j;
                    }
                    if (!isGreedMatch) {
                        // Lazy (non-greedy) matching: stop at the first end marker
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
     * Check if this node is a word-end marker
     *
     * @param c the character to check
     * @return Check if this node is a word-end marker
     */
    private boolean isEnd(Character c) {
        return this.endCharacterSet.contains(c);
    }

    /**
     * Set whether this is a word-end node
     *
     * @param c the character to mark as end
     */
    private void setEnd(Character c) {
        if (null != c) {
            this.endCharacterSet.add(c);
        }
    }
}
