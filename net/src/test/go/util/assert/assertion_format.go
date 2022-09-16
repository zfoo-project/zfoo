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

import "testing"

// Equalf asserts that two objects are equal.
//
//    assert.Equalf(t, 123, 123, "error message %s", "formatted")
//
// Pointer variable equality is determined based on the equality of the
// referenced values (as opposed to the memory addresses). Function equality
// cannot be determined and will always fail.
func Equalf(t *testing.T, expected interface{}, actual interface{}, msg string, args ...interface{}) bool {
	t.Helper()
	return Equal(t, expected, actual, append([]interface{}{msg}, args...)...)
}

// NotEqualf asserts that the specified values are NOT equal.
//
//    assert.NotEqualf(t, obj1, obj2, "error message %s", "formatted")
//
// Pointer variable equality is determined based on the equality of the
// referenced values (as opposed to the memory addresses).
func NotEqualf(t *testing.T, expected interface{}, actual interface{}, msg string, args ...interface{}) bool {
	t.Helper()
	return NotEqual(t, expected, actual, append([]interface{}{msg}, args...)...)
}

// Nilf asserts that the specified object is nil.
//
//    assert.Nilf(t, err, "error message %s", "formatted")
func Nilf(t *testing.T, object interface{}, msg string, args ...interface{}) bool {
	t.Helper()
	return Nil(t, object, append([]interface{}{msg}, args...)...)
}

// NotNilf asserts that the specified object is not nil.
//
//    assert.NotNilf(t, err, "error message %s", "formatted")
func NotNilf(t *testing.T, object interface{}, msg string, args ...interface{}) bool {
	t.Helper()
	return NotNil(t, object, append([]interface{}{msg}, args...)...)
}

// Emptyf asserts that the specified object is empty.  I.e. nil, "", false, 0 or either
// a slice or a channel with len == 0.
//
//  assert.Emptyf(t, obj, "error message %s", "formatted")
func Emptyf(t *testing.T, object interface{}, msg string, args ...interface{}) bool {
	t.Helper()
	return Empty(t, object, append([]interface{}{msg}, args...)...)
}

// NotEmptyf asserts that the specified object is NOT empty.  I.e. not nil, "", false, 0 or either
// a slice or a channel with len == 0.
//
//  if assert.NotEmptyf(t, obj, "error message %s", "formatted") {
//    assert.Equal(t, "two", obj[1])
//  }
func NotEmptyf(t *testing.T, object interface{}, msg string, args ...interface{}) bool {
	t.Helper()
	return NotEmpty(t, object, append([]interface{}{msg}, args...)...)
}

// Lenf asserts that the specified object has specific length.
// Lenf also fails if the object has a type that len() not accept.
//
//    assert.Lenf(t, mySlice, 3, "error message %s", "formatted")
func Lenf(t *testing.T, object interface{}, length int, msg string, args ...interface{}) bool {
	t.Helper()
	return Len(t, object, length, append([]interface{}{msg}, args...)...)
}
