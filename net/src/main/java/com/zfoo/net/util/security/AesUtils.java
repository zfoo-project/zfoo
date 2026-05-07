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

package com.zfoo.net.util.security;


import com.zfoo.protocol.util.FastThreadLocalAdapter;
import com.zfoo.protocol.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.function.Supplier;

/**
 * AES encryption and decryption utilities.
 * <p>
 * Default mode: AES/ECB/PKCS5Padding
 *
 * @author godotg
 */
public abstract class AesUtils {

    private static final Key KEY;
    private static final String KEY_STR = "=jE[`B],YO24Vt+Akh&}D7@s9l1uLKP)";

    /**
     * Key algorithm name.
     */
    private static final String ALGORITHM = "AES";
    /**
     * Cipher transformation: algorithm / operation mode / padding scheme.
     */
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";

    static {
        try {
            KEY = new SecretKeySpec(KEY_STR.getBytes(StringUtils.DEFAULT_CHARSET_NAME), ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final FastThreadLocalAdapter<Cipher> LOCAL_ENCRYPT_CIPHER = new FastThreadLocalAdapter<>(new Supplier<>() {
        @Override
        public Cipher get() {
            try {
                Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
                cipher.init(Cipher.ENCRYPT_MODE, KEY);
                return cipher;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    });

    private static final FastThreadLocalAdapter<Cipher> LOCAL_DECRYPT_CIPHER = new FastThreadLocalAdapter<Cipher>(new Supplier<Cipher>() {
        @Override
        public Cipher get() {
            try {
                var cipher = Cipher.getInstance(ALGORITHM_STR);
                cipher.init(Cipher.DECRYPT_MODE, KEY);
                return cipher;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    });


    /**
     * AES-encrypt the given string.
     *
     * @param str the plaintext string to encrypt
     * @return Base64-encoded AES-encrypted string
     */
    public static String getEncryptString(String str) {
        try {
            var base64Encoder = Base64.getEncoder();
            var strBytes = StringUtils.bytes(str);
            var encryptStrBytes = encrypt(strBytes);
            return base64Encoder.encodeToString(encryptStrBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt(byte[] bytes) {
        try {
            return LOCAL_ENCRYPT_CIPHER.get().doFinal(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES-decrypt the given Base64-encoded string.
     *
     * @param str the Base64-encoded AES-encrypted string to decrypt
     * @return the decrypted plaintext string
     */
    public static String getDecryptString(String str) {
        try {
            var base64Decoder = Base64.getDecoder();
            var strBytes = base64Decoder.decode(str);
            var decryptStrBytes = decrypt(strBytes);
            return StringUtils.bytesToString(decryptStrBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] bytes) {
        try {
            return LOCAL_DECRYPT_CIPHER.get().doFinal(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
