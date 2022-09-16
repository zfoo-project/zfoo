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
package fileutil

import (
	"testing"
)

func TestSelfPath(t *testing.T) {
	t.Logf("SelfPath: %v", SelfPath())
}

func TestSelfDir(t *testing.T) {
	t.Logf("SelfDir: %v", SelfDir())
}

func TestMkDir(t *testing.T) {
	dirPath := "testdata/dir/dir1"
	t.Logf("Mkdir: %v", MkDir(dirPath))
}

func TestIsEmpty(t *testing.T) {
	dirPath := "testdata/dir"
	t.Logf("IsEmpty: %v", IsEmpty(dirPath))
}

func TestIsDir(t *testing.T) {
	dirPath := "testdata/dir"
	t.Logf("IsDir: %v", IsDir(dirPath))
}

func TestIsFile(t *testing.T) {
	dirPath := "testdata/dir"
	t.Logf("IsFile: %v", IsFile(dirPath))
}

func TestListIndex(t *testing.T) {
	dirPath := "testdata"
	files, dirs, err := ListIndex(dirPath)
	t.Logf("ListFiles: %v, %v, %v", files, dirs, err)
}

func TestClearDir(t *testing.T) {
	dirPath := "testdata/dir/dir1"
	t.Logf("ClearDir: %v", ClearDir(dirPath))
}

func TestClearDirF(t *testing.T) {
	dirPath := "testdata"
	t.Logf("ClearDirF: %v", ClearDirF(dirPath))
}

func TestRemoveDir(t *testing.T) {
	dirPath := "testdata"
	t.Logf("RemoveDir: %v", RemoveDir(dirPath))
}
