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
package stringutil

import (
	"bytes"
	"fmt"
	"net/url"
	"regexp"
	"strconv"
	"strings"
	"unicode"
	"unsafe"
)

// Reverse 反转字符串
func Reverse(s string) string {
	r := []rune(s)
	for i, j := 0, len(r)-1; i < len(r)/2; i, j = i+1, j-1 {
		r[i], r[j] = r[j], r[i]
	}
	return string(r)
}

// UcFirst 首字母大写
func UcFirst(s string) string {
	for i, v := range s {
		return string(unicode.ToUpper(v)) + s[i+1:]
	}
	return s
}

// LcFirst 首字母小写
func LcFirst(s string) string {
	for i, v := range s {
		return string(unicode.ToLower(v)) + s[i+1:]
	}
	return s
}

// CamelToSnake camel => snake 简单实现
func CamelToSnake(s string) string {
	buffer := new(bytes.Buffer)
	for i, r := range s {
		if unicode.IsUpper(r) {
			if i != 0 {
				buffer.WriteRune('_')
			}
			buffer.WriteRune(unicode.ToLower(r))
		} else {
			buffer.WriteRune(r)
		}
	}
	return buffer.String()
}

// SnakeToCamel snake => camel 简单实现
func SnakeToCamel(s string) string {
	s = strings.Replace(s, "_", " ", -1)
	s = strings.Title(s)
	return strings.Replace(s, " ", "", -1)
}

func SnakeToSpinal(s string) string {
	return strings.Replace(s, "_", "-", -1)
}

func SpinalToSnake(s string) string {
	return strings.Replace(s, "-", "_", -1)
}

// UrlEncode 空格被编码为+，+被编码为%2B
func UrlEncode(s string) string {
	return url.QueryEscape(s)
}

// UrlDecode URL解码
func UrlDecode(s string) string {
	u, _ := url.QueryUnescape(s)
	return u
}

// Substr 字符串切割
func Substr(s string, pos, length int) string {
	r := []rune(s)
	sl := len(r)
	if pos >= sl {
		return ""
	}
	idx := pos + length
	if length == 0 || idx > sl {
		idx = sl
	} else if length < 0 {
		idx = sl + length
	}

	return string(r[pos:idx])
}

// InString 判断子字符串是否存在
func InString(sub, str string) bool {
	if str != "" && strings.Contains(str, sub) {
		return true
	}
	return false
}

// RegexpReplace ...
func RegexpReplace(src, expr, repl string) (string, error) {
	reg, err := regexp.Compile(expr)
	return reg.ReplaceAllString(src, repl), err
}

// TrimSpace 去除字符串前后空格、换行等
func TrimSpace(s string) string {
	s = strings.TrimSpace(s)
	s = strings.Trim(s, "\r")
	s = strings.Trim(s, "\n")
	s = strings.Trim(s, "\t")
	return s
}

func CamelCase(s string) string {
	if s == "" {
		return ""
	}
	t := make([]byte, 0, 32)
	i := 0
	if s[0] == '_' {
		t = append(t, 'X')
		i++
	}
	for ; i < len(s); i++ {
		c := s[i]
		if c == '_' && i+1 < len(s) && isASCIIUpper(s[i+1]) {
			continue
		}
		if isASCIIDigit(c) {
			t = append(t, c)
			continue
		}

		if isASCIIUpper(c) {
			c ^= ' '
		}
		t = append(t, c)

		for i+1 < len(s) && isASCIIUpper(s[i+1]) {
			i++
			t = append(t, '_')
			t = append(t, bytes.ToLower([]byte{s[i]})[0])
		}
	}
	return string(t)
}
func isASCIIUpper(c byte) bool {
	return 'A' <= c && c <= 'Z'
}

func isASCIIDigit(c byte) bool {
	return '0' <= c && c <= '9'
}

// 手机号码检测
func CheckIsMobile(mobileNum string) bool {
	var regular = "^1[345789]{1}\\d{9}$"
	reg := regexp.MustCompile(regular)
	return reg.MatchString(mobileNum)
}

