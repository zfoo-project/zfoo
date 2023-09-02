package com.zfoo.net.base.nio.filechannel;

import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Ignore
public class FileWriterTest {

    @Test
    public void test() throws IOException {
        writeToFile();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    //fromFile-->toFile
    public void transferFrom() throws IOException {
        RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
        FileChannel toChannel = toFile.getChannel();

        long position = 0;
        long count = fromChannel.size();

        toChannel.transferFrom(fromChannel, position, count);
    }


    //fromFile-->toFile
    public void transferTo() throws IOException {
        RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
        FileChannel fromChannel = fromFile.getChannel();

        RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
        FileChannel toChannel = toFile.getChannel();

        long position = 0;
        long count = fromChannel.size();

        fromChannel.transferTo(position, count, toChannel);
    }

    public void writeToFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile("rainbow.txt", "rw");
        FileChannel fileChannel = file.getChannel();

        String newData = "New String to write to file..." + System.currentTimeMillis();

        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();

        while (buf.hasRemaining()) {
            fileChannel.write(buf);
        }
        fileChannel.close();
        file.close();
    }


}
