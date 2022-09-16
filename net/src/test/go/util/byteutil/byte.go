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
package byteutil

import (
	"bytes"
	"encoding/binary"
	"reflect"
	"unsafe"
)

// StringToBytes 强制转换 []byte(s)
func StringToBytes(s string) []byte {
	sh := (*reflect.StringHeader)(unsafe.Pointer(&s))
	bh := reflect.SliceHeader{
		Data: sh.Data,
		Len:  sh.Len,
		Cap:  sh.Len,
	}
	return *(*[]byte)(unsafe.Pointer(&bh))
}

// BytesToString 强制转换 string(s)
func BytesToString(b []byte) string {
	return *(*string)(unsafe.Pointer(&b))
}

// Uint64ToBytes uint64转byte
func Uint64ToBytes(i uint64) []byte {
	var buf = make([]byte, 8)
	binary.BigEndian.PutUint64(buf, i)
	return buf
}

// BytesToUint64 byte转uint64
func BytesToUint64(b []byte) uint64 {
	return binary.BigEndian.Uint64(b)
}

// Split 数据分片
func Split(buf []byte, limit int) [][]byte {
	var chunk []byte
	chunks := make([][]byte, 0, len(buf)/limit+1)
	for len(buf) >= limit {
		chunk, buf = buf[:limit], buf[limit:]
		chunks = append(chunks, chunk)
	}
	if len(buf) > 0 {
		chunks = append(chunks, buf[:])
	}
	return chunks
}

// Join 数据合并
func Join(s [][]byte) []byte {
	return bytes.Join(s, []byte(""))
}