// 判断是否是18或15位身份证
func IsIdCard(cardNo string) bool {
	// 18位身份证 ^(\d{17})([0-9]|X)$
	if m, _ := regexp.MatchString(`(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)`, cardNo); !m {
		return false
	}
	return true
}

// 字节转字符串
func BytesToString(data []byte) string {
	return *(*string)(unsafe.Pointer(&data))
}

// 字符串转字节数组
func StringToBytes(data string) []byte {
	return *(*[]byte)(unsafe.Pointer(&data))
}

// 判断字符串是否为中文[精确度需要反复试验]
func IsContainCN(str string) bool {
	var hzRegexp = regexp.MustCompile("[\u4e00-\u9fa5]+")
	return hzRegexp.MatchString(str)
}

// Emoji表情解码
func UnicodeEmojiDecode(s string) string {
	//emoji表情的数据表达式
	re := regexp.MustCompile("\\[[\\\\u0-9a-zA-Z]+\\]")
	// 提取emoji数据表达式
	reg := regexp.MustCompile("\\[\\\\u|]")
	src := re.FindAllString(s, -1)
	for i := 0; i < len(src); i++ {
		e := reg.ReplaceAllString(src[i], "")
		p, err := strconv.ParseInt(e, 16, 32)
		if err == nil {
			s = strings.Replace(s, src[i], string(rune(p)), -1)
		}
	}
	return s
}

/**
  身份证手机号填充
*/

func SignIdcard(idcard string) string {
	cp := idcard
	leth := len(cp)
	return cp[0:4] + " **** **** " + cp[leth-4:]
}

func SignMobile(mobile string) string {
	cp := mobile
	leth := len(cp)
	return cp[0:3] + " **** " + cp[leth-4:]
}


// Emoji表情转换
func UnicodeEmojiCode(s string) string {
	ret := ""
	rs := []rune(s)
	for i := 0; i < len(rs); i++ {
		if len(string(rs[i])) == 4 {
			u := `[\u` + strconv.FormatInt(int64(rs[i]), 16) + `]`
			ret += u

		} else {
			ret += string(rs[i])
		}
	}
	return ret
}


// ----------------------------------------------------------------------------------------------------
/**
通过+号拼接字符串
*/
func AddStringWithOperator(str1, str2 string) string {

	return str1 + str2
}

/**
通过strings 包的join连接字符串
*/
func AddStringWidthJoin(strArray []string) string {

	return strings.Join(strArray, "")
}

/**
通过buffer 拼接字符串
*/
func AddStringWidthBuffer(strArray []string) string {
	var buffer bytes.Buffer
	for _, value := range strArray {
		buffer.WriteString(value)
	}
	return buffer.String()
}

/**
反转字符串
*/
func ReversString(str string) string {
	count := len(str)
	bytes := make([]byte, len(str))
	for i := 0; i < len(str); i++ {
		bytes[i] = str[count-1-i]
	}
	return string(bytes)
}

/**
https://www.cnblogs.com/linghu-java/p/9037262.html 参考这边文章
查找给定字符串中的最长不重复子串
返回最长不重复子串+子串的长度
*/
func FindMaxLenNoRepeatSubStr(s string) (string, int) {
	if len(s) == 1 {
		return s, 1
	}
	head, tail := 0, 0
	maxLenNoRepeatSubStr := ""
	for i := 0; i < len(s)-1; i++ {
		for j := i; j < len(s); j++ {
			if strings.Contains(maxLenNoRepeatSubStr, s[j:j+1]) {
				if head == 0 && tail == 0 {
					head, tail = i, j
				}
				if len(s[i:j]) > len(s[head:tail]) {
					head, tail = i, j
				}
				maxLenNoRepeatSubStr = ""
				break
			}
			maxLenNoRepeatSubStr = s[i : j+1]
		}
		if maxLenNoRepeatSubStr == s[i:] && len(s[i:]) > len(s[head:tail]) {
			head, tail = i, len(s)
			break
		}
	}
	return s[head:tail], tail - head
}

