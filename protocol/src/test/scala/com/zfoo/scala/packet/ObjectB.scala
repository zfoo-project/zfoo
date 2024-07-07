package com.zfoo.scala.packet
import com.zfoo.scala.IProtocolRegistration
import com.zfoo.scala.ByteBuffer
import scala.collection.mutable


class ObjectB {
  var flag: Boolean = false
  var innerCompatibleValue: Int = 0
}

object ObjectBRegistration extends IProtocolRegistration {
  override def protocolId: Short = 103

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[ObjectB]
    val beforeWriteIndex = buffer.getWriteOffset
    buffer.writeInt(4)
    buffer.writeBool(message.flag)
    buffer.writeInt(message.innerCompatibleValue)
    buffer.adjustPadding(4, beforeWriteIndex)
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: ObjectB = new ObjectB
    val result0 = buffer.readBool
    packet.flag = result0
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        val result1 = buffer.readInt
        packet.innerCompatibleValue = result1
    }
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}