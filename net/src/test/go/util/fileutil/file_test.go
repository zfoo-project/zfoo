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

func TestCreateFile(t *testing.T) {
	filePath := "testdata/file/create.txt"
	fs, err := CreateFile(filePath)
	defer fs.Close()
	t.Logf("CreateFile: %v", err)
}

func TestWriteBytesToFile(t *testing.T) {
	filePath := "testdata/file/write.txt"
	t.Logf("WriteBytesToFile: %v", WriteBytesToFile(filePath, []byte("hello")))
}

func TestReadFileToBytes(t *testing.T) {
	filePath := "testdata/file/write.txt"
	b, err := ReadFileToBytes(filePath)
	t.Logf("ReadFileToBytes: %v, %v", string(b), err)
}

func TestReadHttpFileToBytes(t *testing.T) {
	filePath := "https://game.gtimg.cn/images/yxzj/img201606/heroimg/109/109.jpg"
	b, err := ReadHttpFileToBytes(filePath)
	err = WriteBytesToFile("testdata/file/httpFile.jpeg", b)
	t.Logf("ReadHttpFileToBytes:  %v", err)
}

func TestCopy(t *testing.T) {
	sourcePath := "testdata/file/write.txt"
	targetPath := "testdata/file/target.txt"
	t.Logf("Copy: %v", Copy(sourcePath, targetPath))
}

func TestDownload(t *testing.T) {
	sourceUrl := "https://game.gtimg.cn/images/yxzj/img201606/heroimg/109/109.jpg"
	targetPath := "testdata/file/109-download.jpeg"
	t.Logf("Copy: %v", Download(sourceUrl, targetPath))
}

func TestName(t *testing.T) {
	t.Logf("Name: %v", Name("testdata/file/109-download.jpeg"))
}
