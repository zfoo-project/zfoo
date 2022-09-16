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
	"encoding/gob"
	"encoding/json"
	"fmt"
	"reflect"
	"regexp"
	"strconv"
	"strings"
	"time"
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


type RawBytes []byte

func cloneBytes(b []byte) []byte {
	if b == nil {
		return nil
	} else {
		c := make([]byte, len(b))
		copy(c, b)
		return c
	}
}

func AsString(src interface{}) string {
	switch v := src.(type) {
	case string:
		return v
	case []byte:
		return string(v)
	case int:
		return strconv.Itoa(v)
	case int32:
		return strconv.FormatInt(int64(v), 10)
	case int64:
		return strconv.FormatInt(v, 10)
	case float32:
		return strconv.FormatFloat(float64(v), 'f', -1, 64)
	case float64:
		return strconv.FormatFloat(v, 'f', -1, 64)
	case time.Time:
		return time.Time.Format(v, "2006-01-02 15:04:05")
	case bool:
		return strconv.FormatBool(v)
	default:
		{
			b, _ := json.Marshal(v)
			return string(b)
		}
	}
	return fmt.Sprintf("%v", src)
}

// 编码二进制
func EncodeByte(data interface{}) ([]byte, error) {
	buf := bytes.NewBuffer(nil)
	enc := gob.NewEncoder(buf)
	err := enc.Encode(data)
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// 解码二进制
func DecodeByte(data []byte, to interface{}) error {
	buf := bytes.NewBuffer(data)
	dec := gob.NewDecoder(buf)
	return dec.Decode(to)
}

// byte转16进制字符串
func ByteToHex(data []byte) string {
	buffer := new(bytes.Buffer)
	for _, b := range data {

		s := strconv.FormatInt(int64(b&0xff), 16)
		if len(s) == 1 {
			buffer.WriteString("0")
		}
		buffer.WriteString(s)
	}

	return buffer.String()
}

// 16进制字符串转[]byte
func HexToBye(hex string) []byte {
	length := len(hex) / 2
	slice := make([]byte, length)
	rs := []rune(hex)

	for i := 0; i < length; i++ {
		s := string(rs[i*2 : i*2+2])
		value, _ := strconv.ParseInt(s, 16, 10)
		slice[i] = byte(value & 0xFF)
	}
	return slice
}


// --------------------------------------------------------------------------------------------

type (
	// Bytes struct
	Bytes struct{}
)

// binary units (IEC 60027)
const (
	_ = 1.0 << (10 * iota) // ignore first value by assigning to blank identifier
	KiB
	MiB
	GiB
	TiB
	PiB
	EiB
)

// decimal units (SI international system of units)
const (
	KB = 1000
	MB = KB * 1000
	GB = MB * 1000
	TB = GB * 1000
	PB = TB * 1000
	EB = PB * 1000
)

var (
	patternBinary  = regexp.MustCompile(`(?i)^(-?\d+(?:\.\d+)?)\s?([KMGTPE]iB?)$`)
	patternDecimal = regexp.MustCompile(`(?i)^(-?\d+(?:\.\d+)?)\s?([KMGTPE]B?|B?)$`)
	global         = New()
)

// New creates a Bytes instance.
func New() *Bytes {
	return &Bytes{}
}

// Format formats bytes integer to human readable string according to IEC 60027.
// For example, 31323 bytes will return 30.59KB.
func (b *Bytes) Format(value int64) string {
	return b.FormatBinary(value)
}

// FormatBinary formats bytes integer to human readable string according to IEC 60027.
// For example, 31323 bytes will return 30.59KB.
func (*Bytes) FormatBinary(value int64) string {
	multiple := ""
	val := float64(value)

	switch {
	case value >= EiB:
		val /= EiB
		multiple = "EiB"
	case value >= PiB:
		val /= PiB
		multiple = "PiB"
	case value >= TiB:
		val /= TiB
		multiple = "TiB"
	case value >= GiB:
		val /= GiB
		multiple = "GiB"
	case value >= MiB:
		val /= MiB
		multiple = "MiB"
	case value >= KiB:
		val /= KiB
		multiple = "KiB"
	case value == 0:
		return "0"
	default:
		return strconv.FormatInt(value, 10) + "B"
	}

	return fmt.Sprintf("%.2f%s", val, multiple)
}

// FormatDecimal formats bytes integer to human readable string according to SI international system of units.
// For example, 31323 bytes will return 31.32KB.
func (*Bytes) FormatDecimal(value int64) string {
	multiple := ""
	val := float64(value)

	switch {
	case value >= EB:
		val /= EB
		multiple = "EB"
	case value >= PB:
		val /= PB
		multiple = "PB"
	case value >= TB:
		val /= TB
		multiple = "TB"
	case value >= GB:
		val /= GB
		multiple = "GB"
	case value >= MB:
		val /= MB
		multiple = "MB"
	case value >= KB:
		val /= KB
		multiple = "KB"
	case value == 0:
		return "0"
	default:
		return strconv.FormatInt(value, 10) + "B"
	}

	return fmt.Sprintf("%.2f%s", val, multiple)
}

// Parse parses human readable bytes string to bytes integer.
// For example, 6GiB (6Gi is also valid) will return 6442450944, and
// 6GB (6G is also valid) will return 6000000000.
func (b *Bytes) Parse(value string) (int64, error) {

	i, err := b.ParseBinary(value)
	if err == nil {
		return i, err
	}

	return b.ParseDecimal(value)
}

// ParseBinary parses human readable bytes string to bytes integer.
// For example, 6GiB (6Gi is also valid) will return 6442450944.
func (*Bytes) ParseBinary(value string) (i int64, err error) {
	parts := patternBinary.FindStringSubmatch(value)
	if len(parts) < 3 {
		return 0, fmt.Errorf("error parsing value=%s", value)
	}
	bytesString := parts[1]
	multiple := strings.ToUpper(parts[2])
	bytes, err := strconv.ParseFloat(bytesString, 64)
	if err != nil {
		return
	}

	switch multiple {
	case "KI", "KIB":
		return int64(bytes * KiB), nil
	case "MI", "MIB":
		return int64(bytes * MiB), nil
	case "GI", "GIB":
		return int64(bytes * GiB), nil
	case "TI", "TIB":
		return int64(bytes * TiB), nil
	case "PI", "PIB":
		return int64(bytes * PiB), nil
	case "EI", "EIB":
		return int64(bytes * EiB), nil
	default:
		return int64(bytes), nil
	}
}

// ParseDecimal parses human readable bytes string to bytes integer.
// For example, 6GB (6G is also valid) will return 6000000000.
func (*Bytes) ParseDecimal(value string) (i int64, err error) {
	parts := patternDecimal.FindStringSubmatch(value)
	if len(parts) < 3 {
		return 0, fmt.Errorf("error parsing value=%s", value)
	}
	bytesString := parts[1]
	multiple := strings.ToUpper(parts[2])
	bytes, err := strconv.ParseFloat(bytesString, 64)
	if err != nil {
		return
	}

	switch multiple {
	case "K", "KB":
		return int64(bytes * KB), nil
	case "M", "MB":
		return int64(bytes * MB), nil
	case "G", "GB":
		return int64(bytes * GB), nil
	case "T", "TB":
		return int64(bytes * TB), nil
	case "P", "PB":
		return int64(bytes * PB), nil
	case "E", "EB":
		return int64(bytes * EB), nil
	default:
		return int64(bytes), nil
	}
}

// Format wraps global Bytes's Format function.
func Format(value int64) string {
	return global.Format(value)
}

// FormatBinary wraps global Bytes's FormatBinary function.
func FormatBinary(value int64) string {
	return global.FormatBinary(value)
}

// FormatDecimal wraps global Bytes's FormatDecimal function.
func FormatDecimal(value int64) string {
	return global.FormatDecimal(value)
}

// Parse wraps global Bytes's Parse function.
func Parse(value string) (int64, error) {
	return global.Parse(value)
}
