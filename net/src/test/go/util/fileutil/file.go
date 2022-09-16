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
	"bufio"
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"path"
	"path/filepath"
	"regexp"
	"strings"
)

/**
os.O_WRONLY	只写
os.O_CREATE	创建文件
os.O_RDONLY	只读
os.O_RDWR	读写
os.O_TRUNC	清空
os.O_APPEND	追加
*/

// CreateFile 创建或清空文件
func CreateFile(filePath string) (*os.File, error) {
	if err := MkDir(path.Dir(filePath)); err != nil {
		return nil, err
	}
	return os.Create(filePath)
}

// ReadFileToBytes 读文件
func ReadFileToBytes(filePath string) ([]byte, error) {
	b, err := ioutil.ReadFile(filePath)
	if err != nil {
		return nil, err
	}
	return b, nil
}

// WriteBytesToFile 写文件 覆盖
func WriteBytesToFile(filePath string, b []byte) error {
	f, err := CreateFile(filePath)
	if err != nil {
		return err
	}
	defer f.Close()
	wt := bufio.NewWriter(f)
	_, err = io.Copy(wt, bytes.NewReader(b))
	if err != nil {
		return err
	}
	wt.Flush()
	return nil
}

// ReadHttpFileToBytes 读网络文件
func ReadHttpFileToBytes(fileUrl string) ([]byte, error) {
	resp, err := http.Get(fileUrl)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	return ioutil.ReadAll(resp.Body)
}

// Copy 复制文件
func Copy(sourcePath, targetPath string) error {
	b, err := ReadFileToBytes(sourcePath)
	if err != nil {
		return nil
	}
	return WriteBytesToFile(targetPath, b)
}

// Download 下载文件
func Download(sourceUrl, targetPath string) error {
	b, err := ReadHttpFileToBytes(sourceUrl)
	if err != nil {
		return nil
	}
	return WriteBytesToFile(targetPath, b)
}

// Name 获取文件名.
func Name(filePath string) string {
	return filepath.Base(filePath)
}

// -----------------------------------------------------------------------------------------