/**
查找给定字符串中的最长不重复子串
返回子串的长度
*/
func FindMaxLenNoRepeatSubStr2(s string) int {
	length := len(s)
	ans := 0
	for i := 0; i < length; i++ {
		for j := i + 1; j <= length; j++ {
			if allUnique(s, i, j) {
				if (j - i) > ans {
					ans = j - i
				}
			}
		}
	}
	return ans
}
func allUnique(s string, start int, end int) bool {
	set := make(map[byte]int, 0)
	for i := start; i < end; i++ {
		if _, ok := set[s[i]]; ok {
			return false
		}
		set[s[i]]++
	}
	return true
}

/**
时间滑动窗口思想
查找给定字符串中的最长不重复子串
返回子串的长度
*/
func FindMaxLenNoRepeatSubStr3(s string) int {
	length := len(s)
	ans := 0
	m := make(map[byte]int, 0)
	i, j := 0, 0
	for i < length && j < length {
		//如果不包含
		if _, ok := m[s[j]]; !ok {
			m[s[j]]++
			j++
			if (j - i) > ans {
				ans = j - i
			}
		} else { //如果包含
			delete(m, s[i])
			i++
		}
	}
	return ans
}

/**
从两个给定字符串中找出最长公共子串
返回最长公共子串  "gfdef", "abcdef"
*/
func FindMaxLenCommonSubStr(str1, str2 string) string {
	start1 := -1
	start2 := -1
	longest := 0
	for i := 0; i < len(str1); i++ {
		for j := 0; j < len(str2); j++ {
			length := 0
			m := i
			n := j
			for m < len(str1) && n < len(str2) {
				if str1[m] != str2[n] {
					break
				}
				length++
				m++
				n++
			}
			if longest < length {
				longest = length
				start1 = i
				start2 = j
			}
		}
	}
	if len(str1) > len(str2) {
		return str1[start1 : start1+longest]
	} else {
		return str2[start2 : start2+longest]
	}

}

// 采用动态规划求取最长公共子串
func FindMaxLenCommonSubStr2(str1, str2 string) string {
	l1 := len(str1)
	l2 := len(str2)
	max := 0
	end := 0

	var twoArray [][]int
	for i := 0; i < l1+1; i++ {
		tmp := make([]int, l2+1)
		twoArray = append(twoArray, tmp)
	}
	for i := 1; i <= l1; i++ {
		for j := 1; j <= l2; j++ {
			if str1[i-1] == str2[j-1] {
				twoArray[i][j] = twoArray[i-1][j-1] + 1
				if twoArray[i][j] > max {
					max = twoArray[i][j]
					end = j
				}
			} else {
				twoArray[i][j] = 0
			}
		}
	}
	bytes := make([]byte, 0)
	for m := end - max; m < end; m++ {
		bytes = append(bytes, str2[m])
	}
	return string(bytes)
}

/**
求最长公共子序列
*/
func FindMaxLenCommonSubSeq(str1, str2 string) string {
	l1 := len(str1)
	l2 := len(str2)

	bs := make([]byte, 0)
	for m := 1; m <= l2; m++ {
		for n := 1; n <= l1; n++ {
			if str1[n-1] == str2[m-1] {
				bs = append(bs, str1[n-1])
			}
		}
	}
	return Deduplicate(string(bs))
}

/**
移除字符串中的重复字符
*/
func RemoveRepeatStr(str string) string {
	if len(str) == 0 {
		return ""
	}
	bs := [256]byte{}
	for _, v := range str {
		bs[v] = 1
	}
	rs := make([]byte, 0)
	for index, v := range bs {
		if v == 1 {
			rs = append(rs, byte(index))
		}
	}
	return string(rs)
}

/**
去除重复的字符，返回去重后的字符
申请了新的数组用来存储，空间复杂度o(n)
*/
func Deduplicate(input string) string {
	if len(input) == 0 {
		return ""
	}
	slice := make([]rune, 0, 0)
	m := make(map[rune]byte, 0)
	for _, v := range input {
		if _, ok := m[v]; ok {
			continue
		}
		slice = append(slice, v)
		m[v] = 0
	}
	return string(slice)
}

