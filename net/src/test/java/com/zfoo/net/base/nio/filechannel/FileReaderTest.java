package com.zfoo.net.base.nio.filechannel;

import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Ignore
public class FileReaderTest {

    @Test
    public void test() throws IOException {
        RandomAccessFile file = new RandomAccessFile("rainbow.txt", "rw");
        // NIO always opens from a Channel; data can be read from Channel into Buffer or written from Buffer to Channel
        FileChannel fileChannel = file.getChannel();
        //create buffer with capacity of 48 bytes
        ByteBuffer buf = ByteBuffer.allocate(1000000000);
        //read into buffer
        int bytesRead = fileChannel.read(buf);

        while (bytesRead != -1) {

            System.out.println("Read " + bytesRead);
            //make buffer ready for read
            buf.flip();

            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }

            //make buffer ready for writing
            buf.clear();
            bytesRead = fileChannel.read(buf);
        }
        fileChannel.close();
        file.close();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


}
