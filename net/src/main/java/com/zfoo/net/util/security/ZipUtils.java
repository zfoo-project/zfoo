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

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.*;

/**
 * @author godotg
 */
public abstract class ZipUtils {

    private static final int BUFFER_SIZE = IOUtils.ONE_BYTE;

    // compression level (0-9)，只能是0-9
    private static final int COMPRESS_LEVEL = 5;

    public static byte[] zip(byte[] bytes) {
        // deflate  [dɪ'fleɪt]  v.抽出空气; 缩小
        Deflater deflater = new Deflater(COMPRESS_LEVEL);
        deflater.setInput(bytes);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            baos.write(buffer, 0, count);
        }
        deflater.end();

        IOUtils.closeIO(baos);

        return baos.toByteArray();
    }

    public static byte[] unZip(byte[] bytes) {
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                baos.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(baos);
        }
        return baos.toByteArray();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * default charset is UTF-8
     */
    public static void zip(String[] sourceFilePaths, String zipFilePath) {
        FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        try {
            fos = new FileOutputStream(zipFilePath);
            zipOut = new ZipOutputStream(fos);
            for (var sourceFilePath : sourceFilePaths) {
                var file = new File(sourceFilePath);
                if (file.isDirectory()) {
                    var files = FileUtils.getAllReadableFiles(file);
                    for (var f : files) {
                        if (f.isHidden()) {
                            continue;
                        }
                        var fis = new FileInputStream(f);
                        var subPath = StringUtils.substringAfterFirst(f.getAbsolutePath(), file.getParentFile().getAbsolutePath());
                        subPath = StringUtils.substringAfterFirst(subPath, file.getName());
                        ZipEntry zipEntry = new ZipEntry(file.getName() + subPath);
                        zipOut.putNextEntry(zipEntry);
                        IOUtils.copy(fis, zipOut);
                        IOUtils.closeIO(fis);
                    }
                } else {
                    var fis = new FileInputStream(file);
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOut.putNextEntry(zipEntry);
                    IOUtils.copy(fis, zipOut);
                    IOUtils.closeIO(fis);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(zipOut, fos);
        }
    }


    /**
     * default charset is UTF-8
     */
    public static void unzip(String zipFilePath, String destDirectory) {
        try {
            unzip(FileUtils.openInputStream(new File(zipFilePath)), destDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unzip(InputStream zipFileInputStream, String destDirectory) throws IOException {
        FileUtils.createDirectory(destDirectory);
        var zipIn = new ZipInputStream(zipFileInputStream);
        var entry = zipIn.getNextEntry();
        // 遍历ZIP文件中的所有条目
        while (entry != null) {
            var filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // 如果条目是文件，则解压该文件
                var fileOutputStream = FileUtils.openOutputStream(new File(filePath), false);
                IOUtils.copy(zipIn, fileOutputStream);
                IOUtils.closeIO(fileOutputStream);
            } else {
                // 如果条目是目录，则创建目录
                FileUtils.createDirectory(filePath);
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }


}
