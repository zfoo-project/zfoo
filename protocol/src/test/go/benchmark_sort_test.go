package main

import (
	"fmt"
	"testing"
	"time"
)

func TestBubbleSort(t *testing.T) {
	// 预热
	testBubbleSort()
	// 正式执行
	testBubbleSort()
}

func testBubbleSort() {
	var startTime = time.Now()
	const NUM int = 100000000
	for i := 0; i < NUM; i++ {
		var arr = []int{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}
		bubbleSort(arr)
	}
	fmt.Println(time.Since(startTime).Milliseconds())
}

// 排序
func bubbleSort(arr []int) {
	for j := 0; j < len(arr)-1; j++ {
		for k := 0; k < len(arr)-1-j; k++ {
			if arr[k] < arr[k+1] {
				temp := arr[k]
				arr[k] = arr[k+1]
				arr[k+1] = temp
			}
		}
	}
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
        var array = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        for (var i = 0; i < 100000000; i++) {
            bubbleSort(array);
        }
        var endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }

    public static void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j] < arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}
*/
