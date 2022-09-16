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
package hashutil

import (
	"crypto/hmac"
	"crypto/md5"
	"crypto/sha1"
	"crypto/sha256"
	"crypto/sha512"
	"encoding/hex"
	"hash"
)

// Md5 hashes using md5 algorithm
func Md5(b []byte) []byte {
	return Hashes(md5.New(), b)
}

// Md5Hex hashes using md5 algorithm
func Md5Hex(text string) string {
	return StringHashes(md5.New(), text)
}

// Sha1 hashes using sha1 algorithm
func Sha1(b []byte) []byte {
	return Hashes(sha1.New(), b)
}

// Sha1Hex hashes using sha1 algorithm
func Sha1Hex(text string) string {
	return StringHashes(sha1.New(), text)
}

// Sha256 hashes using sha256 algorithm
func Sha256(b []byte) []byte {
	return Hashes(sha256.New(), b)
}

// Sha256Hex hashes using sha256 algorithm
func Sha256Hex(text string) string {
	return StringHashes(sha256.New(), text)
}

// Sha512 hashes using sha512 algorithm
func Sha512(b []byte) []byte {
	return Hashes(sha512.New(), b)
}

// Sha512Hex hashes using sha512 algorithm
func Sha512Hex(text string) string {
	return StringHashes(sha512.New(), text)
}

// HmacMd5 hashes using md5 algorithm with a secret
func HmacMd5(b []byte, secret []byte) []byte {
	algorithm := hmac.New(md5.New, secret)
	return Hashes(algorithm, b)
}

// HmacMd5Hex hashes using md5 algorithm with a secret
func HmacMd5Hex(text, secret string) string {
	return StringHashes(hmac.New(md5.New, []byte(secret)), text)
}

// HmacSha256 hashes using sha256 algorithm with a secret
func HmacSha256(b []byte, secret []byte) []byte {
	return Hashes(hmac.New(sha256.New, secret), b)
}

// HmacSha256Hex hashes using sha256 algorithm with a secret
func HmacSha256Hex(text, secret string) string {
	return StringHashes(hmac.New(sha256.New, []byte(secret)), text)
}

// HmacSha512 hashes using sha512 algorithm with a secret
func HmacSha512(b []byte, secret []byte) []byte {
	return Hashes(hmac.New(sha512.New, secret), b)
}

// HmacSha512Hex hashes using sha512 algorithm with a secret
func HmacSha512Hex(text, secret string) string {
	return StringHashes(hmac.New(sha512.New, []byte(secret)), text)
}

// StringHashes hashes string
func StringHashes(algorithm hash.Hash, text string) string {
	return hex.EncodeToString(Hashes(algorithm, []byte(text)))
}

// Hashes hashes
func Hashes(algorithm hash.Hash, b []byte) []byte {
	algorithm.Write(b)
	return algorithm.Sum(nil)
}