/**
去除重复的字符，返回去重后的字符
空间复杂度o(1)
*/
func Deduplicate2(input string) string {
	if len(input) == 0 {
		return ""
	}
	m := make(map[rune]byte, 0)
	bs := []byte(input)
	current := 0
	for next, v := range input {
		if _, ok := m[v]; ok {
			continue
		}
		bs[current] = input[next]
		current++
		m[v] = 0
	}
	return string(bs[:current])
}

/**
你有一个单词列表 words 和一个模式  pattern，你想知道 words 中的哪些单词与模式匹配。
如果存在字母的排列 p ，使得将模式中的每个字母 x 替换为 p(x) 之后，我们就得到了所需的单词，那么单词与模式是匹配的。
（回想一下，字母的排列是从字母到字母的双射：每个字母映射到另一个字母，没有两个字母映射到同一个字母。）
返回 words 中与给定模式匹配的单词列表。
你可以按任何顺序返回答案。
*/
func FindAndReplacePattern(words []string, pattern string) []string {
	patternWords := make([]string, 0)
	for _, word := range words {
		flag := true
		ruleMap1 := make(map[byte]byte, len(pattern))
		ruleMap2 := make(map[byte]byte, len(pattern))
		for j := 0; j < len(pattern); j++ {
			p := pattern[j]
			w := word[j]
			if _, ok := ruleMap1[p]; ok {
				if ruleMap1[p] != w {
					flag = false
					break
				}
			} else if _, ok := ruleMap2[w]; ok {
				flag = false
			} else {
				ruleMap1[p] = w
				ruleMap2[w] = p
			}
		}
		if flag {
			patternWords = append(patternWords, word)
		}
	}
	return patternWords
}

/**
判断字符串是否为空
true 为空 false 不为空
*/
func IsBlank(str string) bool {
	return !(len(str) > 0)
}

/**
给定一个非空的字符串，判断它是否可以由它的一个子串重复多次构成。给定的字符串只含有小写英文字母，并且长度不超过10000
"abab" true   "aba" false
*/
func RepeatedSubstringPattern(s string) bool {
	length := len(s)
	if length == 0 || length == 1 {
		return false
	}
	n := 2
	for n <= length {
		mid := length / n
		step := mid
		index := 0
		flag := true
		for mid < length {
			if (mid+step) > length || s[index:mid] != s[mid:mid+step] {
				flag = false
				break
			}
			index = index + step
			mid = mid + step
		}
		if flag {
			fmt.Println(step)
			return true
		}
		n++
	}
	return false
}

/**
给定一个非空的字符串，判断它是否可以由它的一个子串重复多次构成。给定的字符串只含有小写英文字母，并且长度不超过10000
"abab" true   "aba" false
*/
func RepeatedSubstringPattern2(s string) bool {
	if len(s) == 0 {
		return false
	}
	size := len(s)
	ss := (s + s)[1 : size*2-1]
	return strings.Contains(ss, s)
}

func GetNext(p string) []int { //ababda
	next := make([]int, len(p))
	next[0] = -1
	k := -1
	i := 0
	for i < len(p)-1 {
		if k == -1 || p[i] == p[k] {
			k++
			i++
			next[i] = k
		} else {
			k = next[k]
		}
	}
	fmt.Println(next)
	return next
}

/**
KMP 算法，字符串模式匹配算法 主要是 GetNext
匹配 target 在 source 存在时的起始位置 （字符串搜索） "adabeabcabc", "abcabc"
*/
func StrMatch(source, target string) int {
	slen := len(source)
	tlen := len(target)
	next := GetNext(target)
	q := 0
	for i := 0; i < slen; i++ {
		for q > 0 && target[q] != source[i] {
			q = next[q]
		}
		if target[q] == source[i] {
			q++
		}
		if q == tlen {
			return i - tlen + 1
		}
	}
	return -1
}

