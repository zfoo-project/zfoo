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
	"go/build"
	"io/ioutil"
	"os"
	"path"
	"path/filepath"
	"strings"
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

func PathExists(path string) bool {
	_, err := os.Stat(path)
	if err == nil {
		return true
	}
	if os.IsNotExist(err) {
		return false
	}
	return false
}

// 创建目录
func BuildDir(abs_dir string) error {
	return os.MkdirAll(path.Dir(abs_dir), os.ModePerm) //生成多级目录
}

// 删除文件或文件夹
func DeleteFile(abs_dir string) error {
	return os.RemoveAll(abs_dir)
}

// 获取目录所有文件夹
func GetPathDirs(abs_dir string) (re []string) {
	if PathExists(abs_dir) {
		files, _ := ioutil.ReadDir(abs_dir)
		for _, f := range files {
			if f.IsDir() {
				re = append(re, f.Name())
			}
		}
	}
	return
}

// 获取目录所有文件
func GetPathFiles(abs_dir string) (re []string) {
	if PathExists(abs_dir) {
		files, _ := ioutil.ReadDir(abs_dir)
		for _, f := range files {
			if !f.IsDir() {
				re = append(re, f.Name())
			}
		}
	}
	return
}

// 获取程序运行路径
func GetCurrentDirectory() string {
	dir, _ := filepath.Abs(filepath.Dir(os.Args[0]))
	return strings.Replace(dir, "\\", "/", -1)
}



// GetGopaths returns the list of Go path directories.
func GetGopaths() []string {
	var all []string
	for _, p := range filepath.SplitList(build.Default.GOPATH) {
		if p == "" || p == build.Default.GOROOT {
			// Empty paths are uninteresting.
			// If the path is the GOROOT, ignore it.
			// People sometimes set GOPATH=$GOROOT.
			// Do not get confused by this common mistake.
			continue
		}
		if strings.HasPrefix(p, "~") {
			// Path segments starting with ~ on Unix are almost always
			// users who have incorrectly quoted ~ while setting GOPATH,
			// preventing it from expanding to $HOME.
			// The situation is made more confusing by the fact that
			// bash allows quoted ~ in $PATH (most shells do not).
			// Do not get confused by this, and do not try to use the path.
			// It does not exist, and printing errors about it confuses
			// those users even more, because they think "sure ~ exists!".
			// The go command diagnoses this situation and prints a
			// useful error.
			// On Windows, ~ is used in short names, such as c:\progra~1
			// for c:\program files.
			continue
		}
		all = append(all, p)
	}
	for k, v := range all {
		// GOPATH should end with / or \
		if strings.HasSuffix(v, "/") || strings.HasSuffix(v, string(os.PathSeparator)) {
			continue
		}
		v += string(os.PathSeparator)
		all[k] = v
	}
	return all
}

// GetFirstGopath gets the first $GOPATH value.
func GetFirstGopath(allowAutomaticGuessing bool) (gopath string, err error) {
	a := GetGopaths()
	if len(a) > 0 {
		gopath = a[0]
	}
	defer func() {
		gopath = strings.Replace(gopath, "/", string(os.PathSeparator), -1)
	}()
	if gopath != "" {
		return
	}
	if !allowAutomaticGuessing {
		err = errors.New("not found GOPATH")
		return
	}
	p, _ := os.Getwd()
	p = strings.Replace(p, "\\", "/", -1) + "/"
	i := strings.LastIndex(p, "/src/")
	if i == -1 {
		err = errors.New("not found GOPATH")
		return
	}
	gopath = p[:i+1]
	return
}
