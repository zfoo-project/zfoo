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
package jsonutil

import (
	"encoding/json"
)

/**
  JSON (map转json)
*/

func ToJsonString(data map[string]interface{}) string {
	jsonData, err := json.Marshal(data)
	if err != nil {
		return ""
	}
	return string(jsonData)
}

/*
	泛型比较麻烦,单独做一个
*/
func Struct2Json(data interface{}) (string, error) {
	jsonData, err := json.Marshal(data)
	if err != nil {
		return "", err
	}
	return string(jsonData), err
}

/**
  JSON (json转map)
*/
func StringToJson(data string) map[string]interface{} {
	var jsonData map[string]interface{}
	json.Unmarshal([]byte(data), &jsonData)
	return jsonData
}

/**
	JSONstring (json转IntList)
*/
func ToIntList(data string) []int {
	var tmp = make([]int, 0)
	json.Unmarshal([]byte(data), &tmp)
	return tmp
}
