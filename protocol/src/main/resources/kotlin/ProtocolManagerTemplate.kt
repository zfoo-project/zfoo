${protocol_root_path}
${protocol_imports}
class ProtocolManager {
    companion object {
        val protocols = arrayOfNulls<IProtocolRegistration>(Short.MAX_VALUE.toInt())
        var protocolIdMap: MutableMap<Class<*>, Short> = HashMap()
        @JvmStatic
        fun initProtocol() {
            // initProtocol
            ${protocol_manager_registrations}
        }

        @JvmStatic
        fun getProtocolId(clazz: Class<*>): Short {
            return protocolIdMap[clazz]!!
        }

        @JvmStatic
        fun getProtocol(protocolId: Short): IProtocolRegistration {
            return protocols[protocolId.toInt()]
                ?: throw RuntimeException("[protocolId:$protocolId] not exist")
        }

        @JvmStatic
        fun write(buffer: ByteBuffer, packet: Any) {
            val protocolId = getProtocolId(packet.javaClass)
            // write protocol id to buffer
            buffer.writeShort(protocolId)
            // write packet
            getProtocol(protocolId).write(buffer, packet)
        }

        @JvmStatic
        fun read(buffer: ByteBuffer): Any {
            val protocolId = buffer.readShort()
            return getProtocol(protocolId).read(buffer)
        }
    }
}