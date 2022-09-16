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
package arrayutil

import (
	"math"
	"strconv"
	"strings"
)

// InSlice 判断字符串是否存在
func InSlice(value string, ss []string) bool {
	for _, v := range ss {
		if v == value {
			return true
		}
	}
	return false
}

// IsEmpty 判断Slice是否为空
func IsEmpty(ss []string) bool {
	if len(ss) == 0 {
		return true
	}
	return false
}

// Implode 别名 strings.Join
func Implode(sep string, ss ...string) string {
	return strings.Join(ss, sep)
}

// Explode 别名 strings.Split
func Explode(sep string, s string) []string {
	return strings.Split(s, sep)
}

// Unique slice去重
func Unique(ss []string) []string {
	l := len(ss)
	// 无法保障顺序
	m := make(map[string]bool)
	for i := 0; i < l; i++ {
		m[ss[i]] = true
	}

	nl := len(m)
	n := make([]string, nl)

	i := 0
	for v := range m {
		n[i] = v
		i++
	}

	return n
}

// Merge slice合并 - 不去重
func Merge(slice1, slice2 []string) []string {
	n := make([]string, len(slice1)+len(slice2))
	copy(n, slice1)
	copy(n[len(slice1):], slice2)
	return n
}

// Intersect slice交集
func Intersect(slice1, slice2 []string) []string {
	m := make(map[string]int)
	n := make([]string, 0)
	for _, v := range slice1 {
		m[v]++
	}
	for _, v := range slice2 {
		times, _ := m[v]
		if times == 1 {
			n = append(n, v)
		}
	}
	return n
}

// Union slice并集
func Union(slice1, slice2 []string) []string {
	m := make(map[string]int)
	for _, v := range slice1 {
		m[v]++
	}
	for _, v := range slice2 {
		times, _ := m[v]
		if times == 0 {
			slice1 = append(slice1, v)
		}
	}
	return slice1
}

// Difference slice差集 - 属于slice1，不属于slice2
func Difference(slice1, slice2 []string) []string {
	m := make(map[string]int)
	n := make([]string, 0)
	inter := Intersect(slice1, slice2)
	for _, v := range inter {
		m[v]++
	}

	for _, value := range slice1 {
		times, _ := m[value]
		if times == 0 {
			n = append(n, value)
		}
	}
	return n
}

// IntersectUint64 slice交集
func IntersectUint64(slice1, slice2 []uint64) []uint64 {
	m := make(map[uint64]int)
	n := make([]uint64, 0)
	for _, v := range slice1 {
		m[v]++
	}
	for _, v := range slice2 {
		times, _ := m[v]
		if times == 1 {
			n = append(n, v)
		}
	}
	return n
}

// UnionUint64 slice并集
func UnionUint64(slice1, slice2 []uint64) []uint64 {
	m := make(map[uint64]int)
	for _, v := range slice1 {
		m[v]++
	}
	for _, v := range slice2 {
		times, _ := m[v]
		if times == 0 {
			slice1 = append(slice1, v)
		}
	}
	return slice1
}

// DifferenceUint64 slice差集 - 属于slice1，不属于slice2
func DifferenceUint64(slice1, slice2 []uint64) []uint64 {
	m := make(map[uint64]int)
	n := make([]uint64, 0)
	inter := IntersectUint64(slice1, slice2)
	for _, v := range inter {
		m[v]++
	}

	for _, value := range slice1 {
		times, _ := m[value]
		if times == 0 {
			n = append(n, value)
		}
	}
	return n
}

// IntersectInterface slice交集
func IntersectInterface(slice1, slice2 []interface{}) []interface{} {
	m := make(map[interface{}]int)
	n := make([]interface{}, 0)
	for _, v := range slice1 {
		m[v]++
	}
	for _, v := range slice2 {
		times, _ := m[v]
		if times == 1 {
			n = append(n, v)
		}
	}
	return n
}

