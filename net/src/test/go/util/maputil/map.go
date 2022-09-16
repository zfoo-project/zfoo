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
	"fmt"
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


//
// Part 1: convert a slice or array to the specified type map set strictly.
// Note that the the element type of slice or array need to be equal to map key type.
// For example, []uint64{1, 2, 3} can be converted to map[uint64]struct{}{1:struct{}, 2:struct{},
// 3:struct{}} by calling ToUint64MapSetStrict() but can't be converted to map[string]struct{}{"1":struct{},
// "2":struct{}, "3":struct{}}.
//

// ToBoolMapSetStrict converts a slice or array to map[bool]struct{} strictly.
func ToBoolMapSetStrict(i interface{}) map[bool]struct{} {
	m, _ := ToBoolMapSetStrictE(i)
	return m
}

// ToBoolMapSetStrictE converts a slice or array to map[bool]struct{} with error.
func ToBoolMapSetStrictE(i interface{}) (map[bool]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[bool]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[bool]struct{}", m, m)
}

// ToIntMapSetStrict converts a slice or array to map[int]struct{}.
func ToIntMapSetStrict(i interface{}) map[int]struct{} {
	m, _ := ToIntMapSetStrictE(i)
	return m
}

// ToIntMapSetStrictE converts a slice or array to map[int]struct{} with error.
func ToIntMapSetStrictE(i interface{}) (map[int]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[int]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v isn't map[int]struct{}", m, m)
}

// ToInt8MapSetStrict converts a slice or array to map[int8]struct{}.
func ToInt8MapSetStrict(i interface{}) map[int8]struct{} {
	m, _ := ToInt8MapSetStrictE(i)
	return m
}

// ToInt8MapSetStrictE converts a slice or array to map[int8]struct{} with error.
func ToInt8MapSetStrictE(i interface{}) (map[int8]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[int8]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[int8]struct{}", m, m)
}

// ToInt16MapSetStrict converts a slice or array to map[int16]struct{}.
func ToInt16MapSetStrict(i interface{}) map[int16]struct{} {
	m, _ := ToInt16MapSetStrictE(i)
	return m
}

// ToInt16MapSetStrictE converts a slice or array to map[int16]struct{} with error.
func ToInt16MapSetStrictE(i interface{}) (map[int16]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[int16]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[int16]struct{}", m, m)
}

// ToInt32MapSetStrict converts a slice or array to map[int32]struct{}.
func ToInt32MapSetStrict(i interface{}) map[int32]struct{} {
	m, _ := ToInt32MapSetStrictE(i)
	return m
}

// ToInt32MapSetStrictE converts a slice or array to map[int32]struct{} with error.
func ToInt32MapSetStrictE(i interface{}) (map[int32]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[int32]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[int32]struct{}", m, m)
}

// ToInt64MapSetStrict converts a slice or array to map[int64]struct{}.
func ToInt64MapSetStrict(i interface{}) map[int64]struct{} {
	m, _ := ToInt64MapSetStrictE(i)
	return m
}

// ToInt64MapSetStrictE converts a slice or array to map[int64]struct{} with error.
func ToInt64MapSetStrictE(i interface{}) (map[int64]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[int64]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[int64]struct{}", m, m)
}

// ToUintMapSetStrict converts a slice or array to map[uint]struct{}.
func ToUintMapSetStrict(i interface{}) map[uint]struct{} {
	m, _ := ToUintMapSetStrictE(i)
	return m
}

// ToUintMapSetStrictE converts a slice or array to map[uint]struct{} with error.
func ToUintMapSetStrictE(i interface{}) (map[uint]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[uint]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[uint8]struct{}", m, m)
}

// ToUint8MapSetStrict converts a slice or array to map[uint8]struct{}.
func ToUint8MapSetStrict(i interface{}) map[uint8]struct{} {
	m, _ := ToUint8MapSetStrictE(i)
	return m
}

// ToUint8MapSetStrictE converts a slice or array to map[uint8]struct{} with error.
func ToUint8MapSetStrictE(i interface{}) (map[uint8]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[uint8]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[uint8]struct{}", m, m)
}

// ToUint16MapSetStrict converts a slice or array to map[uint16]struct{}.
func ToUint16MapSetStrict(i interface{}) map[uint16]struct{} {
	m, _ := ToUint16MapSetStrictE(i)
	return m
}

// ToUint16MapSetStrictE converts a slice or array to map[uint16]struct{} with error.
func ToUint16MapSetStrictE(i interface{}) (map[uint16]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[uint16]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[uint16]struct{}", m, m)
}

// ToUint32MapSetStrict converts a slice or array to map[uint32]struct{}.
func ToUint32MapSetStrict(i interface{}) map[uint32]struct{} {
	m, _ := ToUint32MapSetStrictE(i)
	return m
}

// ToUint32MapSetStrictE converts a slice or array to map[uint32]struct{} with error.
func ToUint32MapSetStrictE(i interface{}) (map[uint32]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[uint32]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[uint32]struct{}", m, m)
}

// ToUint64MapSetStrict converts a slice or array to map[uint64]struct{}.
func ToUint64MapSetStrict(i interface{}) map[uint64]struct{} {
	m, _ := ToUint64MapSetStrictE(i)
	return m
}

// ToUint64MapSetStrictE converts a slice or array to map[uint64]struct{} with error.
func ToUint64MapSetStrictE(i interface{}) (map[uint64]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[uint64]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[uint64]struct{}", m, m)
}

// ToStrMapSetStrict converts a slice or array to map[string]struct{}.
func ToStrMapSetStrict(i interface{}) map[string]struct{} {
	m, _ := ToStrMapSetStrictE(i)
	return m
}

// ToStrMapSetStrictE converts a slice or array to map[string]struct{} with error.
func ToStrMapSetStrictE(i interface{}) (map[string]struct{}, error) {
	m, err := ToMapSetStrictE(i)
	if err != nil {
		return nil, err
	}
	if v, ok := m.(map[string]struct{}); ok {
		return v, nil
	}
	return nil, fmt.Errorf("convert success but the type %T of result %#v of isn't map[string]struct{}", m, m)
}

// ToMapSetStrictE converts a slice or array to map set with error strictly.
// The result of map key type is equal to the type input element.
func ToMapSetStrictE(i interface{}) (interface{}, error) {
	// check params.
	if i == nil {
		return nil, fmt.Errorf("unable to converts nil to map[interface{}]struct{}")
	}
	t := reflect.TypeOf(i)
	kind := t.Kind()
	if kind != reflect.Slice && kind != reflect.Array {
		return nil, fmt.Errorf("the type %T of input %#v isn't a slice or array", i, i)
	}
	// execute the convert.
	v := reflect.ValueOf(i)
	mT := reflect.MapOf(t.Elem(), reflect.TypeOf(struct{}{}))
	mV := reflect.MakeMapWithSize(mT, v.Len())
	for j := 0; j < v.Len(); j++ {
		mV.SetMapIndex(v.Index(j), reflect.ValueOf(struct{}{}))
	}
	return mV.Interface(), nil
}