/**
判断括号是否成对出现
输入: "()[]{}"
输出: true
输入: "(]"
输出: false
*/
func IsValid(s string) bool {
	stack := make([]rune, len(s))
	size := 0
	for _, v := range s {
		if v == '(' {
			stack[size] = ')'
		} else if v == '[' {
			stack[size] = ']'
		} else if v == '{' {
			stack[size] = '}'
		} else {
			if size == 0 && stack[size] == 0 {
				return false
			}
			if size-1 < 0 {
				return false
			}
			if v != stack[size-1] {
				return false
			}
			size--
			continue
		}
		size++
	}
	if size > 0 {
		return false
	}

	return true
}

/**
给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为1000。
示例 1：
输入: "babad"
输出: "bab"
注意: "aba"也是一个有效答案。
示例 2：
输入: "cbbd"
输出: "bb"
*/
func LongestPalindrome(s string) string {
	if len(s) == 0 || len(s) == 1 {
		return s
	}
	head, tail := 0, len(s)-1
	seq := 0
	max := 1
	index := 0
	for head < len(s)-1 {
		tail = len(s) - 1
		tempHead := head
		for tempHead < tail {
			if s[tempHead] == s[tail] {
				seq++
				if tail-tempHead <= 2 {
					break
				}
				tempHead++
				tail--
				continue
			}
			tail--
			if seq > 0 {
				tempHead = tempHead - seq
				tail = tail + seq
				seq = 0
			}
		}
		if seq > 0 {
			if (seq*2 + (tail-tempHead)/2) >= max {
				max = seq*2 + (tail-tempHead)/2
				index = tempHead - seq + 1
			}
		}
		head++
		seq = 0
	}
	return s[index : index+max]
}

/**
给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。
示例 1:
输入: "abcabcbb"
输出: 3
解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。
示例 2:
输入: "bbbbb"
输出: 1
解释: 因为无重复字符的最长子串是 "b"，所以其长度为 1。
示例 3:
输入: "pwwkew"
输出: 3
解释: 因为无重复字符的最长子串是 "wke"，所以其长度为 3。
     请注意，你的答案必须是 子串 的长度，"pwke" 是一个子序列，不是子串。
*/
func LengthOfLongestSubstring(s string) int {
	next := GetNext(s)
	m := make(map[byte]int, 0)
	temp := 0
	max := 0
	for i := 0; i < len(s); i++ {
		if _, ok := m[s[i]]; ok {
			if max == 0 {
				max = temp
			}
			i = next[i] - 1
			m = make(map[byte]int, 0)
			if temp > max {
				max = temp
			}
			temp = 0
			continue
		}
		m[s[i]] = 1
		temp++
	}
	if temp > max {
		max = temp
	}
	return max
}

/**
  最长公共前缀
编写一个函数来查找字符串数组中的最长公共前缀。
如果不存在公共前缀，返回空字符串 ""。
示例 1:
输入: ["flower","flow","flight"]
输出: "fl"
示例 2:
输入: ["dog","racecar","car"]
输出: ""
解释: 输入不存在公共前缀。
说明:
所有输入只包含小写字母 a-z 。
*/

func LongestCommonPrefix(strs []string) string {
	if strs == nil || len(strs) == 0 {
		return ""
	}
	bs := make([]rune, 0, 0)
	strs = sort(strs)
	fmt.Println(strs)
	min := strs[0]
	for index, v := range min {
		for _, n := range strs {
			if n[index] != byte(v) {
				return string(bs)
			}
		}
		bs = append(bs, v)
	}
	return string(bs)
}

/**
字符串按长度排序 归并
*/
func sort(strs []string) []string {
	if len(strs) == 1 {
		return strs
	}
	mid := len(strs) / 2
	left := sort(strs[:mid])
	right := sort(strs[mid:])
	return merger(left, right)

}
func merger(left []string, right []string) []string {
	s := make([]string, 0, 0)
	l, r := 0, 0
	for l < len(left) && r < len(right) {
		if len(left[l]) < len(right[r]) {
			s = append(s, left[l])
			l++
		} else if len(left[l]) > len(right[r]) {
			s = append(s, right[r])
			r++
		} else {
			s = append(s, left[l])
			s = append(s, right[r])
			l++
			r++
		}
	}
	for l < len(left) {
		s = append(s, left[l])
		l++
	}
	for r < len(right) {
		s = append(s, right[r])
		r++
	}
	return s
}