// RelPath gets relative path.
func RelPath(targpath string) string {
	basepath, _ := filepath.Abs("./")
	rel, _ := filepath.Rel(basepath, targpath)
	return strings.Replace(rel, `\`, `/`, -1)
}

var curpath = SelfDir()

// SelfChdir switch the working path to my own path.
func SelfChdir() {
	if err := os.Chdir(curpath); err != nil {
		log.Fatal(err)
	}
}

// FileExists reports whether the named file or directory exists.
func FileExists(name string) (existed bool) {
	existed, _ = FileExist(name)
	return
}

// FileExist reports whether the named file or directory exists.
func FileExist(name string) (existed bool, isDir bool) {
	info, err := os.Stat(name)
	if err != nil {
		return !os.IsNotExist(err), false
	}
	return true, info.IsDir()
}

// SearchFile Search a file in paths.
// this is often used in search config file in /etc ~/
func SearchFile(filename string, paths ...string) (fullpath string, err error) {
	for _, path := range paths {
		fullpath = filepath.Join(path, filename)
		existed, _ := FileExist(fullpath)
		if existed {
			return
		}
	}
	return
}

// GrepFile like command grep -E
// for example: GrepFile(`^hello`, "hello.txt")
// \n is striped while read
func GrepFile(patten string, filename string) (lines []string, err error) {
	re, err := regexp.Compile(patten)
	if err != nil {
		return
	}

	fd, err := os.Open(filename)
	if err != nil {
		return
	}
	lines = make([]string, 0)
	reader := bufio.NewReader(fd)
	prefix := ""
	isLongLine := false
	for {
		byteLine, isPrefix, er := reader.ReadLine()
		if er != nil && er != io.EOF {
			return nil, er
		}
		if er == io.EOF {
			break
		}
		line := string(byteLine)
		if isPrefix {
			prefix += line
			continue
		} else {
			isLongLine = true
		}

		line = prefix + line
		if isLongLine {
			prefix = ""
		}
		if re.MatchString(line) {
			lines = append(lines, line)
		}
	}
	return lines, nil
}

// WalkDirs traverses the directory, return to the relative path.
// You can specify the suffix.
func WalkDirs(targpath string, suffixes ...string) (dirlist []string) {
	if !filepath.IsAbs(targpath) {
		targpath, _ = filepath.Abs(targpath)
	}
	err := filepath.Walk(targpath, func(retpath string, f os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !f.IsDir() {
			return nil
		}
		if len(suffixes) == 0 {
			dirlist = append(dirlist, RelPath(retpath))
			return nil
		}
		_retpath := RelPath(retpath)
		for _, suffix := range suffixes {
			if strings.HasSuffix(_retpath, suffix) {
				dirlist = append(dirlist, _retpath)
			}
		}
		return nil
	})

	if err != nil {
		log.Printf("utils.WalkRelDirs: %v\n", err)
		return
	}

	return
}

// FilepathSplitExt splits the filename into a pair (root, ext) such that root + ext == filename,
// and ext is empty or begins with a period and contains at most one period.
// Leading periods on the basename are ignored; splitext('.cshrc') returns ('', '.cshrc').
func FilepathSplitExt(filename string, slashInsensitive ...bool) (root, ext string) {
	insensitive := false
	if len(slashInsensitive) > 0 {
		insensitive = slashInsensitive[0]
	}
	if insensitive {
		filename = FilepathSlashInsensitive(filename)
	}
	for i := len(filename) - 1; i >= 0 && !os.IsPathSeparator(filename[i]); i-- {
		if filename[i] == '.' {
			return filename[:i], filename[i:]
		}
	}
	return filename, ""
}

// FilepathStem returns the stem of filename.
// Example:
//  FilepathStem("/root/dir/sub/file.ext") // output "file"
// NOTE:
//  If slashInsensitive is empty, default is false.
func FilepathStem(filename string, slashInsensitive ...bool) string {
	insensitive := false
	if len(slashInsensitive) > 0 {
		insensitive = slashInsensitive[0]
	}
	if insensitive {
		filename = FilepathSlashInsensitive(filename)
	}
	base := filepath.Base(filename)
	for i := len(base) - 1; i >= 0; i-- {
		if base[i] == '.' {
			return base[:i]
		}
	}
	return base
}

// FilepathSlashInsensitive ignore the difference between the slash and the backslash,
// and convert to the same as the current system.
func FilepathSlashInsensitive(path string) string {
	if filepath.Separator == '/' {
		return strings.Replace(path, "\\", "/", -1)
	}
	return strings.Replace(path, "/", "\\", -1)
}

// FilepathContains checks if the basepath path contains the subpaths.
func FilepathContains(basepath string, subpaths []string) error {
	basepath, err := filepath.Abs(basepath)
	if err != nil {
		return err
	}
	for _, p := range subpaths {
		p, err = filepath.Abs(p)
		if err != nil {
			return err
		}
		rel, err := filepath.Rel(basepath, p)
		if err != nil {
			return err
		}
		if strings.HasPrefix(rel, "..") {
			return fmt.Errorf("%s is not include %s", basepath, p)
		}
	}
	return nil
}

func filepathRelative(basepath, targpath string) (string, error) {
	abs, err := filepath.Abs(targpath)
	if err != nil {
		return "", err
	}
	rel, err := filepath.Rel(basepath, abs)
	if err != nil {
		return "", err
	}
	if strings.HasPrefix(rel, "..") {
		return "", fmt.Errorf("%s is not include %s", basepath, abs)
	}
	return rel, nil
}

// FilepathDistinct removes the same path and return in the original order.
// If toAbs is true, return the result to absolute paths.
func FilepathDistinct(paths []string, toAbs bool) ([]string, error) {
	m := make(map[string]bool, len(paths))
	ret := make([]string, 0, len(paths))
	for _, p := range paths {
		abs, err := filepath.Abs(p)
		if err != nil {
			return nil, err
		}
		if m[abs] {
			continue
		}
		m[abs] = true
		if toAbs {
			ret = append(ret, abs)
		} else {
			ret = append(ret, p)
		}
	}
	return ret, nil
}


// FilepathSame checks if the two paths are the same.
func FilepathSame(path1, path2 string) (bool, error) {
	if path1 == path2 {
		return true, nil
	}
	p1, err := filepath.Abs(path1)
	if err != nil {
		return false, err
	}
	p2, err := filepath.Abs(path2)
	if err != nil {
		return false, err
	}
	return p1 == p2, nil
}

// MkdirAll creates a directory named path,
// along with any necessary parents, and returns nil,
// or else returns an error.
// The permission bits perm (before umask) are used for all
// directories that MkdirAll creates.
// If path is already a directory, MkdirAll does nothing
// and returns nil.
// If perm is empty, default use 0755.
func MkdirAll(path string, perm ...os.FileMode) error {
	var fm os.FileMode = 0755
	if len(perm) > 0 {
		fm = perm[0]
	}
	return os.MkdirAll(path, fm)
}

// WriteFile writes file, and automatically creates the directory if necessary.
// NOTE:
//  If perm is empty, automatically determine the file permissions based on extension.
func WriteFile(filename string, data []byte, perm ...os.FileMode) error {
	filename = filepath.FromSlash(filename)
	err := MkdirAll(filepath.Dir(filename))
	if err != nil {
		return err
	}
	if len(perm) > 0 {
		return ioutil.WriteFile(filename, data, perm[0])
	}
	var ext string
	if idx := strings.LastIndex(filename, "."); idx != -1 {
		ext = filename[idx:]
	}
	switch ext {
	case ".sh", ".py", ".rb", ".bat", ".com", ".vbs", ".htm", ".run", ".App", ".exe", ".reg":
		return ioutil.WriteFile(filename, data, 0755)
	default:
		return ioutil.WriteFile(filename, data, 0644)
	}
}

// RewriteFile rewrites the file.
func RewriteFile(filename string, fn func(content []byte) (newContent []byte, err error)) error {
	f, err := os.OpenFile(filename, os.O_RDWR, 0777)
	if err != nil {
		return err
	}
	defer f.Close()
	content, err := ioutil.ReadAll(f)
	if err != nil {
		return err
	}
	newContent, err := fn(content)
	if err != nil {
		return err
	}
	if bytes.Equal(content, newContent) {
		return nil
	}
	f.Seek(0, 0)
	f.Truncate(0)
	_, err = f.Write(newContent)
	return err
}

// RewriteToFile rewrites the file to newfilename.
// If newfilename already exists and is not a directory, replaces it.
func RewriteToFile(filename, newfilename string, fn func(content []byte) (newContent []byte, err error)) error {
	f, err := os.Open(filename)
	if err != nil {
		return err
	}
	defer f.Close()
	if err != nil {
		return err
	}
	info, err := f.Stat()
	if err != nil {
		return err
	}
	cnt, err := ioutil.ReadAll(f)
	if err != nil {
		return err
	}
	newContent, err := fn(cnt)
	if err != nil {
		return err
	}
	return WriteFile(newfilename, newContent, info.Mode())
}


// ----------------------------------------------------------------------------------------------

// ReadLines reads all lines of the specified file.
func ReadLines(path string) ([]string, int, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, 0, err
	}
	defer file.Close()

	var lines []string
	lineCount := 0
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		lines = append(lines, scanner.Text())
		lineCount++
	}

	if scanner.Err() == bufio.ErrTooLong {
		panic(scanner.Err())
	}
	return lines, lineCount, scanner.Err()
}

// ReadLinesV2 reads all lines of the specified file.
func ReadLinesV2(path string) ([]string, int, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, 0, err
	}
	defer file.Close()

	var lines []string
	lineCount := 0
	reader := bufio.NewReader(file)
	for {
		line, err := reader.ReadString('\n')
		lines = append(lines, line)
		lineCount++
		if err == io.EOF {
			return lines, lineCount, nil
		}
		if err != nil {
			return lines, lineCount, err
		}
	}
}

// ListDir lists all the files in the directory
func ListDir(path string) []string {
	files, err := ioutil.ReadDir(path)
	if err != nil {
		panic(err)
	}

	filenames := make([]string, len(files))
	for idx, file := range files {
		filenames[idx] = file.Name()
	}
	return filenames
}

// IsPathExist determines whether a file/dir path exists.
// User os.Stat to get the info of target file or dir to check whether exists.
// If os.Stat returns nil err, the target exists.
// If os.Stat returns a os.ErrNotExist err, the target does not exist.
// If the error returned is another type, the target is uncertain whether exists.
func IsPathExist(path string) (bool, error) {
	_, err := os.Stat(path)
	if err == nil {
		return true, nil
	}
	if os.IsNotExist(err) {
		return false, nil
	}
	return false, err
}


// Create creates or truncates the target file specified by path.
// If the parent directory does not exist, it will be created with mode os.ModePerm.is cr truncated.
// If the file does not exist, it is created with mode 0666.
// If successful, methods on the returned File can be used for I/O; the associated file descriptor has mode O_RDWR.
func Create(filePath string) (*os.File, error) {
	if exist, err := IsPathExist(filePath); err != nil {
		return nil, err
	} else if exist {
		return os.Create(filePath)
	}
	if err := os.MkdirAll(filepath.Dir(filePath), os.ModePerm); err != nil {
		return nil, err
	}
	return os.Create(filePath)
}

// FileToBytes serialize the file to bytes.
func FileToBytes(path string) []byte {
	byteStream, _ := ioutil.ReadFile(path)
	return byteStream
}

