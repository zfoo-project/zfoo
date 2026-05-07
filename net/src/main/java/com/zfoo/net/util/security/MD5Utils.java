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

import com.zfoo.protocol.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 (Message-Digest Algorithm 5) was invented by MIT's Laboratory for Computer Science
 * and RSA Data Security Inc. in the early 1990s, evolving from MD2, MD3, and MD4.
 * <p>
 * MD5 converts an arbitrary-length byte sequence into a 128-bit integer using a one-way hash.
 * <p>
 * In other words, even with access to the source code and algorithm description, it is impossible
 * to reverse an MD5 hash back to its original input.
 * <p>
 * Mathematically this is because there are infinitely many possible input strings — similar
 * to a mathematical function that has no inverse.
 * <p>
 * A typical use of MD5 is to produce a fingerprint for a message (byte string) to detect tampering.
 * For example, you write text to readme.txt, compute its MD5 and record it, then distribute the file.
 * If anyone modifies the file, recomputing the MD5 will reveal the change.
 * With a trusted third party, MD5 can also prevent authors from repudiating their messages — this
 * is the basis of digital signatures.
 * MD5 is also widely used in encryption: many OSes store user passwords as MD5 hashes (or similar),
 * so the system compares the MD5 of the input at login with the stored MD5 — it never "knows" the plaintext password.
 *
 * @author godotg
 */
public abstract class MD5Utils {

    private static final String MD5_ALGORITHM = "MD5";

    /**
     * Hexadecimal character table.
     */
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static String strToMD5(String str) {
        if (StringUtils.isBlank(str)) {
            throw new NullPointerException();
        }

        return bytesToMD5(StringUtils.bytes(str));
    }

    // MD5 transforms an arbitrary-length byte sequence into a 128-bit integer
    public static String bytesToMD5(byte[] bytes) {
        try {
            var messageDigest = MessageDigest.getInstance(MD5_ALGORITHM);
            // Feed the input byte array into the digest
            messageDigest.update(bytes);

            // Compute the digest (16-byte / 128-bit result) and convert to hex string
            return byteArrayToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unknown exception during MD5 encryption", e);
        }
    }

    // Converts a byte array to a lowercase hexadecimal string
    private static String byteArrayToHex(byte[] bytes) {
        // Each byte is 8 bits = 2 hex characters (2^8 == 16^2)
        var resultCharArray = new char[bytes.length * 2];
        // Use bit shifts (faster than division) to extract high and low nibbles
        var index = 0;
        for (var b : bytes) {
            resultCharArray[index++] = HEX_CHARS[b >>> 4 & 0xf];
            resultCharArray[index++] = HEX_CHARS[b & 0xf];
        }

        // Assemble the character array into the final string
        return new String(resultCharArray);
    }
}
