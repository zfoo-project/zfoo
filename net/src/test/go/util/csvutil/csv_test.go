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
package csvutil

import "testing"

func TestWriteFile(t *testing.T) {
	filePath := "testdata.csv"
	head := []string{"序号", "姓名", "电话"}
	body := make([][]string, 0)
	body = append(body, []string{"1", "Mark", "18812345678"}, []string{"2", "马克", "18612345678"})
	err := WriteFile(filePath, body, head)
	t.Logf("WriteFile: %v", err)
}

func TestWriteBytes(t *testing.T) {
	head := []string{"序号", "姓名", "电话"}
	body := make([][]string, 0)
	body = append(body, []string{"1", "Mark", "18812345678"}, []string{"2", "马克", "18612345678"})
	b, _ := WriteBytes(body, head)
	t.Logf("WriteBytes: %s", b)
}

func TestReadFile(t *testing.T) {
	filePath := "testdata.csv"
	body, head, _ := ReadFile(filePath)
	t.Logf("body: %s", body)
	t.Logf("head: %s", head)
}

func TestReadFileOffset(t *testing.T) {
	filePath := "testdata.csv"
	body, head, err := ReadFileOffset(filePath, 4, 2)
	t.Logf("body: %s", body)
	t.Logf("head: %s", head)
	t.Logf("err: %v", err)
}
