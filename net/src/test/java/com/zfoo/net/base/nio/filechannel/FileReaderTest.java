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
        // FileChannel cannot be set to non-blocking mode; it always operates in blocking mode.
        // NIO operations always start from a Channel; data can be read into or written from a Buffer.
        FileChannel fileChannel = file.getChannel();
        // Create a buffer
        ByteBuffer buf = ByteBuffer.allocate(1000000000);
        // Read into the buffer
        int bytesRead = fileChannel.read(buf);

        while (bytesRead != -1) {

            System.out.println("Read " + bytesRead);
            // Make buffer ready for reading
            buf.flip();

            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }

            // Make buffer ready for writing
            buf.clear();
            bytesRead = fileChannel.read(buf);
        }
        fileChannel.close();
        file.close();

        ThreadUtils.sleep(Long.MAX_VALUE);
    }


}
