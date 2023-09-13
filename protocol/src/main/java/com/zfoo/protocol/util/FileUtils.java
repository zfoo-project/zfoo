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

import com.zfoo.protocol.collection.ArrayUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 文件操作工具类
 *
 * @author godotg
 */
public abstract class FileUtils {


    /**
     * 类Unix路径分隔符
     */
    private static final String UNIX_SEPARATOR = StringUtils.SLASH;
    /**
     * Windows路径分隔符
     */
    private static final String WINDOWS_SEPARATOR = StringUtils.BACK_SLASH;


    /**
     * 获取当前系统的换行分隔符
     * <pre>
     * Windows: \r\n
     * Mac: \r
     * Linux: \n
     * </pre>
     */
    public static final String LS = System.lineSeparator();
    public static final String UNIX_LS = "\\n";
    public static final String WINDOWS_LS = "\\r\\n";
    // The file copy buffer size (30 MB)
    private static final long FILE_COPY_BUFFER_SIZE = IOUtils.BYTES_PER_MB * 30;


    /**
     * User's current working directory
     *
     * @return 绝对路径路径
     */
    public static String getProAbsPath() {
        return System.getProperty("user.dir");
    }

    /**
     * 连接父路径和子路径
     *
     * @return 绝对路径
     */
    public static String joinPath(String parentPath, String childPath) {
        if (StringUtils.isEmpty(parentPath)) {
            return new File(childPath).getAbsolutePath();
        }

        if (StringUtils.isEmpty(childPath)) {
            return new File(parentPath).getAbsolutePath();
        }

        return new File(parentPath, childPath).getAbsolutePath();
    }

    //---------------------------------搜索文件--------------------------------------

