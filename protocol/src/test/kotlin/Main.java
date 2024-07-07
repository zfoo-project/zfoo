import com.zfoo.java.ByteBuffer;
import com.zfoo.java.ProtocolManager;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * @author godotg
 */
public class Main {



    public static void main(String[] args) throws IOException {
        System.out.println("zfoo test");
        ProtocolManager.initProtocol();
        byteBufferTest();
        compatibleTest();
        normalReadTest();
    }


    public static void byteBufferTest() {
        byteTest();
        bytesTest();
        shortTest();
        intTest();
        longTest();
        floatTest();
        doubleTest();
        stringTest();
    }

    public static void byteTest() {
        byte value = 9;
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeByte(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        byte readValue = readerByteBuffer.readByte();
        assertEquals(value, readValue);
    }

    public static void bytesTest() {
        var value = new byte[]{1, 2, 3};
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeBytes(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        var readValue = readerByteBuffer.readBytes(3);
        assertEquals(value, readValue);
    }

    public static void shortTest() {
        short value = 9999;
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeShort(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        short readValue = readerByteBuffer.readShort();
        assertEquals(value, readValue);
    }

    public static void intTest() {
        int value = 99999999;
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeInt(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        int readValue = readerByteBuffer.readInt();
        assertEquals(value, readValue);
    }

    public static void longTest() {
        long value = 9999999999999999L;
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeLong(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        long readValue = readerByteBuffer.readLong();
        assertEquals(value, readValue);
    }

    public static void floatTest() {
        float value = 999999.56F;
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeFloat(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        float readValue = readerByteBuffer.readFloat();
        assertEquals(value, readValue);
    }

    public static void doubleTest() {
        double value = 999999.56;
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeDouble(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        double readValue = readerByteBuffer.readDouble();
        assertEquals(value, readValue);
    }

    public static void stringTest() {
        String value = "aaa";
        ByteBuffer writerByteBuffer = new ByteBuffer();
        writerByteBuffer.writeString(value);
        byte[] bytes = writerByteBuffer.toBytes();

        ByteBuffer readerByteBuffer = new ByteBuffer();
        readerByteBuffer.writeBytes(bytes);
        String readValue = readerByteBuffer.readString();
        assertEquals(value, readValue);
    }

    public static void assertEquals(Object a, Object b) {
        if (a.equals(b)) {
            return;
        }

        throw new RuntimeException("a is not equals b");
    }

    public static void assertEquals(byte[] a, byte[] b) {
        if (a == b) {
            return;
        }
        if (a != null && b != null && a.length == b.length) {
            for (var i = 0; i < a.length; i++) {
                assertEquals(a[i], b[i]);
            }
            return;
        }
        throw new RuntimeException("a is not equals b");
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void compatibleTest() throws IOException {
        var bytes = Files.readAllBytes(new File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes").toPath());
        var buffer = new ByteBuffer();
        buffer.writeBytes(bytes);

        var packet = ProtocolManager.read(buffer);

        var newBuffer = new ByteBuffer();
        ProtocolManager.write(newBuffer, packet);

        buffer.setReadOffset(0);
        newBuffer.setReadOffset(0);

        var equal = 0;
        var notEqual = 0;
        for (int i = 0; i < buffer.writeOffset(); i++) {
            var a = buffer.readByte();
            var b = newBuffer.readByte();
            if (a == b) {
                equal++;
            } else {
                notEqual++;
            }
        }
        System.out.println("equal: " + equal);
        System.out.println("not equal: " + notEqual);
    }


    public static void normalReadTest() throws IOException {

//        var bytes = Files.readAllBytes(new File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes").toPath());
//        var bytes = Files.readAllBytes(new File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes").toPath());
//        var bytes = Files.readAllBytes(new File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes").toPath());
//        var bytes = Files.readAllBytes(new File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes").toPath());
        var bytes = Files.readAllBytes(new File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes").toPath());

        var buffer = new ByteBuffer();
        buffer.writeBytes(bytes);
        var packet = ProtocolManager.read(buffer);

        System.out.println(packet);
    }


}
