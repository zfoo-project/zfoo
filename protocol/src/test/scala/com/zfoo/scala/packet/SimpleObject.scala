package com.zfoo.scala.packet
import com.zfoo.scala.IProtocolRegistration
import com.zfoo.scala.ByteBuffer
import scala.collection.mutable


class SimpleObject {
  var c: Int = 0
  var g: Boolean = false
}

object SimpleObjectRegistration extends IProtocolRegistration {
  override def protocolId: Short = 104

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[SimpleObject]
    buffer.writeInt(-1)
    buffer.writeInt(message.c)
    buffer.writeBool(message.g)
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: SimpleObject = new SimpleObject
    val result0 = buffer.readInt
    packet.c = result0
    val result1 = buffer.readBool
    packet.g = result1
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}