// UnionInterface slice并集
func UnionInterface(slice1, slice2 []interface{}) []interface{} {
	m := make(map[interface{}]int)
	for _, v := range slice1 {
		m[v]++
	}
	for _, v := range slice2 {
		times, _ := m[v]
		if times == 0 {
			slice1 = append(slice1, v)
		}
	}
	return slice1
}

// DifferenceInterface slice差集 - 属于slice1，不属于slice2
func DifferenceInterface(slice1, slice2 []interface{}) []interface{} {
	m := make(map[interface{}]int)
	n := make([]interface{}, 0)
	inter := IntersectInterface(slice1, slice2)
	for _, v := range inter {
		m[v]++
	}

	for _, value := range slice1 {
		times, _ := m[value]
		if times == 0 {
			n = append(n, value)
		}
	}
	return n
}



// 合并数组
func MergeArray(dest []interface{}, src []interface{}) (result []interface{}) {
	result = make([]interface{}, len(dest)+len(src))
	copy(result, dest)
	copy(result[len(dest):], src)
	return
}

// 删除数组
func DeleteArray(src []interface{}, index int) (result []interface{}) {
	result = append(src[:index], src[(index+1):]...)
	return
}

// []string => []int
func ArrayStr2Int(data []string) []int {
	var (
		arr = make([]int, 0, len(data))
	)
	if len(data) == 0 {
		return arr
	}
	for i, _ := range data {
		var num, _ = strconv.Atoi(data[i])
		arr = append(arr, num)
	}
	return arr
}

// []int => []string
func ArrayInt2Str(data []int) []string {
	var (
		arr = make([]string, 0, len(data))
	)
	if len(data) == 0 {
		return arr
	}
	for i, _ := range data {
		arr = append(arr, strconv.Itoa(data[i]))
	}
	return arr
}

// str[TrimSpace] in string list
func TrimSpaceStrInArray(str string, data []string) bool {
	if len(data) > 0 {
		for _, row := range data {
			if str == strings.TrimSpace(row) {
				return true
			}
		}
	}
	return false
}

// str in string list
func StrInArray(str string, data []string) bool {
	if len(data) > 0 {
		for _, row := range data {
			if str == row {
				return true
			}
		}
	}
	return false
}

// str in int list
func IntInArray(num int, data []int) bool {
	if len(data) > 0 {
		for _, row := range data {
			if num == row {
				return true
			}
		}
	}
	return false
}


var defSep = "_"

/**
笛卡尔组合
测试用例
cart := [][]string{
	{"a1", "a2"},
	{"b1", "b2"},
}
CartCombine(cart)
 */
func CartCombine(data [][]string, sep string) []string {
	var _sep = defSep
	if sep != "" {
		_sep = sep
	}
	var _r []string
	lens := func(i int) int { return len(data[i]) }
	for i := make([]int, len(data)); i[0] < lens(0); next(i, lens) {
		var r []string
		for j, k := range i {
			r = append(r, data[j][k])
		}
		_r = append(_r, strings.Join(r, _sep))
	}
	return _r
}

func next(i []int, lens func(i int) int) {
	for j := len(i) - 1; j >= 0; j-- {
		i[j]++
		if j == 0 || i[j] < lens(j) {
			return
		}
		i[j] = 0
	}
}


const (
	TOTAL_PAGE_FIELD   = "total_page"
	PAGE_FIELD         = "page"
	ROWS_FIELD         = "rows"
	TOTAL_RECORD_FIELD = "total_record"
)

/**
	page 当前页
	listRow 每页行数
    total   数据总数

	分页数据填充 返回
		=> map[string]int
			["total_page"] => 1,
			["page"] => 1,
			["rows"] => 20,
			["total_record"] => 3,
*/
func CommaPaginator(page int, listRow int, total int) map[string]int {
	totalpages := int(math.Ceil(float64(total) / float64(listRow)))
	if page <= 0 {
		page = 1
	}
	paginator := make(map[string]int)
	paginator[TOTAL_PAGE_FIELD] = totalpages
	paginator[PAGE_FIELD] = page
	paginator[ROWS_FIELD] = listRow
	paginator[TOTAL_RECORD_FIELD] = total
	return paginator
}


