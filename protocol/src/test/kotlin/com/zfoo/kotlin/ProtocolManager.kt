package com.zfoo.kotlin
import com.zfoo.kotlin.packet.EmptyObject
import com.zfoo.kotlin.packet.ComplexObject
import com.zfoo.kotlin.packet.NormalObject
import com.zfoo.kotlin.packet.ObjectA
import com.zfoo.kotlin.packet.ObjectB
import com.zfoo.kotlin.packet.SimpleObject
class ProtocolManager {
    companion object {
        val protocols = arrayOfNulls<IProtocolRegistration>(Short.MAX_VALUE.toInt())
        var protocolIdMap: MutableMap<Class<*>, Short> = HashMap()
        @JvmStatic
        fun initProtocol() {
            // initProtocol
            protocols[0] = EmptyObject.registrationEmptyObject
            protocolIdMap[EmptyObject::class.java] = 0.toShort()
            protocols[100] = ComplexObject.registrationComplexObject
            protocolIdMap[ComplexObject::class.java] = 100.toShort()
            protocols[101] = NormalObject.registrationNormalObject
            protocolIdMap[NormalObject::class.java] = 101.toShort()
            protocols[102] = ObjectA.registrationObjectA
            protocolIdMap[ObjectA::class.java] = 102.toShort()
            protocols[103] = ObjectB.registrationObjectB
            protocolIdMap[ObjectB::class.java] = 103.toShort()
            protocols[104] = SimpleObject.registrationSimpleObject
            protocolIdMap[SimpleObject::class.java] = 104.toShort()
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