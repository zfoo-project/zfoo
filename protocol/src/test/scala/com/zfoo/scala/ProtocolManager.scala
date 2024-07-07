package com.zfoo.scala
import com.zfoo.scala.packet.EmptyObject
import com.zfoo.scala.packet.EmptyObjectRegistration
import com.zfoo.scala.packet.VeryBigObject
import com.zfoo.scala.packet.VeryBigObjectRegistration
import com.zfoo.scala.packet.ComplexObject
import com.zfoo.scala.packet.ComplexObjectRegistration
import com.zfoo.scala.packet.NormalObject
import com.zfoo.scala.packet.NormalObjectRegistration
import com.zfoo.scala.packet.ObjectA
import com.zfoo.scala.packet.ObjectARegistration
import com.zfoo.scala.packet.ObjectB
import com.zfoo.scala.packet.ObjectBRegistration
import com.zfoo.scala.packet.SimpleObject
import com.zfoo.scala.packet.SimpleObjectRegistration
import scala.collection.mutable

object ProtocolManager {
  val MAX_PROTOCOL_NUM: Short = Short.MaxValue
  val protocols = new Array[IProtocolRegistration](MAX_PROTOCOL_NUM)
  val protocolIdMap = mutable.Map[Class[_], Short]()

  def initProtocol(): Unit = {
    // initProtocol
    protocols(0) = EmptyObjectRegistration
    protocolIdMap.put(classOf[EmptyObject], 0)
    protocols(1) = VeryBigObjectRegistration
    protocolIdMap.put(classOf[VeryBigObject], 1)
    protocols(100) = ComplexObjectRegistration
    protocolIdMap.put(classOf[ComplexObject], 100)
    protocols(101) = NormalObjectRegistration
    protocolIdMap.put(classOf[NormalObject], 101)
    protocols(102) = ObjectARegistration
    protocolIdMap.put(classOf[ObjectA], 102)
    protocols(103) = ObjectBRegistration
    protocolIdMap.put(classOf[ObjectB], 103)
    protocols(104) = SimpleObjectRegistration
    protocolIdMap.put(classOf[SimpleObject], 104)
  }

  def getProtocolId(clazz: Class[_]): Short = protocolIdMap.getOrElse(clazz, -1)

  def getProtocol(protocolId: Short): IProtocolRegistration = {
    val protocol = protocols(protocolId)
    if (protocol == null) throw new RuntimeException("[protocolId:" + protocolId + "] not exist")
    protocol
  }

  def write(buffer: ByteBuffer, packet: Any): Unit = {
    val protocolId = getProtocolId(packet.getClass)
    // write protocol id to buffer
    buffer.writeShort(protocolId)
    // write packet
    getProtocol(protocolId).write(buffer, packet)
  }

  def read(buffer: ByteBuffer): Any = {
    val protocolId = buffer.readShort
    getProtocol(protocolId).read(buffer)
  }
}