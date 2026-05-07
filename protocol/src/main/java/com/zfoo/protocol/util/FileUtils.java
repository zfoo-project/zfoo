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
 * File operation utility class
 *
 * @author godotg
 */
public abstract class FileUtils {


    /**
     * Unix-like path separator
     */
    private static final String UNIX_SEPARATOR = StringUtils.SLASH;
    /**
     * Windows path separator
     */
    private static final String WINDOWS_SEPARATOR = StringUtils.BACK_SLASH;


    /**
     * Get the line separator for the current OS
     * <pre>
     * Windows: \r\n
     * Mac: \r
     * Linux: \n
     * </pre>
     */
    public static final String LS = System.lineSeparator();
    public static final String LS_REGEX = "\\r?\\n";
    public static final String UNIX_LS = "\\n";
    public static final String WINDOWS_LS = "\\r\\n";
    // The file copy buffer size (30 MB)
    private static final long FILE_COPY_BUFFER_SIZE = IOUtils.BYTES_PER_MB * 30;


    /**
     * User's current working directory
     *
     * @return absolute path
     */
    public static String getProAbsPath() {
        return System.getProperty("user.dir");
    }

    /**
     * Join parent and child paths
     *
     * @return absolute path
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

    //---------------------------------File search--------------------------------------

    /**
     * Depth-first file search
     *
     * @param file     the directory to search
     * @param fileName the target file name
     * @return the found file
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
     * Breadth-first file search
     *
     * @param fileOrDirectory the directory to search
     * @return all readable files
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
     * Search for a file
     *
     * @param file the file or directory to search
     * @return the file if found; null otherwise
     */
    public static File searchFileInProject(File file) {
        return searchFileInProject(new File(getProAbsPath()), file.getName());
    }


    /**
     * Search for a file
     * <p>
     * Note: the file name must include the extension
     *
     * @param fileName the full file name including extension
     * @return the file if found; null otherwise
     */
    public static File searchFileInProject(String fileName) {
        return searchFileInProject(new File(getProAbsPath()), fileName);
    }

    //---------------------------------File creation and deletion--------------------------------------

    /**
     * Create a file named fileName under the path directory
     *
     * @param path     the directory path
     * @param fileName the file name
     * @return the newly created File
     * @throws IOException on I/O error
     */
    public static File createFile(String path, String fileName) throws IOException {
        var file = createDirectory(path);

        var newFile = new File(file.getAbsoluteFile() + File.separator + fileName);
        if (newFile.exists()) {
            throw new RuntimeException(StringUtils.format("File already exists [fileName:{}]", fileName));
        }

        if (!newFile.createNewFile()) {
            throw new RuntimeException(StringUtils.format("Failed to create file [fileName:{}]", fileName));
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
            throw new RuntimeException(StringUtils.format("Failed to create file [fileName:{}]", fileName));
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

    // ------------------------------------------------File copy------------------------------------------------
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
     *                         should be the same as the original
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @throws IOException if the output file length is not the same as the input file length after the copy completes
     */
    public static void copyFile(final File srcFile, final File destFile) throws IOException {
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
        doCopyFile(srcFile, destFile);
    }

    public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && !destDir.isDirectory()) {
            throw new IllegalArgumentException(StringUtils.format("Destination [destDir:{}] is not a directory", destDir));
        }
        final File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile);
    }

    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        checkFileRequirements(srcDir, destDir);
        if (!srcDir.isDirectory()) {
            throw new IOException(StringUtils.format("Source [{}] exists but is not a directory", srcDir));
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException(StringUtils.format("Source [{}] and destination '" + destDir + "' are the same", srcDir));
        }
        doCopyDirectory(srcDir, destDir);
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
     * @throws IOException              if an error occurs
     * @throws IOException              if the output file length is not the same as the input file length after the
     *                                  copy completes
     * @throws IllegalArgumentException "Negative size" if the file is truncated so that the size is less than the
     *                                  position
     */
    private static void doCopyFile(final File srcFile, final File destFile) throws IOException {
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
    }

    private static void doCopyDirectory(final File srcDir, final File destDir) throws IOException {
        // recurse
        final File[] srcFiles = srcDir.listFiles();
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException(StringUtils.format("Destination [{}] exists but is not a directory", destDir));
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException(StringUtils.format("Destination [{}] directory cannot be created", destDir));
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException(StringUtils.format("Destination [{}] cannot be written to", destDir));
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (srcFile.isDirectory()) {
                doCopyDirectory(srcFile, dstFile);
            } else {
                doCopyFile(srcFile, dstFile);
            }
        }
    }


    // ------------------------------------------------File reading------------------------------------------------

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
        return StringUtils.joinWith(LS, readFileToStringList(file).toArray());
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
     * Write content to a file
     *
     * @param file    absolute file path
     * @param content content to write
     * @param append  whether to append
     */
    public static void writeStringToFile(File file, String content, boolean append) {
        // Byte stream
        FileOutputStream fileOutputStream = null;
        // Conversion stream: sets encoding; bridges bytes to characters and handles encoding issues
        OutputStreamWriter outputStreamWriter = null;
        // Buffered stream for improved efficiency
        BufferedWriter bufferedWriter = null;
        // Without buffering, each byte is written individually which is very slow for large data.
        // Buffered stream: writes to buffer first; flushes to file when full, greatly improving efficiency
        try {
            // Open file in append mode
            fileOutputStream = openOutputStream(file, append);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StringUtils.DEFAULT_CHARSET_NAME);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(content); // Write data
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Java GC reclaims only heap memory; physical resources must be closed explicitly
            // finally always executes, even when try or catch contains a return statement
            // Used to explicitly close database connections, network connections, and files
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

    //---------------------------------Opening and closing file streams--------------------------------------

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
     * If the file does not exist, create it. Prefer true to open in append mode
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


    // ------------------------------------------------File naming------------------------------------------------

    /**
     * Get the file extension (without the leading dot)
     *
     * @param fileName the file name
     * @return extension
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
     * Get the file name (without extension)
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
