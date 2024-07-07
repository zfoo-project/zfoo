
import com.zfoo.scala.{ByteBuffer, ProtocolManager}

import java.io.IOException
import java.nio.file.{Files, Paths}

object Main {

  def main(args: Array[String]): Unit = {
    System.out.println("zfoo test")
    ProtocolManager.initProtocol
    byteBufferTest()
    compatibleTest()
    normalReadTest()
  }


  def byteBufferTest(): Unit = {
    byteTest()
    bytesTest()
    shortTest()
    intTest()
    longTest()
    floatTest()
    doubleTest()
    stringTest()
  }

  def byteTest(): Unit = {
    val value: Byte = 9
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeByte(value)
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(writerByteBuffer.toBytes)
    val readValue = readerByteBuffer.readByte
    assertEquals(value, readValue)
  }

  def bytesTest(): Unit = {
    val value = Array[Byte](1, 2, 3)
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeBytes(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readBytes(3)
    assertEquals(value, readValue)
  }

  def shortTest(): Unit = {
    val value: Short = 9999
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeShort(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readShort
    assertEquals(value, readValue)
  }

  def intTest(): Unit = {
    val value = 99999999
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeInt(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readInt
    assertEquals(value, readValue)
  }

  def longTest(): Unit = {
    val value = 9999999999999999L
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeLong(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readLong
    assertEquals(value, readValue)
  }

  def floatTest(): Unit = {
    val value = 999999.56F
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeFloat(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readFloat
    assertEquals(value, readValue)
  }

  def doubleTest(): Unit = {
    val value = 999999.56
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeDouble(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readDouble
    assertEquals(value, readValue)
  }

  def stringTest(): Unit = {
    val value = "aaa"
    val writerByteBuffer = new ByteBuffer
    writerByteBuffer.writeString(value)
    val bytes = writerByteBuffer.toBytes
    val readerByteBuffer = new ByteBuffer
    readerByteBuffer.writeBytes(bytes)
    val readValue = readerByteBuffer.readString
    assertEquals(value, readValue)
  }

  def assertEquals(a: Any, b: Any): Unit = {
    if (a.equals(b)) return
    throw new RuntimeException("a is not equals b")
  }

  def assertEquals(a: Array[Byte], b: Array[Byte]): Unit = {
    if (a eq b) return
    if (a != null && b != null && a.length == b.length) {
      for (i <- 0 until a.length) {
        assertEquals(a(i), b(i))
      }
      return
    }
    throw new RuntimeException("a is not equals b")
  }

  // -----------------------------------------------------------------------------------------------------------------
  // -----------------------------------------------------------------------------------------------------------------
  @throws[IOException]
  def compatibleTest(): Unit = {
    val bytes = Files.readAllBytes(Paths.get("C:\\github\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes"));
    val buffer = new ByteBuffer
    buffer.writeBytes(bytes)
    val packet = ProtocolManager.read(buffer)
    val newBuffer = new ByteBuffer
    ProtocolManager.write(newBuffer, packet)
    buffer.setReadOffset(0)
    newBuffer.setReadOffset(0)
    var equal = 0
    var notEqual = 0
    var i = 0
    while (i < buffer.getWriteOffset) {
      val a = buffer.readByte
      val b = newBuffer.readByte
      if (a == b) equal += 1
      else notEqual += 1

      i += 1
    }
    println("equal: " + equal)
    println("note equal: " + notEqual)
  }


  @throws[IOException]
  def normalReadTest(): Unit = {
    //    var bytes = Files.readAllBytes(Paths.get("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes"))
    //    var bytes = Files.readAllBytes(Paths.get("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes"))
    //    var bytes = Files.readAllBytes(Paths.get("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes"))
    //    var bytes = Files.readAllBytes(Paths.get("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes"))
    val bytes: Array[Byte] = Files.readAllBytes(Paths.get("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes"))
    val buffer: ByteBuffer = new ByteBuffer
    buffer.writeBytes(bytes)
    val packet = ProtocolManager.read(buffer)
    val newBuffer = new ByteBuffer()
    ProtocolManager.write(newBuffer, packet)
    val newPacket = ProtocolManager.read(newBuffer)

    println(packet)
    println(newPacket)
  }


  // -----------------------------------------------------------------------------------------------------------------

}
