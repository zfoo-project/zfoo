package main

import (
	"fmt"
	"time"
)

func main() {
	// 预热
	test()
	// 正式执行
	test()
}

func test() {
	var startTime = time.Now()
	for i := 0; i < 10000; i++ {
		fibonacci(32)
	}
	fmt.Println(time.Since(startTime).Milliseconds())
}

func fibonacci(i int) int {
	if i < 2 {
		return i
	}
	return fibonacci(i-2) + fibonacci(i-1)
}

/**
public class MainTest {
    public static void main(String[] args) {
        // java的jit预热
        test();
        // 正式执行
        test();
    }

    static void test() {
        // 正式执行
        var startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            fibonacci(32);
        }
        var endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    static int fibonacci(int i) {
        if (i < 2) return i;
        return fibonacci(i - 2) + fibonacci(i - 1);
    }
}
 */
