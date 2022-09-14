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

package base

import (
	"fmt"
	"time"
)

func init() {
	fmt.Println("base init")
}

func VarTest() {
	var a string = "Runoob"
	fmt.Println(a)

	var b, c int = 1, 2
	fmt.Println(b, c)
}

func NilTest() {
	var a *int
	var b []int
	var c map[string]int
	var d chan int
	var e func(string) int
	var f error // error 是接口

	fmt.Println(a)
	fmt.Println(b)
	fmt.Println(c)
	fmt.Println(d)
	fmt.Println(e)
	fmt.Println(f)
}

func ConstTest() {
	const LENGTH int = 10
	const WIDTH int = 5
	var area int
	const a, b, c = 1, false, "str" //多重赋值

	area = LENGTH * WIDTH
	fmt.Printf("面积为 : %d", area)
	println()
	println(a, b, c)
}

func IfTest() {
	/* 局部变量定义 */
	var a int = 100

	/* 判断布尔表达式 */
	if a < 20 {
		/* 如果条件为 true 则执行以下语句 */
		fmt.Println("a 小于 20")
	} else {
		/* 如果条件为 false 则执行以下语句 */
		fmt.Println("a 不小于 20")
	}
	fmt.Println("a 的值为 : ", a)
}

func ForTest() {
	sum := 0
	for i := 0; i <= 10; i++ {
		sum += i
	}
	fmt.Println(sum)

	// for each
	var strArray = []string{"google", "runoob"}
	for i, s := range strArray {
		fmt.Println(i, s)
	}

	// for map
	map1 := make(map[int]float32)
	map1[1] = 1.0
	map1[2] = 2.0
	map1[3] = 3.0
	map1[4] = 4.0

	// 读取 key 和 value
	for key, value := range map1 {
		fmt.Printf("key is: %d - value is: %f\n", key, value)
	}
}

/* 函数返回两个数的最大值 */
func maxTest(num1, num2 int) int {
	/* 定义局部变量 */
	var result int

	if num1 > num2 {
		result = num1
	} else {
		result = num2
	}
	return result
}

func Max(num1, num2 int) int {
	type maxFunc func(int, int) int
	var max maxFunc
	max = maxTest
	return max(num1, num2)
}

var myChan = make(chan string)

func show(msg string) {
	fmt.Println(msg)
	time.Sleep(time.Millisecond * 5000)
	myChan <- ("go" + msg)
}

func RoutinesTest() {
	go show("java")
	fmt.Println("wait...")
	var msg = <-myChan
	fmt.Println(msg)
}
