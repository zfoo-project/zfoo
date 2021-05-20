/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.util.security;


import com.zfoo.protocol.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * AES加密和解密
 * <p>
 * 默认AES/ECB/PKCS5Padding
 *
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class AesUtils {

    private static final Key KEY;
    private static final String KEY_STR = "=jE[`B],YO24Vt+Akh&}D7@s9l1uLKP)";

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";
    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";

    static {
        try {
            KEY = new SecretKeySpec(KEY_STR.getBytes(StringUtils.DEFAULT_CHARSET_NAME), ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 对str进行AES加密
     *
     * @param str 需要加密的字符串
     * @return AES加密后的字符串
     */
    public static String getEncryptString(String str) {
        try {
            var base64Encoder = Base64.getEncoder();
            var strBytes = str.getBytes(StringUtils.DEFAULT_CHARSET_NAME);
            var cipher = Cipher.getInstance(ALGORITHM_STR);
            cipher.init(Cipher.ENCRYPT_MODE, KEY);
            var encryptStrBytes = cipher.doFinal(strBytes);
            return base64Encoder.encodeToString(encryptStrBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对str进行AES解密
     *
     * @param str 需要解密的字符串
     * @return AES解密后的字符串
     */
    public static String getDecryptString(String str) {
        try {
            var base64Decoder = Base64.getDecoder();
            var strBytes = base64Decoder.decode(str);
            var cipher = Cipher.getInstance(ALGORITHM_STR);
            cipher.init(Cipher.DECRYPT_MODE, KEY);
            var decryptStrBytes = cipher.doFinal(strBytes);
            return new String(decryptStrBytes, StringUtils.DEFAULT_CHARSET_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