/**
字符串全排列
题目：终端随机输入一串字符串，输出该字符串的所有排列。
　　例如，输入：“abc”，输出：abc、acb、bac、bca、cab、cba
*/
func RecursionPermutation(str string) []string {
	arrays := make([]string, 0, 0)
	arrays = permutation([]byte(str), 0, arrays)
	return arrays
}
func permutation(s []byte, i int, arrays []string) []string {
	if i == len(s)-1 {
		arrays = append(arrays, string(s))
		return arrays
	}
	for temp := i; temp < len(s); temp++ {
		s[i], s[temp] = s[temp], s[i]
		arrays = permutation(s, i+1, arrays)
		s[i], s[temp] = s[temp], s[i]
	}
	return arrays
}

/**
给定两个字符串 s1 和 s2，写一个函数来判断 s2 是否包含 s1 的排列。
换句话说，第一个字符串的排列之一是第二个字符串的子串。
示例1:
输入: s1 = "ab" s2 = "eidbaooo"
输出: True
解释: s2 包含 s1 的排列之一 ("ba").
示例2:
输入: s1= "ab" s2 = "eidboaoo"
输出: FalseRecursionPermutation
*/
//我们不用真的去算出s1的全排列，只要统计字符出现的次数即可。可以使用一个哈希表配上双指针来做
func CheckInclusion(s1 string, s2 string) bool {
	if len(s1) > len(s2) {
		return false
	}
	m := make(map[rune]byte, 0)
	for index, v := range s1 {
		m[v]++
		m[rune(s2[index])]--
	}
	if allZero(m) {
		return true
	}
	for temp := len(s1); temp < len(s2); temp++ {
		m[rune(s2[temp])]--
		m[rune(s2[temp-len(s1)])]++
		if allZero(m) {
			return true
		}
	}
	return false
}
func allZero(m map[rune]byte) bool {
	for _, v := range m {
		if v != 0 {
			return false
		}
	}
	return true
}

/**
给定两个以字符串形式表示的非负整数 num1 和 num2，返回 num1 和 num2 的乘积，它们的乘积也表示为字符串形式。
示例 1:
输入: num1 = "2", num2 = "3"
输出: "6"
示例 2:
输入: num1 = "123", num2 = "456"
输出: "56088"
说明：
num1 和 num2 的长度小于110。
num1 和 num2 只包含数字 0-9。
num1 和 num2 均不以零开头，除非是数字 0 本身。
不能使用任何标准库的大数类型（比如 BigInteger）或直接将输入转换为整数来处理。
*/

func Multiply(num1 string, num2 string) string {
	m := make(map[int]int, 0)
	value := 0
	// 映射0 到9 的ASCII码
	for i := 48; i < 58; i++ {
		m[i] = value
		value++
	}
	n1 := len(num1)
	n2 := len(num2)
	result := make([]int, n1+n2)
	rss := make([][]int, 0, 0)
	index := n1 + n2 - 1
	for t := len(num1) - 1; t >= 0; t-- {
		var j, y int = 0, 0
		for temp := len(num2) - 1; temp >= 0; temp-- {
			k := m[int(num1[t])] * m[int(num2[temp])]
			y = (k + j) % 10
			j = (k + j) / 10
			result[index] = y
			index--
			if temp == 0 { //如果到最高位了，就把商赋值
				result[index] = j
			}
		}
		index = index + len(num2) - 1
		rss = append(rss, result)
		result = make([]int, n1+n2)
	}
	fmt.Println(rss)
	index = n1 + n2 - 1
	var sum int = 0
	var j, y int = 0, 0
	for m := index; m >= 0; m-- {
		for i := 0; i < len(rss); i++ {
			sum = sum + rss[i][m]
		}
		y = sum % 10
		if y+j < 10 {
			result[m] = y + j
			j = sum / 10
		} else {
			result[m] = (y + j) % 10
			j = sum/10 + (y+j)/10
		}
		sum = 0
	}
	for index, v := range result {
		if index == len(result)-1 && v == 0 {
			return "0"
		}
		if v == 0 {
			continue
		}
		result = result[index:]
		break
	}
	s := make([]string, len(result), len(result))
	for index, v := range result {
		fmt.Println(v)
		s[index] = string(strconv.Itoa(int(v)))
	}
	fmt.Println(result)
	fmt.Println(s)
	return strings.Join(s, "")
}

