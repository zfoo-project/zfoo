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
	"fmt"
	"math"
	"reflect"
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


// -----------------------------------------------------------------------------------------------
/**
求最大子序列和 （就是说子序列加起来和最大）
*/
func FindMaxSeqSum(array []int) int {
	SeqSum := make([]int, 0) // 存储子序列和
	// 初始子序列和为 数组下标为0的值
	SeqSum = append(SeqSum, array[0])
	for i := 1; i < len(array); i++ {
		if array[i] > SeqSum[i-1]+array[i] {
			SeqSum = append(SeqSum, array[i])
		} else {
			SeqSum = append(SeqSum, SeqSum[i-1]+array[i])
		}
	}
	max := SeqSum[0]
	for j := 1; j < len(SeqSum); j++ {
		if SeqSum[j] > SeqSum[j-1] {
			max = SeqSum[j]
		}
	}
	//fmt.Println(max)
	return max
}

/**
二分查找法
查找某个值在有序数组中是否存在
*/
func BinaryFindOrderArray(array []int, value int) bool {
	head := 0
	tail := len(array) - 1
	for head <= tail {
		mid := (head + tail) >> 1
		if array[mid] == value {
			return true
		} else if array[mid] > value {
			tail = mid - 1
		} else {
			head = mid + 1
		}
	}
	return false
}

/**
数组是有序的
在数组中查找匹配value的第一个下标位置
*/
func BinaryFindFirstOrderArray(array []int, value int) int {
	head := 0
	height := len(array) - 1
	for head <= height {
		mid := head + (height-head)>>1
		if value > array[mid] {
			head = mid + 1
		} else if value < array[mid] {
			height = mid - 1
		} else {
			if mid == 0 || array[mid-1] != value {
				return mid
			}
			height = mid - 1
		}
	}
	return -1
}

/**
查找有序数组中匹配目标的最后一个位置的下标
*/
func BinaryFindTailOrderArray(array []int, value int) int {
	head := 0
	tail := len(array) - 1
	for head <= tail {
		mid := head + (tail-head)>>1
		if array[mid] > value {
			tail = mid - 1
		} else if array[mid] < value {
			head = mid + 1
		} else {
			if mid == len(array)-1 || array[mid+1] != value {
				return mid
			}
			head = mid + 1
		}
	}

	return -1
}

/**
给定两个有序整数数组 nums1 和 nums2，将 nums2 合并到 nums1 中，使得 num1 成为一个有序数组。
说明:
初始化 nums1 和 nums2 的元素数量分别为 m 和 n。
你可以假设 nums1 有足够的空间（空间大小大于或等于 m + n）来保存 nums2 中的元素。
示例:
输入:
nums1 = [1,2,3,0,0,0], m = 3
nums2 = [2,5,6],       n = 3
输出: [1,2,2,3,5,6]
*/
func MergeTwoArray(nums1 []int, m int, nums2 []int, n int) {
	if n > 0 {
		for i := 0; i < n; i++ {
			nums1[m+i] = nums2[i]
		}
	}
	lindex := 0
	rindex := m
	for lindex < m && len(nums1) > rindex {
		for lindex < m && nums1[lindex] > nums1[rindex] {
			nums1[lindex], nums1[rindex] = nums1[rindex], nums1[lindex]
			//使右边重新变得有序
			for (rindex + 1) < (m + n) {
				if nums1[rindex] < nums1[rindex+1] {
					break
				}
				nums1[rindex], nums1[rindex+1] = nums1[rindex+1], nums1[rindex]
				rindex++
			}
			rindex = m
		}
		lindex++
	}
}

/**
给定一个整数数组 nums ，找到一个具有最大和的连续子数组（子数组最少包含一个元素），返回其最大和。
示例:
输入: [-2,1,-3,4,-1,2,1,-5,4],
输出: 6
解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。
进阶:
如果你已经实现复杂度为 O(n) 的解法，尝试使用更为精妙的分治法求解。
*/
/**
 * 定义状态：
 * dp[i] ： 表示以 nums[i] 结尾的连续子数组的最大和
 * <p>
 * 状态转移方程：
 * dp[i] = max{num[i],dp[i-1] + num[i]}
 *
 * @param nums
 * @return
 */
