import com.zfoo.kotlin.ByteBuffer
import com.zfoo.kotlin.ProtocolManager
import com.zfoo.kotlin.ProtocolManager.Companion.initProtocol
import com.zfoo.kotlin.ProtocolManager.Companion.read
import com.zfoo.kotlin.ProtocolManager.Companion.write
import java.io.*

/**
 * @author godotg
 */
fun main() {
    println("zfoo test")
    initProtocol()
    byteBufferTest()
    compatibleTest()
    normalReadTest()
}


fun byteBufferTest() {
    byteTest()
    bytesTest()
    shortTest()
    intTest()
    longTest()
    floatTest()
    doubleTest()
    stringTest()
}

fun byteTest() {
    val value: Byte = 9
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeByte(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readByte()
    assertObjectEquals(value, readValue)
}

fun bytesTest() {
    val value = byteArrayOf(1, 2, 3)
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeBytes(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readBytes(3)
    assertArrayEquals(value, readValue)
}

fun shortTest() {
    val value: Short = 9999
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeShort(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readShort()
    assertObjectEquals(value, readValue)
}

fun intTest() {
    val value = 99999999
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeInt(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readInt()
    assertObjectEquals(value, readValue)
}

fun longTest() {
    val value = 9999999999999999L
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeLong(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readLong()
    assertObjectEquals(value, readValue)
}

fun floatTest() {
    val value = 999999.56f
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeFloat(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readFloat()
    assertObjectEquals(value, readValue)
}

fun doubleTest() {
    val value = 999999.56
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeDouble(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readDouble()
    assertObjectEquals(value, readValue)
}

fun stringTest() {
    val value = "aaa"
    val writerByteBuffer = ByteBuffer()
    writerByteBuffer.writeString(value)
    val bytes = writerByteBuffer.toBytes()

    val readerByteBuffer = ByteBuffer()
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readString()
    assertObjectEquals(value, readValue)
}

fun assertObjectEquals(a: Any, b: Any) {
    if (a == b) {
        return
    }

    throw RuntimeException("a is not equals b")
}

fun assertArrayEquals(a: ByteArray?, b: ByteArray?) {
    if (a == b) {
        return
    }
    if (a != null && b != null && a.size == b.size) {
        for (i in a.indices) {
            assertObjectEquals(a[i], b[i])
        }
        return
    }
    throw RuntimeException("a is not equals b")
}

// -----------------------------------------------------------------------------------------------------------------
@Throws(IOException::class)
fun compatibleTest() {
    val bytes =
        toByteArray(FileInputStream("C:\\github\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes"))
    val buffer = ByteBuffer()
    buffer.writeBytes(bytes)

    val packet = read(buffer)

    val newBuffer = ByteBuffer()
    write(newBuffer, packet!!)

    buffer.setReadOffset(0)
    newBuffer.setReadOffset(0)

    var equal = 0
    var notEqual = 0
    for (i in 0 until buffer.writeOffset()) {
        val a = buffer.readByte()
        val b = newBuffer.readByte()
        if (a == b) {
            equal++
        } else {
            notEqual++
        }
    }
    println(format("equal [{}], not equal [{}]", equal, notEqual))
}


@Throws(IOException::class)
fun normalReadTest() {
//    var bytes = toByteArray(FileInputStream("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes"));
//    var bytes = toByteArray(FileInputStream("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes"));
//    var bytes = toByteArray(FileInputStream ("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes"));
    var bytes =
        toByteArray(FileInputStream("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes"));
//    val bytes = toByteArray(FileInputStream("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes"))

    val buffer = ByteBuffer()
    buffer.writeBytes(bytes)
    val packet = read(buffer)

    var newBuffer = ByteBuffer();
    ProtocolManager.write(newBuffer, packet)
    var newPacket = ProtocolManager.read(newBuffer)

    println(packet)
    println(newPacket)
}


// -----------------------------------------------------------------------------------------------------------------
@Throws(IOException::class)
fun toByteArray(input: InputStream): ByteArray {
    val output = ByteArrayOutputStream()
    copy(input, output)
    val bytes = output.toByteArray()
    return bytes
}

const val EOF: Int = -1

// The number of bytes in a byte
const val ONE_BYTE: Int = 1

// The number of bytes in a kilobyte
const val BYTES_PER_KB: Int = ONE_BYTE * 1024

// The number of bytes in a megabyte
const val BYTES_PER_MB: Int = BYTES_PER_KB * 1024

// The number of bytes in a gigabyte
const val BYTES_PER_GB: Long = (BYTES_PER_MB * 1024).toLong()

@Throws(IOException::class)
fun copy(input: InputStream, output: OutputStream): Int {
    val buffer = ByteArray(BYTES_PER_KB)
    var count: Long = 0
    var n: Int
    while (EOF != (input.read(buffer).also { n = it })) {
        output.write(buffer, 0, n)
        count += n.toLong()
    }

    if (count > BYTES_PER_GB * 2L) {
        return -1
    }
    return count.toInt()
}

fun format(template: String, vararg args: Any?): String {
    // 初始化定义好的长度以获得更好的性能
    val builder = StringBuilder(template.length + 50)

    // 记录已经处理到的位置
    var readIndex = 0
    for (i in args.indices) {
        // 占位符所在位置
        val placeholderIndex = template.indexOf("{}", readIndex)
        // 剩余部分无占位符
        if (placeholderIndex == -1) {
            // 不带占位符的模板直接返回
            if (readIndex == 0) {
                return template
            }
            break
        }

        builder.append(template, readIndex, placeholderIndex)
        builder.append(args[i])
        readIndex = placeholderIndex + 2
    }

    // 字符串模板剩余部分不再包含占位符，加入剩余部分后返回结果
    builder.append(template, readIndex, template.length)
    return builder.toString()
}
