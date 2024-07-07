${protocol_root_path}
${protocol_imports}
import scala.collection.mutable

object ProtocolManager {
  val MAX_PROTOCOL_NUM: Short = Short.MaxValue
  val protocols = new Array[IProtocolRegistration](MAX_PROTOCOL_NUM)
  val protocolIdMap = mutable.Map[Class[_], Short]()

  def initProtocol(): Unit = {
    // initProtocol
    ${protocol_manager_registrations}
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