func maxSubArray(nums []int) int {
	if len(nums) == 0 {
		return 0
	}
	dp := make([]int, len(nums))
	for index, v := range nums {
		if index == 0 {
			dp[index] = v
		} else {
			if dp[index-1]+v > v {
				dp[index] = dp[index-1] + v
			} else {
				dp[index] = v
			}
		}
	}
	max := dp[0]
	for _, v := range dp {
		if v > max {
			max = v
		}
	}
	return max
}

/**
给定一个数组，它的第 i 个元素是一支给定股票第 i 天的价格。
设计一个算法来计算你所能获取的最大利润。你可以尽可能地完成更多的交易（多次买卖一支股票）。
注意：你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）。
示例 1:
输入: [7,1,5,3,6,4]
输出: 7
解释: 在第 2 天（股票价格 = 1）的时候买入，在第 3 天（股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4 。
     随后，在第 4 天（股票价格 = 3）的时候买入，在第 5 天（股票价格 = 6）的时候卖出, 这笔交易所能获得利润 = 6-3 = 3 。
示例 2:
输入: [1,2,3,4,5]
输出: 4
解释: 在第 1 天（股票价格 = 1）的时候买入，在第 5 天 （股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4 。
     注意你不能在第 1 天和第 2 天接连购买股票，之后再将它们卖出。
     因为这样属于同时参与了多笔交易，你必须在再次购买前出售掉之前的股票。
示例 3:
输入: [7,6,4,3,1]
输出: 0
解释: 在这种情况下, 没有交易完成, 所以最大利润为 0。
*/
func maxProfit(prices []int) int {

	return 0
}

/**
给定一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？找出所有满足条件且不重复的三元组。
注意：答案中不可以包含重复的三元组。
例如, 给定数组 nums = [-1, 0, 1, 2, -1, -4]，
满足要求的三元组集合为：
[
  [-1, 0, 1],
  [-1, -1, 2]
]
*/

/**
首先对数组从小到大排序，从一个数开始遍历，若该数大于0，后面的数不可能与其相加和为0，所以跳过；否则该数可能是满足要求的第一个数，这样可以转化为求后面数组中两数之和为该数的相反数的问题。
定义两个指针一前一后，若找到两数之和满足条件则加入到解集中；若大于和则后指针向前移动，反之则前指针向后移动，直到前指针大于等于后指针。这样遍历第一个数直到数组的倒数第3位。
注意再求和过程中首先判断该数字是否与前面数字重复，保证解集中没有重复解。
*/
func ThreeSum(nums []int) [][]int {
	result := make([][]int, 0, 0)
	if len(nums) < 3 {
		return result
	}
	bigHeapSort(nums)
	if nums[0] == 0 && nums[len(nums)-1] == 0 {
		result = append(result, []int{0, 0, 0})
		return result
	}
	for index, v := range nums {
		if v > 0 && index == 0 {
			return result
		}
		if index != 0 && v == nums[index-1] {
			continue
		}
		next, pre := index+1, len(nums)-1
		temp := make([]int, 3, 3)
		for next < pre {
			if nums[next]+nums[pre] > -v {
				pre--
			} else if nums[next]+nums[pre] < -v {
				next++
			} else {
				temp[0] = nums[index]
				temp[1] = nums[next]
				temp[2] = nums[pre]
				result = append(result, temp)
				temp = make([]int, 3, 3)
				t := next
				next++
				for next < pre && nums[next] == nums[t] {
					next++
				}
			}
		}
	}
	return result
}
func bigHeapSort(nums []int) {
	for i := len(nums)/2 - 1; i >= 0; i-- {
		createHeap(nums, i, len(nums))
	}
	for j := len(nums) - 1; j >= 0; j-- {
		nums[0], nums[j] = nums[j], nums[0]
		createHeap(nums, 0, j)
	}
}
func createHeap(nums []int, i int, length int) {
	left := i*2 + 1
	for left < length {
		if left+1 < length && nums[left+1] > nums[left] {
			left++
		}
		if nums[i] < nums[left] {
			nums[i], nums[left] = nums[left], nums[i]
		}
		i = left
		left = i*2 + 1
	}
}

