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
package convert

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"strconv"
	"time"
)

// AnyToString ...
func AnyToString(i interface{}) string {
	var s string
	switch v := i.(type) {
	case nil:
		s = ""
	case int:
		s = strconv.Itoa(v)
	case int8:
		s = strconv.Itoa(int(v))
	case int16:
		s = strconv.Itoa(int(v))
	case int32: // same as `rune`
		s = strconv.Itoa(int(v))
	case int64:
		s = strconv.Itoa(int(v))
	case uint:
		s = strconv.FormatUint(uint64(v), 10)
	case uint8:
		s = strconv.FormatUint(uint64(v), 10)
	case uint16:
		s = strconv.FormatUint(uint64(v), 10)
	case uint32:
		s = strconv.FormatUint(uint64(v), 10)
	case uint64:
		s = strconv.FormatUint(v, 10)
	case float32:
		s = strconv.FormatFloat(float64(v), 'f', -1, 32)
	case float64:
		s = strconv.FormatFloat(v, 'f', -1, 64)
	case bool:
		s = strconv.FormatBool(v)
	case string:
		s = v
	case []byte:
		s = string(v)
	case time.Duration:
		s = v.String()
	case json.Number:
		s = v.String()
	default:
		s = fmt.Sprint(v)
	}
	return s
}

// IntToString int => string
func IntToString(i int) string {
	return strconv.Itoa(i)
}

// Uint64ToString uint64 => string
func Uint64ToString(i uint64) string {
	return strconv.FormatUint(i, 10)
}

// Float64ToString float64 => string
func Float64ToString(f float64) string {
	return strconv.FormatFloat(f, 'f', -1, 64)
}

// Float32ToString float32 => string
func Float32ToString(f float32) string {
	return strconv.FormatFloat(float64(f), 'f', -1, 32)
}

// StringToFloat64 string => float64
func StringToFloat64(s string) float64 {
	f, _ := strconv.ParseFloat(s, 64)
	return f
}

// StringToFloat32 string => float32
func StringToFloat32(s string) float32 {
	f64, _ := strconv.ParseFloat(s, 32)
	return float32(f64)
}

// StringToInt string => int
func StringToInt(s string) int {
	i, _ := strconv.Atoi(s)
	return i
}

// StringToInt32 string => int32
func StringToInt32(s string) int32 {
	return int32(StringToInt64(s))
}

// StringToInt64 string => int64
func StringToInt64(s string) int64 {
	i, _ := strconv.ParseInt(s, 10, 64)
	return i
}

// StringToUint64 string => uint64
func StringToUint64(s string) uint64 {
	i, _ := strconv.ParseUint(s, 10, 64)
	return i
}

// IntToUint int => uint
func IntToUint(i int) uint {
	return uint(i)
}

// UintToInt uint => int
func UintToInt(i uint) int {
	return int(i)
}

// JsonNumberToInt json.Number => int
func JsonNumberToInt(n json.Number) int {
	i64, _ := n.Int64()
	return int(i64)
}

// MapToJson map => json
func MapToJson(m map[string]string) (string, error) {
	b, e := json.Marshal(m)
	if e != nil {
		return "", e
	}
	return string(b), nil
}

// JsonToMap json => map
func JsonToMap(s string) (map[string]string, error) {
	m := make(map[string]string)
	err := json.Unmarshal([]byte(s), &m)
	if err != nil {
		return nil, err
	}
	return m, nil
}

// Base64Encode base64 编码
func Base64Encode(src []byte) string {
	return base64.StdEncoding.EncodeToString(src)
}

// Base64Decode base64 解码
func Base64Decode(src string) ([]byte, error) {
	return base64.StdEncoding.DecodeString(src)
}

// 四舍五入
func Float64Rand(v float64, dig int) float64 {
	cDig := strconv.Itoa(dig)
	val := fmt.Sprintf("%0."+cDig+"f", v)
	return StringToFloat64(val)
}

// 浮点数串化(左边是整数位置,右边是小数位,dig参数控制)
func FloatToFDig(floVal float64, dig int) string {
	return fmt.Sprintf("%10."+strconv.Itoa(dig)+"f", floVal) //十位整数，8位小数
}

