package com.zfoo.scala.packet
import com.zfoo.scala.packet.ObjectA
import com.zfoo.scala.packet.ObjectB
import com.zfoo.scala.IProtocolRegistration
import com.zfoo.scala.ByteBuffer
import scala.collection.mutable

// 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject {
  var a: Byte = 0
  var aaa: Array[Byte] = _
  var b: Short = 0
  // 整数类型
  var c: Int = 0
  var d: Long = 0L
  var e: Float = 0f
  var f: Double = 0D
  var g: Boolean = false
  var jj: String = _
  var kk: ObjectA = _
  var l: List[Int] = _
  var ll: List[Long] = _
  var lll: List[ObjectA] = _
  var llll: List[String] = _
  var m: Map[Int, String] = _
  var mm: Map[Int, ObjectA] = _
  var s: Set[Int] = _
  var ssss: Set[String] = _
  var outCompatibleValue: Int = 0
  var outCompatibleValue2: Int = 0
}

object RegistrationNormalObject extends IProtocolRegistration {
  override def protocolId: Short = 101

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[NormalObject]
    val beforeWriteIndex = buffer.getWriteOffset
    buffer.writeInt(857)
    buffer.writeByte(message.a)
    buffer.writeByteArray(message.aaa)
    buffer.writeShort(message.b)
    buffer.writeInt(message.c)
    buffer.writeLong(message.d)
    buffer.writeFloat(message.e)
    buffer.writeDouble(message.f)
    buffer.writeBool(message.g)
    buffer.writeString(message.jj)
    buffer.writePacket(message.kk, 102)
    buffer.writeIntList(message.l)
    buffer.writeLongList(message.ll)
    buffer.writePacketList(message.lll, 102)
    buffer.writeStringList(message.llll)
    buffer.writeIntStringMap(message.m)
    buffer.writeIntPacketMap(message.mm, 102)
    buffer.writeIntSet(message.s)
    buffer.writeStringSet(message.ssss)
    buffer.writeInt(message.outCompatibleValue)
    buffer.writeInt(message.outCompatibleValue2)
    buffer.adjustPadding(857, beforeWriteIndex)
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: NormalObject = new NormalObject
    val result0 = buffer.readByte
    packet.a = result0
    val array1 = buffer.readByteArray
    packet.aaa = array1
    val result2 = buffer.readShort
    packet.b = result2
    val result3 = buffer.readInt
    packet.c = result3
    val result4 = buffer.readLong
    packet.d = result4
    val result5 = buffer.readFloat
    packet.e = result5
    val result6 = buffer.readDouble
    packet.f = result6
    val result7 = buffer.readBool
    packet.g = result7
    val result8 = buffer.readString
    packet.jj = result8
    val result9 = buffer.readPacket(102).asInstanceOf[ObjectA]
    packet.kk = result9
    val list10 = buffer.readIntList
    packet.l = list10
    val list11 = buffer.readLongList
    packet.ll = list11
    val list12 = buffer.readPacketList(classOf[ObjectA], 102)
    packet.lll = list12
    val list13 = buffer.readStringList
    packet.llll = list13
    val map14 = buffer.readIntStringMap
    packet.m = map14
    val map15 = buffer.readIntPacketMap(classOf[ObjectA], 102)
    packet.mm = map15
    val set16 = buffer.readIntSet
    packet.s = set16
    val set17 = buffer.readStringSet
    packet.ssss = set17
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        val result18 = buffer.readInt
        packet.outCompatibleValue = result18
    }
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        val result19 = buffer.readInt
        packet.outCompatibleValue2 = result19
    }
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}