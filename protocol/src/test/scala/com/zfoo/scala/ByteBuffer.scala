package com.zfoo.scala
import java.nio.charset.Charset
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class ByteBuffer {
  val INIT_SIZE: Int = 128
  val MAX_SIZE: Int = 655537
  // *******************************************String***************************************************
  val DEFAULT_CHARSET_NAME: String = "UTF-8"
  val DEFAULT_CHARSET: Charset = Charset.forName(DEFAULT_CHARSET_NAME)

  private var buffer: Array[Byte] = new Array[Byte](INIT_SIZE)
  private var writeOffset: Int = 0
  private var readOffset: Int = 0

  def adjustPadding(predictionLength: Int, beforewriteIndex: Int): Unit = {
    // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
    val currentwriteIndex: Int = writeOffset
    val predictionCount: Int = writeIntCount(predictionLength)
    val length: Int = currentwriteIndex - beforewriteIndex - predictionCount
    val lengthCount: Int = writeIntCount(length)
    val padding: Int = lengthCount - predictionCount
    if (padding == 0) {
      writeOffset = beforewriteIndex
      writeInt(length)
      writeOffset = currentwriteIndex
    }
    else {
      val bytes: Array[Byte] = new Array[Byte](length)
      System.arraycopy(buffer, currentwriteIndex - length, bytes, 0, length)
      writeOffset = beforewriteIndex
      writeInt(length)
      writeBytes(bytes)
    }
  }

  def compatibleRead(beforeReadIndex: Int, length: Int): Boolean = length != -1 && readOffset < length + beforeReadIndex

  // -------------------------------------------------get/set-------------------------------------------------
  def getBuffer: Array[Byte] = buffer

  def getWriteOffset: Int = writeOffset

  def setWriteOffset(writeIndex: Int): Unit = {
    if (writeIndex > buffer.length) throw new RuntimeException("writeIndex[" + writeIndex + "] out of bounds exception: readOffset: " + readOffset + ", writeOffset: " + writeOffset + "(expected: 0 <= readOffset <= writeOffset <= capacity:" + buffer.length + ")")
    writeOffset = writeIndex
  }

  def getReadOffset: Int = readOffset

  def setReadOffset(readIndex: Int): Unit = {
    if (readIndex > writeOffset) throw new RuntimeException("readIndex[" + readIndex + "] out of bounds exception: readOffset: " + readOffset + ", writeOffset: " + writeOffset + "(expected: 0 <= readOffset <= writeOffset <= capacity:" + buffer.length + ")")
    readOffset = readIndex
  }

  def getBytes: Array[Byte] = buffer

  def toBytes: Array[Byte] = buffer.slice(0, writeOffset)

  def isReadable: Boolean = writeOffset > readOffset

  // -------------------------------------------------write/read-------------------------------------------------
  def writeBool(value: Boolean): Unit = {
    ensureCapacity(1)
    buffer(writeOffset) = if (value) 1.toByte else 0.toByte
    writeOffset += 1
  }

  def readBool: Boolean = {
    val byteValue: Byte = buffer(readOffset)
    readOffset += 1
    byteValue == 1
  }

  def writeByte(value: Byte): Unit = {
    ensureCapacity(1)
    buffer(writeOffset) = value
    writeOffset += 1
  }

  def readByte: Byte = buffer({
    readOffset += 1;
    readOffset - 1
  })

  def getCapacity: Int = buffer.length - writeOffset

  def ensureCapacity(capacity: Int): Unit = {
    while (capacity - getCapacity > 0) {
      val newSize: Int = buffer.length * 2
      if (newSize > MAX_SIZE) throw new RuntimeException("Bytebuf max size is [655537], out of memory error")
      val newBytes: Array[Byte] = new Array[Byte](newSize)
      System.arraycopy(buffer, 0, newBytes, 0, buffer.length)
      this.buffer = newBytes
    }
  }

  def writeBytes(bytes: Array[Byte]): Unit = {
    writeBytes(bytes, bytes.length)
  }

  def writeBytes(bytes: Array[Byte], length: Int): Unit = {
    ensureCapacity(length)
    System.arraycopy(bytes, 0, buffer, writeOffset, length)
    writeOffset += length
  }

  def readBytes(count: Int): Array[Byte] = {
    val bytes: Array[Byte] = new Array[Byte](count)
    System.arraycopy(buffer, readOffset, bytes, 0, count)
    readOffset += count
    bytes
  }

  def writeShort(value: Short): Unit = {
    ensureCapacity(2)
    buffer(writeOffset) = (value >>> 8).toByte
    writeOffset = writeOffset + 1
    buffer(writeOffset) = value.toByte
    writeOffset = writeOffset + 1
  }

  def readShort: Short = (buffer({
    readOffset += 1;
    readOffset - 1
  }) << 8 | buffer({
    readOffset += 1;
    readOffset - 1
  }) & 255).toShort

  // *******************************************int***************************************************
  def writeInt(value: Int): Int = writeVarInt((value << 1) ^ (value >> 31))

  def writeVarInt(value: Int): Int = {
    var a: Int = value >>> 7
    if (a == 0) {
      writeByte(value.toByte)
      return 1
    }
    ensureCapacity(5)
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value | 0x80).toByte
    var b: Int = value >>> 14
    if (b == 0) {
      buffer({
        writeOffset += 1;
        writeOffset - 1
      }) = a.toByte
      return 2
    }
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (a | 0x80).toByte
    a = value >>> 21
    if (a == 0) {
      buffer({
        writeOffset += 1;
        writeOffset - 1
      }) = b.toByte
      return 3
    }
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (b | 0x80).toByte
    b = value >>> 28
    if (b == 0) {
      buffer({
        writeOffset += 1;
        writeOffset - 1
      }) = a.toByte
      return 4
    }
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (a | 0x80).toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = b.toByte
    5
  }

  def readInt: Int = {
    var b: Int = readByte
    var value: Int = b
    if (b < 0) {
      b = readByte
      value = value & 0x0000007F | b << 7
      if (b < 0) {
        b = readByte
        value = value & 0x00003FFF | b << 14
        if (b < 0) {
          b = readByte
          value = value & 0x001FFFFF | b << 21
          if (b < 0) value = value & 0x0FFFFFFF | readByte << 28
        }
      }
    }
    (value >>> 1) ^ -(value & 1)
  }

  def writeIntCount(value: Int): Int = {
    val v = (value << 1) ^ (value >> 31)
    if (v >>> 7 == 0) return 1
    if (v >>> 14 == 0) return 2
    if (v >>> 21 == 0) return 3
    if (v >>> 28 == 0) return 4
    5
  }

  // 写入没有压缩的int
  def writeRawInt(value: Int): Unit = {
    ensureCapacity(4)
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 24).toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 16).toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 8).toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = value.toByte
  }

  // 读取没有压缩的int
  def readRawInt: Int = (buffer({
    readOffset += 1;
    readOffset - 1
  }) & 255) << 24 | (buffer({
    readOffset += 1;
    readOffset - 1
  }) & 255) << 16 | (buffer({
    readOffset += 1;
    readOffset - 1
  }) & 255) << 8 | buffer({
    readOffset += 1;
    readOffset - 1
  }) & 255

  // *******************************************long**************************************************
  def writeLong(value: Long): Unit = {
    val mask: Long = (value << 1) ^ (value >> 63)
    if (mask >>> 32 == 0) {
      writeVarInt(mask.toInt)
      return
    }
    val bytes: Array[Byte] = new Array[Byte](9)
    bytes(0) = (mask | 0x80).toByte
    bytes(1) = (mask >>> 7 | 0x80).toByte
    bytes(2) = (mask >>> 14 | 0x80).toByte
    bytes(3) = (mask >>> 21 | 0x80).toByte
    var a: Int = (mask >>> 28).toInt
    var b: Int = (mask >>> 35).toInt
    if (b == 0) {
      bytes(4) = a.toByte
      writeBytes(bytes, 5)
      return
    }
    bytes(4) = (a | 0x80).toByte
    a = (mask >>> 42).toInt
    if (a == 0) {
      bytes(5) = b.toByte
      writeBytes(bytes, 6)
      return
    }
    bytes(5) = (b | 0x80).toByte
    b = (mask >>> 49).toInt
    if (b == 0) {
      bytes(6) = a.toByte
      writeBytes(bytes, 7)
      return
    }
    bytes(6) = (a | 0x80).toByte
    a = (mask >>> 56).toInt
    if (a == 0) {
      bytes(7) = b.toByte
      writeBytes(bytes, 8)
      return
    }
    bytes(7) = (b | 0x80).toByte
    bytes(8) = a.toByte
    writeBytes(bytes, 9)
  }

  def readLong: Long = {
    var b: Long = readByte
    var value: Long = b
    if (b < 0) {
      b = readByte
      value = value & 0x00000000_0000007FL | b << 7
      if (b < 0) {
        b = readByte
        value = value & 0x00000000_00003FFFL | b << 14
        if (b < 0) {
          b = readByte
          value = value & 0x00000000_001FFFFFL | b << 21
          if (b < 0) {
            b = readByte
            value = value & 0x00000000_0FFFFFFFL | b << 28
            if (b < 0) {
              b = readByte
              value = value & 0x00000007_FFFFFFFFL | b << 35
              if (b < 0) {
                b = readByte
                value = value & 0x000003FF_FFFFFFFFL | b << 42
                if (b < 0) {
                  b = readByte
                  value = value & 0x0001FFFF_FFFFFFFFL | b << 49
                  if (b < 0) {
                    b = readByte
                    value = value & 0x00FFFFFF_FFFFFFFFL | b << 56
                  }
                }
              }
            }
          }
        }
      }
    }
    (value >>> 1) ^ -(value & 1)
  }

  def writeRawLong(value: Long): Unit = {
    ensureCapacity(8)
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 56).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 48).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 40).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 32).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 24).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 16).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = (value >>> 8).toInt.toByte
    buffer({
      writeOffset += 1;
      writeOffset - 1
    }) = value.toInt.toByte
  }

  def readRawLong: Long = (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 56 | (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 48 | (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 40 | (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 32 | (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 24 | (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 16 | (buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L) << 8 | buffer({
    readOffset += 1;
    readOffset - 1
  }).toLong & 255L

  // *******************************************float***************************************************
  def writeFloat(value: Float): Unit = {
    writeRawInt(java.lang.Float.floatToRawIntBits(value))
  }

  def readFloat: Float = java.lang.Float.intBitsToFloat(readRawInt)

  // *******************************************double***************************************************
  def writeDouble(value: Double): Unit = {
    writeRawLong(java.lang.Double.doubleToRawLongBits(value))
  }

  def readDouble: Double = java.lang.Double.longBitsToDouble(readRawLong)

  def writeString(value: String): Unit = {
    if (value == null || value.isEmpty) {
      writeInt(0)
      return
    }
    val bytes: Array[Byte] = value.getBytes(DEFAULT_CHARSET)
    writeInt(bytes.length)
    writeBytes(bytes)
  }

  def readString: String = {
    val length: Int = readInt
    if (length <= 0) return ""
    val bytes: Array[Byte] = readBytes(length)
    new String(bytes, DEFAULT_CHARSET)
  }

  def writeBoolArray(array: Array[Boolean]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeBool(array(index))
      }
    }
  }

  def readBoolArray: Array[Boolean] = {
    val size: Int = readInt
    val array: Array[Boolean] = new Array[Boolean](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readBool
    }
    array
  }

  def writeByteArray(array: Array[Byte]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeByte(array(index))
      }
    }
  }

  def readByteArray: Array[Byte] = {
    val size: Int = readInt
    val array: Array[Byte] = new Array[Byte](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readByte
    }
    array
  }

  def writeShortArray(array: Array[Short]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeShort(array(index))
      }
    }
  }

  def readShortArray: Array[Short] = {
    val size: Int = readInt
    val array: Array[Short] = new Array[Short](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readShort
    }
    array
  }

  def writeIntArray(array: Array[Int]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeInt(array(index))
      }
    }
  }

  def readIntArray: Array[Int] = {
    val size: Int = readInt
    val array: Array[Int] = new Array[Int](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readInt
    }
    array
  }

  def writeLongArray(array: Array[Long]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeLong(array(index))
      }
    }
  }

  def readLongArray: Array[Long] = {
    val size: Int = readInt
    val array: Array[Long] = new Array[Long](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readLong
    }
    array
  }

  def writeFloatArray(array: Array[Float]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeFloat(array(index))
      }
    }
  }

  def readFloatArray: Array[Float] = {
    val size: Int = readInt
    val array: Array[Float] = new Array[Float](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readFloat
    }
    array
  }

  def writeDoubleArray(array: Array[Double]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeDouble(array(index))
      }
    }
  }

  def readDoubleArray: Array[Double] = {
    val size: Int = readInt
    val array: Array[Double] = new Array[Double](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readDouble
    }
    array
  }

  def writeStringArray(array: Array[String]): Unit = {
    if ((array == null) || (array.length == 0)) writeInt(0)
    else {
      writeInt(array.length)
      val length: Int = array.length
      for (index <- 0 until length) {
        writeString(array(index))
      }
    }
  }

  def readStringArray: Array[String] = {
    val size: Int = readInt
    val array: Array[String] = new Array[String](size)
    if (size > 0) for (index <- 0 until size) {
      array(index) = readString
    }
    array
  }

  def writeBoolList(list: List[Boolean]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeBool(ele)
      }
    }
  }

  def readBoolList: List[Boolean] = {
    val size: Int = readInt
    var list: List[Boolean] = List()
    if (size > 0) {
      for (index <- 0 until size) {
        list = list :+ readBool
      }
    }
    list
  }

  def writeByteList(list: List[Byte]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeByte(ele)
      }
    }
  }

  def readByteList: List[Byte] = {
    val size: Int = readInt
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    if (size > 0) {
      for (index <- 0 until size) {
        list.addOne(readByte)
      }
    }
    list.toList
  }

  def writeShortList(list: List[Short]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeShort(ele)
      }
    }
  }

  def readShortList: List[Short] = {
    val size: Int = readInt
    val list: ListBuffer[Short] = new ListBuffer[Short]
    if (size > 0) for (index <- 0 until size) {
      list.addOne(readShort)
    }
    list.toList
  }

  def writeIntList(list: List[Int]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeInt(ele)
      }
    }
  }

  def readIntList: List[Int] = {
    val size: Int = readInt
    val list: ArrayBuffer[Int] = new ArrayBuffer[Int]
    if (size > 0) for (index <- 0 until size) {
      list.addOne(readInt)
    }
    list.toList
  }

  def writeLongList(list: List[Long]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeLong(ele)
      }
    }
  }

  def readLongList: List[Long] = {
    val size: Int = readInt
    val list: ArrayBuffer[Long] = new ArrayBuffer[Long]
    if (size > 0) for (index <- 0 until size) {
      list.addOne(readLong)
    }
    list.toList
  }

  def writeFloatList(list: List[Float]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeFloat(ele)
      }
    }
  }

  def readFloatList: List[Float] = {
    val size: Int = readInt
    val list: ArrayBuffer[Float] = new ArrayBuffer[Float]
    if (size > 0) for (index <- 0 until size) {
      list.addOne(readFloat)
    }
    list.toList
  }

  def writeDoubleList(list: List[Double]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeDouble(ele)
      }
    }
  }

  def readDoubleList: List[Double] = {
    val size: Int = readInt
    val list: ArrayBuffer[Double] = new ArrayBuffer[Double]
    if (size > 0) for (index <- 0 until size) {
      list.addOne(readDouble)
    }
    list.toList
  }

  def writeStringList(list: List[String]): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      writeInt(list.size)
      for (ele <- list) {
        writeString(ele)
      }
    }
  }

  def readStringList: List[String] = {
    val size: Int = readInt
    val list: ArrayBuffer[String] = new ArrayBuffer[String]
    if (size > 0) for (index <- 0 until size) {
      list.addOne(readString)
    }
    list.toList
  }

  def writePacketList(list: List[_], protocolId: Short): Unit = {
    if ((list == null) || list.isEmpty) writeInt(0)
    else {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      writeInt(list.size)
      for (ele <- list) {
        protocolRegistration.write(this, ele)
      }
    }
  }

  def readPacketList[T](clazz: Class[T], protocolId: Short): List[T] = {
    val size: Int = readInt
    val list: ArrayBuffer[T] = new ArrayBuffer[T]
    if (size > 0) {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      for (index <- 0 until size) {
        list.addOne(protocolRegistration.read(this).asInstanceOf[T])
      }
    }
    list.toList
  }

  def writeBoolSet(set: Set[Boolean]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeBool(ele)
      }
    }
  }

  def readBoolSet: Set[Boolean] = {
    val size: Int = readInt
    val set: ArrayBuffer[Boolean] = new ArrayBuffer[Boolean]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readBool)
    }
    set.toSet
  }

  def writeShortSet(set: Set[Short]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeShort(ele)
      }
    }
  }

  def readShortSet: Set[Short] = {
    val size: Int = readInt
    val set: ArrayBuffer[Short] = new ArrayBuffer[Short]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readShort)
    }
    set.toSet
  }

  def writeIntSet(set: Set[Int]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeInt(ele)
      }
    }
  }

  def readIntSet: Set[Int] = {
    val size: Int = readInt
    val set: ArrayBuffer[Int] = new ArrayBuffer[Int]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readInt)
    }
    set.toSet
  }

  def writeLongSet(set: Set[Long]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeLong(ele)
      }
    }
  }

  def readLongSet: Set[Long] = {
    val size: Int = readInt
    val set: ArrayBuffer[Long] = new ArrayBuffer[Long]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readLong)
    }
    set.toSet
  }

  def writeFloatSet(set: Set[Float]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeFloat(ele)
      }
    }
  }

  def readFloatSet: Set[Float] = {
    val size: Int = readInt
    val set: ArrayBuffer[Float] = new ArrayBuffer[Float]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readFloat)
    }
    set.toSet
  }

  def writeDoubleSet(set: Set[Double]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeDouble(ele)
      }
    }
  }

  def readDoubleSet: Set[Double] = {
    val size: Int = readInt
    val set: ArrayBuffer[Double] = new ArrayBuffer[Double]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readDouble)
    }
    set.toSet
  }

  def writeStringSet(set: Set[String]): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      writeInt(set.size)
      for (ele <- set) {
        writeString(ele)
      }
    }
  }

  def readStringSet: Set[String] = {
    val size: Int = readInt
    val set: ArrayBuffer[String] = new ArrayBuffer[String]
    if (size > 0) for (index <- 0 until size) {
      set.addOne(readString)
    }
    set.toSet
  }

  def writePacketSet(set: Set[_], protocolId: Short): Unit = {
    if ((set == null) || set.isEmpty) writeInt(0)
    else {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      writeInt(set.size)
      for (element <- set) {
        protocolRegistration.write(this, element)
      }
    }
  }

  def readPacketSet[T](clazz: Class[T], protocolId: Short): Set[T] = {
    val size: Int = readInt
    val set: ArrayBuffer[T] = new ArrayBuffer[T]
    if (size > 0) {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      for (index <- 0 until size) {
        set.addOne(protocolRegistration.read(this).asInstanceOf[T])
      }
    }
    set.toSet
  }

  def writeIntIntMap(map: Map[Int, Int]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeInt(key)
        writeInt(value)
      }
    }
  }

  def readIntIntMap: Map[Int, Int] = {
    val size: Int = readInt
    val map = mutable.Map[Int, Int]()
    if (size > 0) for (index <- 0 until size) {
      val key: Int = readInt
      val value: Int = readInt
      map.put(key, value)
    }
    map.toMap
  }

  def writeIntLongMap(map: Map[Int, Long]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeInt(key)
        writeLong(value)
      }
    }
  }

  def readIntLongMap: Map[Int, Long] = {
    val size: Int = readInt
    val map = mutable.Map[Int, Long]()
    if (size > 0) for (index <- 0 until size) {
      val key: Int = readInt
      val value: Long = readLong
      map.put(key, value)
    }
    map.toMap
  }

  def writeIntStringMap(map: Map[Int, String]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeInt(key)
        writeString(value)
      }
    }
  }

  def readIntStringMap: Map[Int, String] = {
    val size: Int = readInt
    val map = mutable.Map[Int, String]()
    if (size > 0) for (index <- 0 until size) {
      val key: Int = readInt
      val value: String = readString
      map.put(key, value)
    }
    map.toMap
  }

  def writeIntPacketMap(map: Map[Int, _], protocolId: Short): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      writeInt(map.size)
      for ((key, value) <- map) {
        writeInt(key)
        protocolRegistration.write(this, value)
      }
    }
  }

  def readIntPacketMap[T](clazz: Class[T], protocolId: Short): Map[Int, T] = {
    val size: Int = readInt
    val map = mutable.Map[Int, T]()
    if (size > 0) {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      for (index <- 0 until size) {
        val key: Int = readInt
        val value: T = protocolRegistration.read(this).asInstanceOf[T]
        map.put(key, value)
      }
    }
    map.toMap
  }

  def writeLongIntMap(map: Map[Long, Int]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeLong(key)
        writeInt(value)
      }
    }
  }

  def readLongIntMap: Map[Long, Int] = {
    val size: Int = readInt
    val map = mutable.Map[Long, Int]()
    if (size > 0) for (index <- 0 until size) {
      val key: Long = readLong
      val value: Int = readInt
      map.put(key, value)
    }
    map.toMap
  }

  def writeLongLongMap(map: Map[Long, Long]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeLong(key)
        writeLong(value)
      }
    }
  }

  def readLongLongMap: Map[Long, Long] = {
    val size: Int = readInt
    val map = mutable.Map[Long, Long]()
    if (size > 0) for (index <- 0 until size) {
      val key: Long = readLong
      val value: Long = readLong
      map.put(key, value)
    }
    map.toMap
  }

  def writeLongStringMap(map: Map[Long, String]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeLong(key)
        writeString(value)
      }
    }
  }

  def readLongStringMap: Map[Long, String] = {
    val size: Int = readInt
    val map = mutable.Map[Long, String]()
    if (size > 0) for (index <- 0 until size) {
      val key: Long = readLong
      val value: String = readString
      map.put(key, value)
    }
    map.toMap
  }

  def writeLongPacketMap(map: Map[Long, _], protocolId: Short): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      writeInt(map.size)
      for ((key, value) <- map) {
        writeLong(key)
        protocolRegistration.write(this, value)
      }
    }
  }

  def readLongPacketMap[T](clazz: Class[T], protocolId: Short): Map[Long, T] = {
    val size: Int = readInt
    val map = mutable.Map[Long, T]()
    if (size > 0) {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      for (index <- 0 until size) {
        val key: Long = readLong
        val value: T = protocolRegistration.read(this).asInstanceOf[T]
        map.put(key, value)
      }
    }
    map.toMap
  }

  def writeStringIntMap(map: Map[String, Int]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeString(key)
        writeInt(value)
      }
    }
  }

  def readStringIntMap: Map[String, Int] = {
    val size: Int = readInt
    val map = mutable.Map[String, Int]()
    if (size > 0) for (index <- 0 until size) {
      val key: String = readString
      val value: Int = readInt
      map.put(key, value)
    }
    map.toMap
  }

  def writeStringLongMap(map: Map[String, Long]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeString(key)
        writeLong(value)
      }
    }
  }

  def readStringLongMap: Map[String, Long] = {
    val size: Int = readInt
    val map = mutable.Map[String, Long]()
    if (size > 0) for (index <- 0 until size) {
      val key: String = readString
      val value: Long = readLong
      map.put(key, value)
    }
    map.toMap
  }

  def writeStringStringMap(map: Map[String, String]): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      writeInt(map.size)
      for ((key, value) <- map) {
        writeString(key)
        writeString(value)
      }
    }
  }

  def readStringStringMap: Map[String, String] = {
    val size: Int = readInt
    val map = mutable.Map[String, String]()
    if (size > 0) for (index <- 0 until size) {
      val key: String = readString
      val value: String = readString
      map.put(key, value)
    }
    map.toMap
  }

  def writeStringPacketMap(map: Map[String, _], protocolId: Short): Unit = {
    if ((map == null) || map.isEmpty) writeInt(0)
    else {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      writeInt(map.size)
      for ((key, value) <- map) {
        writeString(key)
        protocolRegistration.write(this, value)
      }
    }
  }

  def readStringPacketMap[T](clazz: Class[T], protocolId: Short): Map[String, T] = {
    val size: Int = readInt
    val map = mutable.Map[String, T]()
    if (size > 0) {
      val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
      for (index <- 0 until size) {
        val key: String = readString
        val value: T = protocolRegistration.read(this).asInstanceOf[T]
        map.put(key, value)
      }
    }
    map.toMap
  }

  def writePacket(packet: Any, protocolId: Short): Unit = {
    val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
    protocolRegistration.write(this, packet)
  }

  def readPacket(protocolId: Short): Any = {
    val protocolRegistration: IProtocolRegistration = ProtocolManager.getProtocol(protocolId)
    protocolRegistration.read(this)
  }
}