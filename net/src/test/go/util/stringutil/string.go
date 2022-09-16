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
