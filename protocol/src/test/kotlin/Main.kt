import com.zfoo.kotlin.ByteBuffer
import com.zfoo.kotlin.ProtocolManager
import com.zfoo.kotlin.ProtocolManager.Companion.initProtocol
import com.zfoo.kotlin.ProtocolManager.Companion.read
import com.zfoo.kotlin.ProtocolManager.Companion.write
import java.io.*
import java.nio.file.Files

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
    val bytes = Files.readAllBytes(File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes").toPath())
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
    println("equal: " + equal)
    println("not equal:" + notEqual)
}


@Throws(IOException::class)
fun normalReadTest() {
//    var bytes = Files.readAllBytes(File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes").toPath())
//    var bytes = Files.readAllBytes(File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes").toPath())
//    var bytes = Files.readAllBytes(File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes").toPath())
//    var bytes = Files.readAllBytes(File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes").toPath())
    val bytes = Files.readAllBytes(File("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes").toPath())

    val buffer = ByteBuffer()
    buffer.writeBytes(bytes)
    val packet = read(buffer)

    var newBuffer = ByteBuffer();
    ProtocolManager.write(newBuffer, packet)
    var newPacket = ProtocolManager.read(newBuffer)

    println(packet)
    println(newPacket)
}
