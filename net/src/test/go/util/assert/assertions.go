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
package assert

import (
	"bufio"
	"bytes"
	"errors"
	"fmt"
	"reflect"
	"strings"
	"testing"
	"time"
)

// Equal asserts that two objects are equal.
//
//    assert.Equal(t, 123, 123)
//
// Pointer variable equality is determined based on the equality of the
// referenced values (as opposed to the memory addresses). Function equality
// cannot be determined and will always fail.
func Equal(t *testing.T, expected, actual interface{}, msgAndArgs ...interface{}) bool {
	t.Helper()
	if err := validateEqualArgs(expected, actual); err != nil {
		return Fail(t, fmt.Sprintf("Invalid operation: %#v == %#v (%s)",
			expected, actual, err), msgAndArgs...)
	}
	if !ObjectsAreEqual(expected, actual) {
		expected, actual = formatUnequalValues(expected, actual)
		return Fail(t, fmt.Sprintf("Not equal: \n"+
			"expected: %s\n"+
			"actual  : %s", expected, actual), msgAndArgs...)
	}
	return true
}

// NotEqual asserts that the specified values are NOT equal.
//
//    assert.NotEqual(t, obj1, obj2)
//
// Pointer variable equality is determined based on the equality of the
// referenced values (as opposed to the memory addresses).
func NotEqual(t *testing.T, expected, actual interface{}, msgAndArgs ...interface{}) bool {
	t.Helper()
	if err := validateEqualArgs(expected, actual); err != nil {
		return Fail(t, fmt.Sprintf("Invalid operation: %#v != %#v (%s)",
			expected, actual, err), msgAndArgs...)
	}
	if ObjectsAreEqual(expected, actual) {
		return Fail(t, fmt.Sprintf("Should not be: %#v\n", actual), msgAndArgs...)
	}
	return true
}

// Empty asserts that the specified object is empty.  I.e. nil, "", false, 0 or either
// a slice or a channel with len == 0.
//
//  assert.Empty(t, obj)
func Empty(t *testing.T, object interface{}, msgAndArgs ...interface{}) bool {
	pass := isEmpty(object)
	if !pass {
		t.Helper()
		Fail(t, fmt.Sprintf("Should be empty, but was %v", object), msgAndArgs...)
	}
	return pass
}

// NotEmpty asserts that the specified object is NOT empty.  I.e. not nil, "", false, 0 or either
// a slice or a channel with len == 0.
//
//  if assert.NotEmpty(t, obj) {
//    assert.Equal(t, "two", obj[1])
//  }
func NotEmpty(t *testing.T, object interface{}, msgAndArgs ...interface{}) bool {
	pass := !isEmpty(object)
	if !pass {
		t.Helper()
		Fail(t, fmt.Sprintf("Should NOT be empty, but was %v", object), msgAndArgs...)
	}
	return pass
}

// Nil asserts that the specified object is nil.
//
//    assert.Nil(t, err)
func Nil(t *testing.T, object interface{}, msgAndArgs ...interface{}) bool {
	if isNil(object) {
		return true
	}
	t.Helper()
	return Fail(t, fmt.Sprintf("Expected nil, but got: %#v", object), msgAndArgs...)
}

// NotNil asserts that the specified object is not nil.
//
//    assert.NotNil(t, err)
func NotNil(t *testing.T, object interface{}, msgAndArgs ...interface{}) bool {
	if !isNil(object) {
		return true
	}
	t.Helper()
	return Fail(t, "Expected value not to be nil.", msgAndArgs...)
}

// Len asserts that the specified object has specific length.
// Len also fails if the object has a type that len() not accept.
//
//    assert.Len(t, mySlice, 3)
func Len(t *testing.T, object interface{}, length int, msgAndArgs ...interface{}) bool {
	t.Helper()
	ok, l := getLen(object)
	if !ok {
		return Fail(t, fmt.Sprintf("\"%v\" could not be applied builtin len()", object), msgAndArgs...)
	}
	if l != length {
		return Fail(t, fmt.Sprintf("\"%s\" should have %d item(s), but has %d", object, length, l), msgAndArgs...)
	}
	return true
}

// Fail reports a failure through
func Fail(t *testing.T, failureMessage string, msgAndArgs ...interface{}) bool {
	t.Helper()
	content := []labeledContent{
		{"Error", failureMessage},
	}
	message := messageFromMsgAndArgs(msgAndArgs...)
	if len(message) > 0 {
		content = append(content, labeledContent{"Messages", message})
	}
	t.Errorf("\n%s", ""+labeledOutput(content...))
	return false
}

