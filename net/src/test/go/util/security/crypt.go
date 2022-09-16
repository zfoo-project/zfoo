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
package security

import (
	"bytes"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/hex"
)

// EncryptToBase64 ...
func EncryptToBase64(plainText []byte, base64PubKey string) (string, error) {
	publicBytes, err := base64.StdEncoding.DecodeString(base64PubKey)
	if err != nil {
		return "", err
	}
	cipherBytes, err := encrypt(plainText, publicBytes)
	if err != nil {
		return "", err
	}
	return base64.StdEncoding.EncodeToString(cipherBytes), nil
}

// DecryptByBase64 ...
func DecryptByBase64(cipherText, base64PriKey string) ([]byte, error) {
	privateBytes, err := base64.StdEncoding.DecodeString(base64PriKey)
	if err != nil {
		return nil, err
	}
	cipherBytes, err := base64.StdEncoding.DecodeString(cipherText)
	if err != nil {
		return nil, err
	}
	return decrypt(cipherBytes, privateBytes)
}

// EncryptToHex ...
func EncryptToHex(plainText []byte, hexPubKey string) (string, error) {
	publicBytes, err := hex.DecodeString(hexPubKey)
	if err != nil {
		return "", err
	}
	cipherBytes, err := encrypt(plainText, publicBytes)
	if err != nil {
		return "", err
	}
	return hex.EncodeToString(cipherBytes), nil
}

// DecryptByHex ...
func DecryptByHex(cipherText, hexPriKey string) ([]byte, error) {
	privateBytes, err := hex.DecodeString(hexPriKey)
	if err != nil {
		return nil, err
	}
	cipherTextBytes, err := hex.DecodeString(cipherText)
	if err != nil {
		return nil, err
	}
	return decrypt(cipherTextBytes, privateBytes)
}

// encrypt 加密
func encrypt(plainText, pubKey []byte) ([]byte, error) {
	pub, err := x509.ParsePKIXPublicKey(pubKey)
	if err != nil {
		return nil, err
	}
	publicKey := pub.(*rsa.PublicKey)
	pubSize, plainTextSize := publicKey.Size(), len(plainText)
	// EncryptPKCS1v15 encrypts the given message with RSA and the padding
	// scheme from PKCS #1 v1.5.  The message must be no longer than the
	// length of the public modulus minus 11 bytes.
	//
	// The rand parameter is used as a source of entropy to ensure that
	// encrypting the same message twice doesn't result in the same
	// ciphertext.
	//
	// WARNING: use of this function to encrypt plaintexts other than
	// session keys is dangerous. Use RSA OAEP in new protocols.
	offSet, once := 0, pubSize-11
	buffer := bytes.Buffer{}
	for offSet < plainTextSize {
		endIndex := offSet + once
		if endIndex > plainTextSize {
			endIndex = plainTextSize
		}
		bytesOnce, err := rsa.EncryptPKCS1v15(rand.Reader, publicKey, plainText[offSet:endIndex])
		if err != nil {
			return nil, err
		}
		buffer.Write(bytesOnce)
		offSet = endIndex
	}
	return buffer.Bytes(), nil
}

// decrypt 解密
func decrypt(cipherText, priKey []byte) (plainText []byte, err error) {
	pri, err := x509.ParsePKCS8PrivateKey(priKey)
	if err != nil {
		return []byte{}, err
	}
	privateKey := pri.(*rsa.PrivateKey)
	priSize, cipherTextSize := privateKey.Size(), len(cipherText)
	var offSet = 0
	var buffer = bytes.Buffer{}
	for offSet < cipherTextSize {
		endIndex := offSet + priSize
		if endIndex > cipherTextSize {
			endIndex = cipherTextSize
		}
		bytesOnce, err := rsa.DecryptPKCS1v15(rand.Reader, privateKey, cipherText[offSet:endIndex])
		if err != nil {
			return nil, err
		}
		buffer.Write(bytesOnce)
		offSet = endIndex
	}
	return buffer.Bytes(), nil
}
