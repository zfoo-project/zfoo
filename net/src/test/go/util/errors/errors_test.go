package errors

import (
	"fmt"
	"testing"
)

func test1() error {
	return test2()
}

func test2() error {
	return Wrapf(New("something go wrong"), "自定义消息")
}

func TestErr(t *testing.T) {
	err := test1()
	fmt.Println(fmt.Sprintf("%+v", err))
	err = Cause(err) //获取原始对象
	fmt.Println(fmt.Sprintf("%+v", err))
}

func test11() error {
	return test21()
}

func test21() error {
	return New("something go wrong")
}

func TestErr1(t *testing.T) {
	err := test11()
	fmt.Println(fmt.Sprintf("%+v", err))
	err = Cause(err) //获取原始对象
	fmt.Println(fmt.Sprintf("%+v", err))
}
