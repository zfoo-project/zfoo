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
package mathutil

// AbsInt8 gets absolute value of int8.
//
// Example 1: AbsInt8(-5)
// -5 code value as below
// original code	1000,0101
// inverse code		1111,1010
// complement code	1111,1011
// Negative numbers are represented by complement code in memory.
// shifted = n >> 7 = (1111,1011) >> 7 = 1111,1111 = -1(10-base) (负数右移，左补1)
//               1111,1011
// n xor shifted =  ----------- = 0000,0100 = 4(10-base)
//               1111,1111
// (n ^ shifted) - shifted = 4 - (-1) = 5
//
// Example 2: AbsInt8(5)
// 5 code value as below
// original code 0000,0101
// Positive numbers are represented by original code in memory,
// and the XOR operation between positive numbers and 0 is equal to itself.
// shifted = n >> 7 = 0
//               0000,0101
// n xor shifted =  ----------- = 0000,0101 = 5(10-base)
//               0000,0000
// (n ^ shifted) - shifted = 5 - 0 = 5
func AbsInt8(n int8) int8 {
	shifted := n >> 7
	return (n ^ shifted) - shifted
}

// AbsInt16 gets absolute value of int16.
func AbsInt16(n int16) int16 {
	shifted := n >> 15
	return (n ^ shifted) - shifted
}

// AbsInt32 gets absolute value of int32.
func AbsInt32(n int32) int32 {
	shifted := n >> 31
	return (n ^ shifted) - shifted
}

// AbsInt64 gets absolute value of int64.
func AbsInt64(n int64) int64 {
	shifted := n >> 63
	return (n ^ shifted) - shifted
}