/**
 给出集合 [1,2,3,…,n]，其所有元素共有 n! 种排列。
按大小顺序列出所有排列情况，并一一标记，当 n = 3 时, 所有排列如下：
"123"
"132"
"213"
"231"
"312"
"321"
 求n的全排列
*/
func GetAllPermutation(n int) [][]int {
	nums := make([]int, n)
	for i := 1; i <= n; i++ {
		nums[i-1] = i
	}
	result := make([][]int, 0, 0)
	result = permutation(nums, 0, result)
	return result
}
func permutation(nums []int, index int, result [][]int) [][]int {
	if index == len(nums)-1 {
		tempArray := make([]int, len(nums), len(nums))
		copy(tempArray, nums)
		result = append(result, tempArray)
		return result
	}
	for temp := index; temp < len(nums); temp++ {
		nums[index], nums[temp] = nums[temp], nums[index]
		result = permutation(nums, index+1, result)
		nums[index], nums[temp] = nums[temp], nums[index]
	}
	return result
}

func CombinationSum(candidates []int, target int) [][]int {
	result := make([][]int, 0)
	temp := make([]int, 0)
	backtrack(candidates, temp, 0, target, &result)
	return result
}

func backtrack(candidates, temp []int, start, target int, result *[][]int) {
	if target == 0 {
		t := make([]int, len(temp))
		copy(t, temp)
		*result = append(*result, t)
		return
	}
	for i := start; i < len(candidates); i++ {
		temp = append(temp, candidates[i])
		target = target - candidates[i]
		backtrack(candidates, temp, start+1, target, result)
		target = target + temp[len(temp)-1]
		temp = temp[:len(temp)-1]
	}

}


// ----------------------------------------------------------------------------------------------------------

//
// Part 1: unique a slice, e.g. input []int32{1, 2, 2, 3} and output is []int32{1, 2, 3}.
//

func UniqueIntSlice(src []int) []int {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]int)
	return v
}

func UniqueInt8Slice(src []int8) []int8 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]int8)
	return v
}

func UniqueInt16Slice(src []int16) []int16 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]int16)
	return v
}

func UniqueInt32Slice(src []int32) []int32 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]int32)
	return v
}

func UniqueInt64Slice(src []int64) []int64 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]int64)
	return v
}

func UniqueUintSlice(src []uint) []uint {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]uint)
	return v
}

func UniqueUint8Slice(src []uint8) []uint8 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]uint8)
	return v
}

func UniqueUint16Slice(src []uint16) []uint16 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]uint16)
	return v
}

func UniqueUint32Slice(src []uint32) []uint32 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]uint32)
	return v
}

func UniqueUint64Slice(src []uint64) []uint64 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]uint64)
	return v
}

func UniqueFloat32Slice(src []float32) []float32 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]float32)
	return v
}

func UniqueFloat64Slice(src []float64) []float64 {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]float64)
	return v
}

func UniqueStrSlice(src []string) []string {
	dst, _ := UniqueSliceE(src)
	v, _ := dst.([]string)
	return v
}

//
// Part 2: reverse a slice, e.g. input []int32{1, 2, 3} and output is []int32{3, 2, 1}.
//

func ReverseIntSlice(src []int) []int {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]int)
	return v
}

func ReverseInt8Slice(src []int8) []int8 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]int8)
	return v
}

func ReverseInt16Slice(src []int16) []int16 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]int16)
	return v
}

func ReverseInt32Slice(src []int32) []int32 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]int32)
	return v
}

func ReverseInt64Slice(src []int64) []int64 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]int64)
	return v
}

func ReverseUintSlice(src []uint) []uint {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]uint)
	return v
}

func ReverseUint8Slice(src []uint8) []uint8 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]uint8)
	return v
}

func ReverseUint16Slice(src []uint16) []uint16 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]uint16)
	return v
}

func ReverseUint32Slice(src []uint32) []uint32 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]uint32)
	return v
}

func ReverseUint64Slice(src []uint64) []uint64 {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]uint64)
	return v
}

func ReverseStrSlice(src []string) []string {
	dst, _ := ReverseSliceE(src)
	v, _ := dst.([]string)
	return v
}

//
// Part 3: sum a slice, e.g. input []int32{1, 2, 3} and output is 6.
//

// SumSlice calculates the sum of slice elements
func SumSlice(slice interface{}) float64 {
	v, _ := SumSliceE(slice)
	return v
}

//
// Part 4: determine whether the slice contains an element.
//

