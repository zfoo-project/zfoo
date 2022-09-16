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
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/hex"
	"errors"
)

var (
	RSABitsErr                = errors.New("bits 1024 or 2048")
	RSAPrivateKeyPemDecodeErr = errors.New("private key pem.decode error")
	RSAPublicKeyPemDecodeErr  = errors.New("public key pem.decode error")
)

type RsaKey struct {
	PrivateKey string // PKCS#8
	PublicKey  string // PKCS#8
}

// GenerateRsaKey 生成公钥私钥
func GenerateRsaKey(bits int) ([]byte, []byte, error) {
	if bits != 1024 && bits != 2048 {
		return nil, nil, RSABitsErr
	}
	privateKey, err := rsa.GenerateKey(rand.Reader, bits)
	if err != nil {
		return nil, nil, err
	}
	priKey, err := x509.MarshalPKCS8PrivateKey(privateKey)
	if err != nil {
		return nil, nil, err
	}
	pubKey, err := x509.MarshalPKIXPublicKey(&privateKey.PublicKey)
	if err != nil {
		return nil, nil, err
	}
	return priKey, pubKey, nil
}

// GenerateRsaKeyBase64 生成公钥私钥 Base64
func GenerateRsaKeyBase64(bits int) (RsaKey, error) {
	priKey, pubKey, err := GenerateRsaKey(bits)
	if err != nil {
		return RsaKey{}, err
	}
	return RsaKey{
		PrivateKey: base64.StdEncoding.EncodeToString(priKey),
		PublicKey:  base64.StdEncoding.EncodeToString(pubKey),
	}, nil
}

// GenerateRsaKeyHex 生成公钥私钥 Hex
func GenerateRsaKeyHex(bits int) (RsaKey, error) {
	priKey, pubKey, err := GenerateRsaKey(bits)
	if err != nil {
		return RsaKey{}, err
	}
	return RsaKey{
		PrivateKey: hex.EncodeToString(priKey),
		PublicKey:  hex.EncodeToString(pubKey),
	}, nil
}