    /**
     * 深度优先搜索文件
     *
     * @param file     需要搜索的文件
     * @param fileName 需要搜索的目标文件名
     * @return 搜索到的文件
     */
    private static File searchFileInProject(File file, String fileName) {
        if (file.isFile() && file.getName().equals(fileName)) {
            return file;
        }
        if (file.isDirectory()) {
            var files = file.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                return null;
            }

            for (var f : files) {
                File result = searchFileInProject(f, fileName);
                if (result == null) {
                    continue;
                }
                return result;
            }
        }
        return null;
    }

    /**
     * 广度优先搜索文件
     *
     * @param fileOrDirectory 需要查找的文件夹
     * @return 所有可读的文件
     */
    public static List<File> getAllReadableFiles(File fileOrDirectory) {
        List<File> readableFileList = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        queue.add(fileOrDirectory);
        while (!queue.isEmpty()) {
            var file = queue.poll();
            if (file.isDirectory()) {
                for (var f : file.listFiles()) {
                    queue.offer(f);
                }
                continue;
            }

            if (file.canRead()) {
                readableFileList.add(file);
            }
        }

        return readableFileList;
    }

    /**
     * 搜索文件
     *
     * @param file 需要查找的文件
     * @return 如果没有搜索到返回null
     */
    public static File searchFileInProject(File file) {
        return searchFileInProject(new File(getProAbsPath()), file.getName());
    }


    /**
     * 搜索文件
     * <p>
     * 注意：文件名必须是文件全称，包括文件名的后缀
     *
     * @param fileName 文件名的全称，包括文件名的后缀
     * @return 如果没有搜索到返回null
     */
    public static File searchFileInProject(String fileName) {
        return searchFileInProject(new File(getProAbsPath()), fileName);
    }

    //---------------------------------创建，删除文件--------------------------------------

    /**
     * 在path文件夹下创建一个fileName文件
     *
     * @param path     路径
     * @param fileName 文件名
     * @return 新创建的File
     * @throws IOException IO异常
     */
    public static File createFile(String path, String fileName) throws IOException {
        var file = createDirectory(path);

        var newFile = new File(file.getAbsoluteFile() + File.separator + fileName);
        if (newFile.exists()) {
            throw new RuntimeException(StringUtils.format("文件已经存在[fileName:{}]", fileName));
        }

        if (!newFile.createNewFile()) {
            throw new RuntimeException(StringUtils.format("创建文件[fileName:{}]失败", fileName));
        }
        return newFile;
    }

    public static File getOrCreateFile(String path, String fileName) throws IOException {
        var file = createDirectory(path);

        var newFile = new File(file.getAbsoluteFile() + File.separator + fileName);
        if (newFile.exists()) {
            return newFile;
        }

        if (!newFile.createNewFile()) {
            throw new RuntimeException(StringUtils.format("创建文件[fileName:{}]失败", fileName));
        }
        return newFile;
    }

    public static File createDirectory(String path) {
        var file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException(StringUtils.format("Directory [file:{}] could not be created", file));
            }
        }
        return file;
    }


    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     *
     * @param file file or directory to delete, must not be null
     */
    public static void deleteFile(final File file) {
        if (file.isDirectory()) {
            var files = file.listFiles();
            if (files != null) {
                for (var subFile : files) {
                    deleteFile(subFile);
                }
            }
            if (!file.delete()) {
                throw new RuntimeException("Unable to delete file directory: " + file);
            }
        } else {
            boolean filePresent = file.exists();
            if (filePresent) {
                if (!file.delete()) {
                    throw new RuntimeException("Unable to delete file: " + file);
                }
            }
        }
    }

    // ------------------------------------------------复制文件------------------------------------------------

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the
     * specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this
     * method will overwrite it.
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcFile  an existing file to copy, must not be null
     * @param destFile the new file, must not be null
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @throws IOException if the output file length is not the same as the input file length after the copy completes
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    public static void copyFileToDirectory(final File srcFile, final File destDir, final boolean preserveFileDate)
            throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && !destDir.isDirectory()) {
            throw new IllegalArgumentException(StringUtils.format("Destination [destDir:{}] is not a directory", destDir));
        }
        final File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file
     * to the specified destination file.
     * The directory holding the destination file is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcFile          an existing file to copy, must not be {@code null}
     * @param destFile         the new file, must not be {@code null}
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @throws IOException if the output file length is not the same as the input file length after the copy completes
     */
    public static void copyFile(final File srcFile, final File destFile,
                                final boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcFile, destFile);
        if (srcFile.isDirectory()) {
            throw new IOException(StringUtils.format("Source [srcFile:{}] exists but is a directory", srcFile));
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException(StringUtils.format("Source [srcFile:{}] and destination [destFile:{}] are the same", srcFile, destFile));
        }
        final File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException(StringUtils.format("Destination [parentFile:{}] directory cannot be created", parentFile));
            }
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException(StringUtils.format("Destination [destFile:{}] exists but is read-only", destFile));
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * checks requirements for file copy
     *
     * @param src  the source file
     * @param dest the destination
     * @throws FileNotFoundException if the destination does not exist
     */
    private static void checkFileRequirements(File src, File dest) throws FileNotFoundException {
        if (src == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (dest == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!src.exists()) {
            throw new FileNotFoundException(StringUtils.format("Source [src:{}] does not exist", src));
        }
    }

    /**
     * Internal copy file method.
     * This caches the original file length, and throws an IOException
     * if the output file length is different from the current input file length.
     * So it may fail if the file changes size.
     * It may also fail with "IllegalArgumentException: Negative size" if the input file is truncated part way
     * through copying the data and the new file size is less than the current position.
     *
     * @param srcFile          the validated source file, must not be {@code null}
     * @param destFile         the validated destination file, must not be {@code null}
     * @param preserveFileDate whether to preserve the file date
     * @throws IOException              if an error occurs
     * @throws IOException              if the output file length is not the same as the input file length after the
     *                                  copy completes
     * @throws IllegalArgumentException "Negative size" if the file is truncated so that the size is less than the
     *                                  position
     */
    private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
            throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException(StringUtils.format("Destination [destFile:{}] exists but is a directory", destFile));
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            final long size = input.size(); // TODO See IO-386
            long pos = 0;
            long count;
            while (pos < size) {
                final long remain = size - pos;
                count = Math.min(remain, FILE_COPY_BUFFER_SIZE);
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) { // IO-385 - can happen if file is truncated after caching the size
                    break; // ensure we don't loop forever
                }
                pos += bytesCopied;
            }
        } finally {
            IOUtils.closeIO(output, fos, input, fis);
        }

        final long srcLen = srcFile.length(); // TODO See IO-386
        final long dstLen = destFile.length(); // TODO See IO-386
        if (srcLen != dstLen) {
            throw new IOException(StringUtils.format("Failed to copy full contents from [srcFile:{}] to [destFile:{}] Expected length:[srcLen:{}] Actual [dstLen:{}]"
                    , srcFile, destFile, srcLen, dstLen));
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }


    // ------------------------------------------------读取文件------------------------------------------------

    /**
     * Reads the contents of a file into a byte array.
     * The file is always closed.
     *
     * @param file the file to read, must not be {@code null}
     * @return the file contents, never {@code null}
     * @throws IOException in case of an I/O error
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return IOUtils.toByteArray(in); // Do NOT use file.length() - see IO-453
        } finally {
            IOUtils.closeIO(in);
        }
    }


    public static String readFileToString(final File file) {
        return StringUtils.joinWith(StringUtils.EMPTY, readFileToStringList(file).toArray());
    }

    public static List<String> readFileToStringList(final File file) {
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        var list = new ArrayList<String>();
        try {
            fileInputStream = openInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream, StringUtils.DEFAULT_CHARSET_NAME);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(bufferedReader, inputStreamReader, fileInputStream);
        }
        return list;
    }

    /**
     * 写入一个content
     *
     * @param file    文件的绝对路径
     * @param content 写入的内容
     * @param append  是否追加
     */
    public static void writeStringToFile(File file, String content, boolean append) {
        // 字节流
        FileOutputStream fileOutputStream = null;
        // 转换流，设置编码集和解码集 .处理乱码问题，是字节到字符的桥梁
        OutputStreamWriter outputStreamWriter = null;
        //处理流中的缓冲流，提高效率
        BufferedWriter bufferedWriter = null;
        // 如果不用缓冲流的话，程序是读一个数据，写一个数据，这样在数据量大的程序中非常影响效率。
        // 缓冲流作用是把数据先写入缓冲区，等缓冲区满了，再把数据写到文件里。这样效率就大大提高了
        try {
            // 以追加的方式打开文件
            fileOutputStream = openOutputStream(file, append);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StringUtils.DEFAULT_CHARSET_NAME);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(content);// 写数据
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Java的垃圾回收机制不会回收任何的物理资源，只会回收堆内存中对象所占用的内存
            // finally总会被执行，即使try块中和catch块中有return，也会被执行。
            // 用来显示回收数据库连接，网络连接，磁盘文件
            IOUtils.closeIO(bufferedWriter, outputStreamWriter, fileOutputStream);
        }
    }

    public static void writeInputStreamToFile(File file, InputStream inputStream) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openOutputStream(file, true);
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(fileOutputStream);
        }
    }

    //---------------------------------打开，关闭，文件流--------------------------------------

    /**
     * Opens a {@link FileInputStream} for the specified file, providing better
     * error messages than simply calling <code>new FileInputStream(file)</code>.
     *
     * @param file the file to open for input, must not be {@code null}
     * @return a new {@link FileInputStream} for the specified file
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException           if the file object is a directory
     * @throws IOException           if the file cannot be read
     */
    public static FileInputStream openInputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException(StringUtils.format("File [file:{}] exists but is a directory", file));
            }
            if (!file.canRead()) {
                throw new IOException(StringUtils.format("File [file:{}] cannot be read", file));
            }
        } else {
            throw new FileNotFoundException(StringUtils.format("File [file:{}] does not exist", file));
        }
        return new FileInputStream(file);
    }

    /**
     * 如果文件不存在，则创建该文件。最好指定为true，以追加的方式打开文件
     * <p>
     * The parent directory will be created if it does not exist.The file will be created if it does not exist.
     *
     * @param file   the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     */
    public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException(StringUtils.format("File [file:{}] exists but is a directory", file));
            }
            if (!file.canWrite()) {
                throw new IOException(StringUtils.format("File [file:{}] cannot be written to", file));
            }
        } else {
            final File parentFile = file.getParentFile();
            if (parentFile != null) {
                if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                    throw new IOException(StringUtils.format("Directory [parentFile:{}] could not be created", parentFile));
                }
            }
        }
        return new FileOutputStream(file, append);
    }


    // ------------------------------------------------文件名称------------------------------------------------

    /**
     * 获得文件的扩展名，扩展名不带“.”
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String fileExtName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return StringUtils.EMPTY;
        }
        var fileExtName = StringUtils.substringAfterLast(fileName, StringUtils.PERIOD);
        if (StringUtils.isBlank(fileExtName) || fileExtName.contains(UNIX_SEPARATOR) || fileExtName.contains(WINDOWS_SEPARATOR)) {
            return StringUtils.EMPTY;
        }
        return fileExtName;
    }

    /**
     * 获得文件的名称，不带“.”和扩展名
     */
    public static String fileSimpleName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return StringUtils.EMPTY;
        }
        var fileSimpleName = StringUtils.substringBeforeLast(fileName, StringUtils.PERIOD);
        if (StringUtils.isBlank(fileSimpleName) || fileSimpleName.contains(UNIX_SEPARATOR) || fileSimpleName.contains(WINDOWS_SEPARATOR)) {
            return StringUtils.EMPTY;
        }
        return fileSimpleName;
    }

}