// ObjectsAreEqual determines if two objects are considered equal..
func ObjectsAreEqual(expected, actual interface{}) bool {
	if expected == nil || actual == nil {
		return expected == actual
	}

	exp, ok := expected.([]byte)
	if !ok {
		return reflect.DeepEqual(expected, actual)
	}

	act, ok := actual.([]byte)
	if !ok {
		return false
	}
	if exp == nil || act == nil {
		return exp == nil && act == nil
	}
	return bytes.Equal(exp, act)
}

func validateEqualArgs(expected, actual interface{}) error {
	if expected == nil && actual == nil {
		return nil
	}

	if isFunction(expected) || isFunction(actual) {
		return errors.New("cannot take func type as argument")
	}
	return nil
}

func formatUnequalValues(expected, actual interface{}) (e string, a string) {
	if reflect.TypeOf(expected) != reflect.TypeOf(actual) {
		return fmt.Sprintf("%T(%s)", expected, truncatingFormat(expected)),
			fmt.Sprintf("%T(%s)", actual, truncatingFormat(actual))
	}
	switch expected.(type) {
	case time.Duration:
		return fmt.Sprintf("%v", expected), fmt.Sprintf("%v", actual)
	}
	return truncatingFormat(expected), truncatingFormat(actual)
}

func truncatingFormat(data interface{}) string {
	value := fmt.Sprintf("%#v", data)
	max := bufio.MaxScanTokenSize - 100 // Give us some space the type info too if needed.
	if len(value) > max {
		value = value[0:max] + "<... truncated>"
	}
	return value
}

type labeledContent struct {
	label   string
	content string
}

func messageFromMsgAndArgs(msgAndArgs ...interface{}) string {
	if len(msgAndArgs) == 0 || msgAndArgs == nil {
		return ""
	}
	if len(msgAndArgs) == 1 {
		msg := msgAndArgs[0]
		if msgAsStr, ok := msg.(string); ok {
			return msgAsStr
		}
		return fmt.Sprintf("%+v", msg)
	}
	if len(msgAndArgs) > 1 {
		return fmt.Sprintf(msgAndArgs[0].(string), msgAndArgs[1:]...)
	}
	return ""
}

func labeledOutput(content ...labeledContent) string {
	longestLabel := 0
	for _, v := range content {
		if len(v.label) > longestLabel {
			longestLabel = len(v.label)
		}
	}
	var output string
	for _, v := range content {
		output += "\t" + v.label + ":" + strings.Repeat(" ", longestLabel-len(v.label)) + "\t" + indentMessageLines(v.content, longestLabel) + "\n"
	}
	return output
}

func indentMessageLines(message string, longestLabelLen int) string {
	outBuf := new(bytes.Buffer)
	for i, scanner := 0, bufio.NewScanner(strings.NewReader(message)); scanner.Scan(); i++ {
		// no need to align first line because it starts at the correct location (after the label)
		if i != 0 {
			// append alignLen+1 spaces to align with "{{longestLabel}}:" before adding tab
			outBuf.WriteString("\n\t" + strings.Repeat(" ", longestLabelLen+1) + "\t")
		}
		outBuf.WriteString(scanner.Text())
	}
	return outBuf.String()
}

func getLen(x interface{}) (ok bool, length int) {
	v := reflect.ValueOf(x)
	defer func() {
		if e := recover(); e != nil {
			ok = false
		}
	}()
	return true, v.Len()
}

func isFunction(arg interface{}) bool {
	if arg == nil {
		return false
	}
	return reflect.TypeOf(arg).Kind() == reflect.Func
}

func isEmpty(object interface{}) bool {

	if object == nil {
		return true
	}
	objValue := reflect.ValueOf(object)
	switch objValue.Kind() {
	case reflect.Array, reflect.Chan, reflect.Map, reflect.Slice:
		return objValue.Len() == 0
	case reflect.Ptr:
		if objValue.IsNil() {
			return true
		}
		deref := objValue.Elem().Interface()
		return isEmpty(deref)
	default:
		zero := reflect.Zero(objValue.Type())
		return reflect.DeepEqual(object, zero.Interface())
	}
}

func containsKind(kinds []reflect.Kind, kind reflect.Kind) bool {
	for i := 0; i < len(kinds); i++ {
		if kind == kinds[i] {
			return true
		}
	}
	return false
}

func isNil(object interface{}) bool {
	if object == nil {
		return true
	}

	value := reflect.ValueOf(object)
	kind := value.Kind()
	isNilableKind := containsKind(
		[]reflect.Kind{
			reflect.Chan, reflect.Func,
			reflect.Interface, reflect.Map,
			reflect.Ptr, reflect.Slice},
		kind)
	if isNilableKind && value.IsNil() {
		return true
	}
	return false
}
