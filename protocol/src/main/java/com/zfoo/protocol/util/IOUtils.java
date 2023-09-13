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

package com.zfoo.protocol.util;

import java.io.*;

/**
 * @author godotg
 */
public abstract class IOUtils {

    // Represents the end-of-file (or stream)
    public static final int EOF = -1;

    // The number of bytes in a byte
    public static final int ONE_BYTE = 1;
    // The number of bytes in a kilobyte
    public static final int BYTES_PER_KB = ONE_BYTE * 1024;
    // The number of bytes in a megabyte
    public static final int BYTES_PER_MB = BYTES_PER_KB * 1024;
    // The number of bytes in a gigabyte
    public static final long BYTES_PER_GB = BYTES_PER_MB * 1024;

    public static final int BITS_PER_BYTE = ONE_BYTE * 8;
    public static final int BITS_PER_KB = BYTES_PER_KB * 8;
    public static final int BITS_PER_MB = BYTES_PER_MB * 8;
    public static final long BITS_PER_GB = BYTES_PER_GB * 8L;


    /**
     * Copies bytes from an InputStream to an OutputStream.
     * <p>
     * Large streams (over 2GB) will return a bytes copied value of -1
     * after the copy has completed since the correct number of bytes cannot be returned as an int.
     * For large streams use the copyLarge(InputStream, OutputStream) method.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied, or -1 if &gt; Integer.MAX_VALUE
     * @throws IOException IO异常
     */
    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        byte[] buffer = new byte[BYTES_PER_KB];
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }

        if (count > BYTES_PER_GB * 2L) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Gets the contents of an InputStream as a byte[].
     *
     * @param input the InputStream to read from
     * @return the requested byte array
     * @throws IOException IO异常
     */
    public static byte[] toByteArray(final InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        var bytes = output.toByteArray();
        IOUtils.closeIO(input, output);
        return bytes;
    }


    public static void closeIO(Closeable... closeables) {
        if (closeables == null) {
            return;
        }

        for (Closeable obj : closeables) {
            if (obj == null) {
                continue;
            }
            try {
                obj.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
