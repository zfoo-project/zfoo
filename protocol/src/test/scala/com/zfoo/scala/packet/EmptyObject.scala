package com.zfoo.scala.packet
import com.zfoo.scala.IProtocolRegistration
import com.zfoo.scala.ByteBuffer
import scala.collection.mutable


class EmptyObject {
  
}

object RegistrationEmptyObject extends IProtocolRegistration {
  override def protocolId: Short = 0

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[EmptyObject]
    buffer.writeInt(-1)
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: EmptyObject = new EmptyObject
    
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}