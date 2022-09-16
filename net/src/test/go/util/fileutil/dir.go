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
	"errors"
	"io/ioutil"
	"os"
	"path"
	"path/filepath"
)

// SelfPath gets compiled executable file absolute path.
func SelfPath() string {
	selfPath, _ := filepath.Abs(os.Args[0])
	return selfPath
}

// SelfDir gets compiled executable file directory.
func SelfDir() string {
	return filepath.Dir(SelfPath())
}

//MkDir 创建目录.
func MkDir(dirPath string) error {
	if IsExist(dirPath) {
		return nil
	}
	return os.MkdirAll(dirPath, os.ModePerm)
}

// IsExist 判断文件或目录是否存在.
func IsExist(filePath string) bool {
	_, err := os.Stat(filePath)
	return err == nil || os.IsExist(err)
}

// IsEmpty 判断目录是否为空.
func IsEmpty(dirname string) bool {
	dir, _ := ioutil.ReadDir(dirname)
	if len(dir) == 0 {
		return true
	} else {
		return false
	}
}

// IsFile 判断文件是否存在.
func IsFile(filePath string) bool {
	f, e := os.Stat(filePath)
	if e != nil {
		return false
	}
	return !f.IsDir()
}

// IsDir 判断目录是否存在.
func IsDir(filePath string) bool {
	f, e := os.Stat(filePath)
	if e != nil {
		return false
	}
	return f.IsDir()
}

// ListIndex 目录下文件和子目录列表.
func ListIndex(dirPath string) ([]string, []string, error) {
	if !IsDir(dirPath) {
		return nil, nil, errors.New(dirPath + " not a directory")
	}
	fs, err := ioutil.ReadDir(dirPath)
	var files, dirs []string
	for _, fi := range fs {
		if !fi.IsDir() {
			files = append(files, fi.Name())
		} else {
			dirs = append(dirs, fi.Name())
		}
	}
	return files, dirs, err
}

// ClearDir 清空目录下所有文件不包括子目录.
func ClearDir(dirPath string) error {
	dir, e := ioutil.ReadDir(dirPath)
	for _, d := range dir {
		if d.IsDir() {
			continue
		}
		_ = os.Remove(path.Join([]string{dirPath, d.Name()}...))
	}
	return e
}

// ClearDirF 清空目录下所有文件和目录.
func ClearDirF(dirPath string) error {
	dir, e := ioutil.ReadDir(dirPath)
	for _, d := range dir {
		_ = os.RemoveAll(path.Join([]string{dirPath, d.Name()}...))
	}
	return e
}

// RemoveDir 删除空目录.
func RemoveDir(dirPath string) error {
	if !IsDir(dirPath) {
		return errors.New(dirPath + " not a directory")
	}
	return os.Remove(dirPath)
}
