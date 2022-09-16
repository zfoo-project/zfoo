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

import (
	"reflect"
)

type CMPRES int8

const (
	INCMP CMPRES = iota - 2
	LT
	EQ
	GT
)

// Compare compare the size relationship between two numeric values or strings.
// The result is INCMP(incomparable), LT(less than), EQ(equal) or GT(greater than).
func Compare(lhs, rhs interface{}) CMPRES {
	if !isComparable(lhs, rhs) {
		return INCMP
	}

	lhsVal := reflect.ValueOf(lhs)
	rhsVal := reflect.ValueOf(rhs)

	switch lhsVal.Kind() {
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
		switch {
		case lhsVal.Int() < rhsVal.Int():
			return LT
		case lhsVal.Int() == rhsVal.Int():
			return EQ
		case lhsVal.Int() > rhsVal.Int():
			return GT
		}
	case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
		switch {
		case lhsVal.Uint() < rhsVal.Uint():
			return LT
		case lhsVal.Uint() == rhsVal.Uint():
			return EQ
		case lhsVal.Uint() > rhsVal.Uint():
			return GT
		}
	case reflect.Float32, reflect.Float64:
		switch {
		case lhsVal.Float() < rhsVal.Float():
			return LT
		case lhsVal.Float() == rhsVal.Float():
			return EQ
		case lhsVal.Float() > rhsVal.Float():
			return GT
		}
	case reflect.String:
		switch {
		case lhsVal.String() < rhsVal.String():
			return LT
		case lhsVal.String() == rhsVal.String():
			return EQ
		case lhsVal.String() > rhsVal.String():
			return GT
		}
	}
	return INCMP
}

func isComparable(lhs, rhs interface{}) bool {
	lhsVal := reflect.ValueOf(lhs)
	rhsVal := reflect.ValueOf(rhs)
	return lhsVal.Kind() == rhsVal.Kind()
}

func CompareLT(lhs, rhs interface{}) bool {
	return Compare(lhs, rhs) == LT
}

func CompareLE(lhs, rhs interface{}) bool {
	res := Compare(lhs, rhs)
	return res == LT || res == EQ
}

func CompareEQ(lhs, rhs interface{}) bool {
	return Compare(lhs, rhs) == EQ
}

func CompareGT(lhs, rhs interface{}) bool {
	return Compare(lhs, rhs) == GT
}

func CompareGE(lhs, rhs interface{}) bool {
	res := Compare(lhs, rhs)
	return Compare(lhs, rhs) == GT || res == EQ
}
