import com.zfoo.java.ByteBuffer;
import com.zfoo.java.ProtocolManager;

import java.io.*;
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
        var bytes = toByteArray(new FileInputStream("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes"));
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
        System.out.println(format("equal [{}], not equal [{}]", equal, notEqual));
    }


    public static void normalReadTest() throws IOException {

//        var bytes = toByteArray(new FileInputStream("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes"));
//        var bytes = toByteArray(new FileInputStream("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes"));
//        var bytes = toByteArray(new FileInputStream("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes"));
//        var bytes = toByteArray(new FileInputStream("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes"));
        var bytes = toByteArray(new FileInputStream("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes"));

        var buffer = new ByteBuffer();
        buffer.writeBytes(bytes);
        var packet = ProtocolManager.read(buffer);

        System.out.println(packet);
    }


    // -----------------------------------------------------------------------------------------------------------------
    public static byte[] toByteArray(final InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        var bytes = output.toByteArray();
        return bytes;
    }
    public static final int EOF = -1;

    // The number of bytes in a byte
    public static final int ONE_BYTE = 1;
    // The number of bytes in a kilobyte
    public static final int BYTES_PER_KB = ONE_BYTE * 1024;
    // The number of bytes in a megabyte
    public static final int BYTES_PER_MB = BYTES_PER_KB * 1024;
    // The number of bytes in a gigabyte
    public static final long BYTES_PER_GB = BYTES_PER_MB * 1024;
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

    public static String format(final String template, final Object... args) {
        // 初始化定义好的长度以获得更好的性能
        var builder = new StringBuilder(template.length() + 50);

        // 记录已经处理到的位置
        var readIndex = 0;
        for (int i = 0; i < args.length; i++) {
            // 占位符所在位置
            var placeholderIndex = template.indexOf("{}", readIndex);
            // 剩余部分无占位符
            if (placeholderIndex == -1) {
                // 不带占位符的模板直接返回
                if (readIndex == 0) {
                    return template;
                }
                break;
            }

            builder.append(template, readIndex, placeholderIndex);
            builder.append(args[i]);
            readIndex = placeholderIndex + 2;
        }

        // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
        builder.append(template, readIndex, template.length());
        return builder.toString();
    }
}