// IsContains checks whether slice or array contains the target element.
// Note that if the target element is a numeric literal, please specify its type explicitly, otherwise it defaults to int.
// For example you might call like IsContains([]int32{1,2,3}, int32(1)).
func IsContains(i interface{}, target interface{}) bool {
	if i == nil {
		return false
	}
	t := reflect.TypeOf(i)
	if t.Kind() != reflect.Slice && t.Kind() != reflect.Array {
		return false
	}
	v := reflect.ValueOf(i)
	for i := 0; i < v.Len(); i++ {
		if target == v.Index(i).Interface() {
			return true
		}
	}
	return false
}


//
// Part 6: CRUD(Create Read Update Delete) on slice by index.
//

func InsertIntSlice(src []int, index, value int) []int {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]int)
	return v
}

func InsertInt8Slice(src []int8, index int, value int8) []int8 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]int8)
	return v
}

func InsertInt16Slice(src []int, index int, value int16) []int16 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]int16)
	return v
}

func InsertInt32Slice(src []int, index int, value int32) []int32 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]int32)
	return v
}

func InsertInt64Slice(src []int, index int, value int64) []int64 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]int64)
	return v
}

func InsertUintSlice(src []int, index int, value uint) []uint {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]uint)
	return v
}

func InsertUint8Slice(src []int8, index int, value uint8) []uint8 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]uint8)
	return v
}

func InsertUint16Slice(src []int, index int, value uint16) []uint16 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]uint16)
	return v
}

func InsertUint32Slice(src []int, index int, value uint32) []uint32 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]uint32)
	return v
}

func InsertUint64Slice(src []int, index int, value uint64) []uint64 {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]uint64)
	return v
}

func InsertStrSlice(src []int, index int, value string) []string {
	tmp, _ := InsertSliceE(src, index, value)
	v, _ := tmp.([]string)
	return v
}

func UpdateIntSlice(src []int, index, value int) []int {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]int)
	return v
}

func UpdateInt8Slice(src []int8, index int, value int8) []int8 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]int8)
	return v
}

func UpdateInt16Slice(src []int, index int, value int16) []int16 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]int16)
	return v
}

func UpdateInt32Slice(src []int, index int, value int32) []int32 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]int32)
	return v
}

func UpdateInt64Slice(src []int, index int, value int64) []int64 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]int64)
	return v
}

func UpdateUintSlice(src []int, index int, value uint) []uint {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]uint)
	return v
}

func UpdateUint8Slice(src []int8, index int, value uint8) []uint8 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]uint8)
	return v
}

func UpdateUint16Slice(src []int, index int, value uint16) []uint16 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]uint16)
	return v
}

func UpdateUint32Slice(src []int, index int, value uint32) []uint32 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]uint32)
	return v
}

func UpdateUint64Slice(src []int, index int, value uint64) []uint64 {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]uint64)
	return v
}

func UpdateStrSlice(src []int, index int, value string) []string {
	tmp, _ := UpdateSliceE(src, index, value)
	v, _ := tmp.([]string)
	return v
}

func GetEleIndexesSlice(slice interface{}, value interface{}) []int {
	indexes, _ := GetEleIndexesSliceE(slice, value)
	return indexes
}

//
// Part 7: get the min or max element of a slice.
//

func MinIntSlice(sl []int) int {
	min, _ := MinSliceE(sl)
	v, _ := min.(int)
	return v
}

func MinInt8Slice(sl []int8) int8 {
	min, _ := MinSliceE(sl)
	v, _ := min.(int8)
	return v
}

func MinInt16Slice(sl []int16) int16 {
	min, _ := MinSliceE(sl)
	v, _ := min.(int16)
	return v
}

func MinInt32Slice(sl []int32) int32 {
	min, _ := MinSliceE(sl)
	v, _ := min.(int32)
	return v
}

func MinInt64Slice(sl []int64) int64 {
	min, _ := MinSliceE(sl)
	v, _ := min.(int64)
	return v
}

func MinUintSlice(sl []uint) uint {
	min, _ := MinSliceE(sl)
	v, _ := min.(uint)
	return v
}

func MinUint8Slice(sl []uint8) uint8 {
	min, _ := MinSliceE(sl)
	v, _ := min.(uint8)
	return v
}

func MinUint16Slice(sl []uint16) uint16 {
	min, _ := MinSliceE(sl)
	v, _ := min.(uint16)
	return v
}

func MinUint32Slice(sl []uint32) uint32 {
	min, _ := MinSliceE(sl)
	v, _ := min.(uint32)
	return v
}

func MinUint64Slice(sl []uint64) uint64 {
	min, _ := MinSliceE(sl)
	v, _ := min.(uint64)
	return v
}

