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
package mask

import (
	"strings"
)

// Mask 自定义字符脱敏 保留前f位和最后e位
func Mask(s string, f, e int) string {
	l := len(s)
	if f+e >= l || f < 0 || e < 0 {
		return ""
	}
	return s[0:f] + strings.Repeat("*", l-f-e) + s[l-e:]
}

// Left 保留前f位
func Left(s string, f int) string {
	return Mask(s, f, 0)
}

// Right 保留后e位
func Right(s string, e int) string {
	return Mask(s, 0, e)
}

// First 取值前f位
func First(s string, f int) string {
	l := len(s)
	if l <= f || f < 0 {
		return s
	}
	return s[0:f]
}

// Last 取值后e位
func Last(s string, e int) string {
	l := len(s)
	if l <= e || e < 0 {
		return s
	}
	return s[l-e:]
}

// LastFour 后四位
func LastFour(s string) string {
	return Last(s, 4)
}

// IdCard 身份证号脱敏
func IdCard(s string) string {
	if len(s) != 18 {
		return ""
	}
	return s[0:4] + " **** **** " + s[len(s)-4:]
}

// IdCardStrict 严格身份证号脱敏
func IdCardStrict(s string) string {
	if len(s) != 18 {
		return ""
	}
	return s[0:1] + "*** **** **** ***" + s[len(s)-1:]
}

// Mobile 手机号脱敏
func Mobile(s string) string {
	return s[0:3] + "****" + s[len(s)-4:]
}

// ChineseName 中文姓名脱敏
func ChineseName(s string) string {
	r := []rune(s)
	l := len(r)
	if l == 2 {
		return "*" + string(r[1:])
	} else if l == 3 {
		return "*" + string(r[l-2:])
	} else if l == 4 {
		return "**" + string(r[l-2:])
	} else if l > 4 {
		return string(r[:1]) + strings.Repeat("*", l-3) + string(r[l-2:])
	}
	return "**"
}

// Email 邮箱脱敏
func Email(s string) string {
	ss := strings.Split(s, "@")
	l := len(ss[0])
	if l <= 1 {
		return "*@" + ss[1]
	}
	r := []rune(s)
	return string(r[0:1]) + strings.Repeat("*", l-1) + "@" + ss[1]
}
