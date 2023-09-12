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
 * MD5的全称是Message-Digest Algorithm 5，在90年代初由MIT的计算机科学实验室和RSA Data Security Inc发明，经MD2、MD3和MD4发展而来。
 * <p>
 * MD5将任意长度的“字节串”变换成一个128bit的大整数，并且它是一个不可逆的字符串变换算法。
 * <p>
 * 换句话说就是，即使你看到源程序和算法描述，也无法将一个MD5的值变换回原始的字符串。
 * <p>
 * 从数学原理上说，是因为原始的字符串有无穷多个，这有点象不存在反函数的数学函数。
 * <p>
 * MD5的典型应用是对一段Message(字节串)产生fingerprint(指纹)，以防止被“篡改”。
 * <p>
 * 举个例子，你将一段话写在一个叫 readme.txt文件中，并对这个readme.txt产生一个MD5的值并记录在案，然后你可以传播这个文件给别人，
 * 别人如果修改了文件中的任何内容，你对这个文件重新计算MD5时就会发现。如果再有一个第三方的认证机构，用MD5还可以防止文件作者的“抵赖”，
 * 这就是所谓的数字签名应用。MD5还广泛用于加密和解密技术上，在很多操作系统中，用户的密码是以MD5值（或类似的其它算法）的方式保存的，
 * 用户Login的时候，系统是把用户输入的密码计算成MD5值，然后再去和系统中保存的MD5值进行比较，而系统并不“知道”用户的密码是什么。
 *
 * @author godotg
 */
public abstract class MD5Utils {

    private static final String MD5_ALGORITHM = "MD5";

    /**
     * 16进制字符
     */
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    public static String strToMD5(String str) {
        if (StringUtils.isBlank(str)) {
            throw new NullPointerException();
        }

        return bytesToMD5(StringUtils.bytes(str));
    }

    // MD5将任意长度的“字节串”变换成一个128bit的大整数
    public static String bytesToMD5(byte[] bytes) {
        try {

            var messageDigest = MessageDigest.getInstance(MD5_ALGORITHM);
            // inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(bytes);

            // 转换并返回结果，也是字节数组，包含16个元素
            // 字符数组转换成字符串返回，MD5将任意长度的字节数组变换成一个16个字节，128bit的大整数
            return byteArrayToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD加密出现未知异常", e);
        }
    }

    //下面这个函数用于将字节数组换成成16进制的字符串
    private static String byteArrayToHex(byte[] bytes) {
        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        var resultCharArray = new char[bytes.length * 2];
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        var index = 0;
        for (var b : bytes) {
            resultCharArray[index++] = HEX_CHARS[b >>> 4 & 0xf];
            resultCharArray[index++] = HEX_CHARS[b & 0xf];
        }

        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }
}