func MinFloat32Slice(sl []float32) float32 {
	min, _ := MinSliceE(sl)
	v, _ := min.(float32)
	return v
}

func MinFloat64Slice(sl []float64) float64 {
	min, _ := MinSliceE(sl)
	v, _ := min.(float64)
	return v
}

func MaxIntSlice(sl []int) int {
	max, _ := MaxSliceE(sl)
	v, _ := max.(int)
	return v
}

func MaxInt8Slice(sl []int8) int8 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(int8)
	return v
}

func MaxInt16Slice(sl []int16) int16 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(int16)
	return v
}

func MaxInt32Slice(sl []int32) int32 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(int32)
	return v
}

func MaxInt64Slice(sl []int64) int64 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(int64)
	return v
}

func MaxUintSl(sl []uint) uint {
	max, _ := MaxSliceE(sl)
	v, _ := max.(uint)
	return v
}

func MaxUint8Slice(sl []uint8) uint8 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(uint8)
	return v
}

func MaxUint16Slice(sl []uint16) uint16 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(uint16)
	return v
}

func MaxUint32Slice(sl []uint32) uint32 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(uint32)
	return v
}

func MaxUint64Slice(sl []uint64) uint64 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(uint64)
	return v
}

func MaxFloat32Slice(sl []float32) float32 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(float32)
	return v
}

func MaxFloat64Slice(sl []float64) float64 {
	max, _ := MaxSliceE(sl)
	v, _ := max.(float64)
	return v
}

//
// Part 8: get a random element from a slice or array.
//

// GetRandomSliceElem get a random element from a slice or array.
// If the length of slice or array is zero it will panic.

//
// Part x: basic operating functions of slice.
//

// UniqueSliceE deletes repeated elements in a slice with error.
// Note that the original slice will not be modified.
func UniqueSliceE(slice interface{}) (interface{}, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	// unique the slice
	dst := reflect.MakeSlice(reflect.TypeOf(slice), 0, v.Len())
	m := make(map[interface{}]struct{})
	for i := 0; i < v.Len(); i++ {
		if _, ok := m[v.Index(i).Interface()]; !ok {
			dst = reflect.Append(dst, v.Index(i))
			m[v.Index(i).Interface()] = struct{}{}
		}
	}
	return dst.Interface(), nil
}

// ReverseSliceE reverses the specified slice without modifying the original slice.
func ReverseSliceE(slice interface{}) (interface{}, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	// reverse the slice
	dst := reflect.MakeSlice(reflect.TypeOf(slice), 0, v.Len())
	for i := v.Len() - 1; i >= 0; i-- {
		dst = reflect.Append(dst, v.Index(i))
	}
	return dst.Interface(), nil
}

// SumSliceE returns the sum of slice elements and an error if occurred.
func SumSliceE(slice interface{}) (float64, error) {
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return 0.0, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}

	var sum float64
	for i := 0; i < v.Len(); i++ {
		switch v := v.Index(i).Interface().(type) {
		case int:
			sum += float64(v)
		case int8:
			sum += float64(v)
		case int16:
			sum += float64(v)
		case int32:
			sum += float64(v)
		case int64:
			sum += float64(v)
		case uint:
			sum += float64(v)
		case uint8:
			sum += float64(v)
		case uint16:
			sum += float64(v)
		case uint32:
			sum += float64(v)
		case uint64:
			sum += float64(v)
		case float32:
			sum += float64(v)
		case float64:
			sum += v
		default:
			return 0.0, fmt.Errorf("the element %#v of slice type %T isn't numerical type", v, v)
		}
	}
	return sum, nil
}

// MinSliceE returns the smallest element of the slice and an error if occurred.
// If slice length is zero return the zero value of the element type.
func MinSliceE(slice interface{}) (interface{}, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	if v.Len() == 0 {
		return nil, nil
	}
	// get the min element
	min := v.Index(0).Interface()
	for i := 1; i < v.Len(); i++ {
		switch v := v.Index(i).Interface().(type) {
		case int:
			if v < min.(int) {
				min = v
			}
		case int8:
			if v < min.(int8) {
				min = v
			}
		case int16:
			if v < min.(int16) {
				min = v
			}
		case int32:
			if v < min.(int32) {
				min = v
			}
		case int64:
			if v < min.(int64) {
				min = v
			}
		case uint:
			if v < min.(uint) {
				min = v
			}
		case uint8:
			if v < min.(uint8) {
				min = v
			}
		case uint16:
			if v < min.(uint16) {
				min = v
			}
		case uint32:
			if v < min.(uint32) {
				min = v
			}
		case uint64:
			if v < min.(uint64) {
				min = v
			}
		case float32:
			if v < min.(float32) {
				min = v
			}
		case float64:
			if v < min.(float64) {
				min = v
			}
		default:
			return nil, fmt.Errorf("the element %#v of slice type %T isn't numerical type", v, v)
		}
	}
	return min, nil
}