func LetterCasePermutation(S string) []string {
	if len(S) == 0 {
		return nil
	}
	temps := ""
	position := 0
	sArray := make([]string, 0, 0)
	sArray = dfs(temps, S, sArray, position)
	return sArray
}

// 65-90 大写
// 97-122 小写
func dfs(temps string, s string, sArray []string, position int) []string {
	if position == len(s) {
		sArray = append(sArray, temps)
		return sArray
	}
	//不是字母
	if s[position] < 65 || s[position] > 122 || (s[position] > 90 && s[position] < 97) {
		sArray = dfs(temps+string(rune(s[position])), s, sArray, position+1)
	} else {
		sArray = dfs(temps+strings.ToLower(string(rune(s[position]))), s, sArray, position+1)
		sArray = dfs(temps+strings.ToUpper(string(rune(s[position]))), s, sArray, position+1)
	}
	return sArray
}



// Split replaces strings.Split.
// strings.Split has a giant pit because strings.Split ("", ",") will return a slice with an empty string.
func Split(s, sep string) []string {
	if s == "" {
		return []string{}
	}
	return strings.Split(s, sep)
}

// JoinStrSkipEmpty concatenates multiple strings to a single string with the specified separator and skips the empty
// string.
func JoinStrSkipEmpty(sep string, s ...string) string {
	var buf bytes.Buffer
	for _, v := range s {
		if v == "" {
			continue
		}
		if buf.Len() > 0 {
			buf.WriteString(sep)
		}
		buf.WriteString(v)
	}
	return buf.String()
}

// JoinStr concatenates multiple strings to a single string with the specified separator.
// Note that JoinStr doesn't skip the empty string.
func JoinStr(sep string, s ...string) string {
	var buf bytes.Buffer
	for i, v := range s {
		if i != 0 {
			buf.WriteString(sep)
		}
		buf.WriteString(v)
	}
	return buf.String()
}

// ReverseStr reverses the specified string without modifying the original string.
func ReverseStr(s string) string {
	rs := []rune(s)
	var r []rune
	for i := len(rs) - 1; i >= 0; i-- {
		r = append(r, rs[i])
	}
	return string(r)
}

// GetAlphanumericNumByASCII gets the alphanumeric number based on the ASCII code value.
// Note that this function has a better performance than GetAlphanumericNumByRegExp, so this function is recommended.
func GetAlphanumericNumByASCII(s string) int {
	num := int(0)
	for i := 0; i < len(s); i++ {
		switch {
		case 48 <= s[i] && s[i] <= 57: 	// digits
			fallthrough
		case 65 <= s[i] && s[i] <= 90: 	// uppercase letters
			fallthrough
		case 97 <= s[i] && s[i] <= 122: // lowercase letters
			num++
		default:
		}
	}
	return num
}

// GetAlphanumericNumByASCIIV2 gets the alphanumeric number based on the ASCII code value.
// Because range by rune so the performance is worse than GetAlphanumericNumByASCII.
func GetAlphanumericNumByASCIIV2(s string) int {
	num := int(0)
	for _, c := range s {
		switch {
		case '0' <= c && c <= '9':
			fallthrough
		case 'a' <= c && c <= 'z':
			fallthrough
		case 'A' <= c && c <= 'Z':
			num++
		default:
		}
	}
	return num
}

// GetAlphanumericNumByRegExp gets the alphanumeric number based on regular expression.
// Note that this function has a poor performance when compared to GetAlphanumericNumByASCII,
// so the GetAlphanumericNumByASCII is recommended.
func GetAlphanumericNumByRegExp(s string) int {
	rNum := regexp.MustCompile(`\d`)
	rLetter := regexp.MustCompile("[a-zA-Z]")
	return len(rNum.FindAllString(s, -1)) + len(rLetter.FindAllString(s, -1))
}
