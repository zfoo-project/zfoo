package com.zfoo.scala.packet
import com.zfoo.scala.packet.ObjectB
import com.zfoo.scala.IProtocolRegistration
import com.zfoo.scala.ByteBuffer
import scala.collection.mutable


class ObjectA {
  var a: Int = 0
  var m: Map[Int, String] = _
  var objectB: ObjectB = _
  var innerCompatibleValue: Int = 0
}

object ObjectARegistration extends IProtocolRegistration {
  override def protocolId: Short = 102

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[ObjectA]
    val beforeWriteIndex = buffer.getWriteOffset
    buffer.writeInt(201)
    buffer.writeInt(message.a)
    buffer.writeIntStringMap(message.m)
    buffer.writePacket(message.objectB, 103)
    buffer.writeInt(message.innerCompatibleValue)
    buffer.adjustPadding(201, beforeWriteIndex)
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: ObjectA = new ObjectA
    val result0 = buffer.readInt
    packet.a = result0
    val map1 = buffer.readIntStringMap
    packet.m = map1
    val result2 = buffer.readPacket(103).asInstanceOf[ObjectB]
    packet.objectB = result2
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        val result3 = buffer.readInt
        packet.innerCompatibleValue = result3
    }
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}