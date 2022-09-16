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
package maputil

import (
	"reflect"
	"strings"
)

// IsEmpty ...
func IsEmpty(mp map[string]string) bool {
	return len(mp) == 0
}

// HasKey ...
func HasKey(mp map[string]string, key string) bool {
	if _, ok := mp[key]; ok {
		return true
	}
	return false
}

// Value ...
func Value(mp map[string]string, key string) string {
	if HasKey(mp, key) {
		return mp[key]
	}
	return ""
}

// HasValue ...
func HasValue(mp map[string]string, value string) bool {
	for _, v := range mp {
		if v == value {
			return true
		}
	}
	return false
}

// Keys ...
func Keys(mp map[string]string) []string {
	ks := make([]string, 0, len(mp))
	for k, _ := range mp {
		ks = append(ks, k)
	}
	return ks
}

// Values ...
func Values(mp map[string]string) []string {
	vs := make([]string, 0, len(mp))
	for _, v := range mp {
		vs = append(vs, v)
	}
	return vs
}

// KeyToLower convert keys to lower case.
func KeyToLower(mp map[string]string) map[string]string {
	nmp := make(map[string]string, len(mp))
	for k, v := range mp {
		k = strings.ToLower(k)
		nmp[k] = v
	}
	return nmp
}

// Merge src 会覆盖 dst
func Merge(src, dst map[string]string) map[string]string {
	for k, v := range src {
		dst[k] = v
	}
	return dst
}

// 结构体转map
func Struct2Map(obj interface{}) map[string]interface{} {
	t := reflect.TypeOf(obj)
	v := reflect.ValueOf(obj)

	var data = make(map[string]interface{})
	for i := 0; i < t.NumField(); i++ {
		data[t.Field(i).Name] = v.Field(i).Interface()
	}
	return data
}