// MaxSliceE returns the largest element of the slice and an error if occurred.
// If slice length is zero return the zero value of the element type.
func MaxSliceE(slice interface{}) (interface{}, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	if v.Len() == 0 {
		return nil, nil
	}
	// get the max element.
	max := v.Index(0).Interface()
	for i := 1; i < v.Len(); i++ {
		switch v := v.Index(i).Interface().(type) {
		case int:
			if v > max.(int) {
				max = v
			}
		case int8:
			if v > max.(int8) {
				max = v
			}
		case int16:
			if v > max.(int16) {
				max = v
			}
		case int32:
			if v > max.(int32) {
				max = v
			}
		case int64:
			if v > max.(int64) {
				max = v
			}
		case uint:
			if v > max.(uint) {
				max = v
			}
		case uint8:
			if v > max.(uint8) {
				max = v
			}
		case uint16:
			if v > max.(uint16) {
				max = v
			}
		case uint32:
			if v > max.(uint32) {
				max = v
			}
		case uint64:
			if v > max.(uint64) {
				max = v
			}
		case float32:
			if v > max.(float32) {
				max = v
			}
		case float64:
			if v > max.(float64) {
				max = v
			}
		default:
			return nil, fmt.Errorf("the element %#v of slice type %T isn't numerical type", v, v)
		}
	}
	return max, nil
}


// InsertSliceE inserts a element to slice in the specified index.
// Note that the original slice will not be modified.
func InsertSliceE(slice interface{}, index int, value interface{}) (interface{}, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	t := reflect.TypeOf(slice)
	if index < 0 || index > v.Len() || t.Elem() != reflect.TypeOf(value) {
		return nil, errors.New("param is invalid")
	}

	dst := reflect.MakeSlice(t, 0, v.Len()+1)

	// add the element to the end of slice
	if index == v.Len() {
		dst = reflect.AppendSlice(dst, v)
		dst = reflect.Append(dst, reflect.ValueOf(value))
		return dst.Interface(), nil
	}

	dst = reflect.AppendSlice(dst, v.Slice(0, index+1))
	dst = reflect.AppendSlice(dst, v.Slice(index, v.Len()))
	dst.Index(index).Set(reflect.ValueOf(value))
	return dst.Interface(), nil
}

// New returns an error that formats as the given text.
// Each call to New returns a distinct error value even if the text is identical.
func NewArrayError(text string) error {
	return &ArrayErrorString{text}
}

// errorString is a trivial implementation of error.
type ArrayErrorString struct {
	s string
}

func (e *ArrayErrorString) Error() string {
	return e.s
}

// UpdateSliceE modifies the specified index element of slice.
// Note that the original slice will not be modified.
func UpdateSliceE(slice interface{}, index int, value interface{}) (interface{}, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	if index > v.Len()-1 || reflect.TypeOf(slice).Elem() != reflect.TypeOf(value) {
		return nil, NewArrayError("param is invalid")
	}

	t := reflect.MakeSlice(reflect.TypeOf(slice), 0, 0)
	t = reflect.AppendSlice(t, v.Slice(0, v.Len()))
	t.Index(index).Set(reflect.ValueOf(value))
	return t.Interface(), nil
}

// GetEleIndexesSliceE finds all indexes of the specified element in a slice.
func GetEleIndexesSliceE(slice interface{}, value interface{}) ([]int, error) {
	// check params
	v := reflect.ValueOf(slice)
	if v.Kind() != reflect.Slice {
		return nil, fmt.Errorf("the input %#v of type %T isn't a slice", slice, slice)
	}
	// get indexes
	var indexes []int
	for i := 0; i < v.Len(); i++ {
		if v.Index(i).Interface() == value {
			indexes = append(indexes, i)
		}
	}
	return indexes, nil
}

