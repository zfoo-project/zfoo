package com.zfoo.kotlin.packet
import com.zfoo.kotlin.packet.ObjectB
import com.zfoo.kotlin.IProtocolRegistration
import com.zfoo.kotlin.ByteBuffer

class ObjectA {
    var a: Int = 0
    var m: Map<Int, String> = emptyMap()
    var objectB: ObjectB? = null
    var innerCompatibleValue: Int = 0

    companion object {
        @JvmField
        val registrationObjectA: IProtocolRegistration = object : IProtocolRegistration {
            override fun protocolId(): Short {
                return 102
            }
    
            override fun write(buffer: ByteBuffer, packet: Any?) {
                if (packet == null) {
                    buffer.writeInt(0)
                    return
                }
                val message = packet as ObjectA
                val beforeWriteIndex = buffer.writeOffset()
                buffer.writeInt(201)
                buffer.writeInt(message.a)
                buffer.writeIntStringMap(message.m)
                buffer.writePacket(message.objectB, 103)
                buffer.writeInt(message.innerCompatibleValue)
                buffer.adjustPadding(201, beforeWriteIndex)
            }
    
            override fun read(buffer: ByteBuffer): Any {
                val length = buffer.readInt()
                val packet = ObjectA()
                if (length == 0) {
                    return packet
                }
                val beforeReadIndex = buffer.readOffset()
                val result0 = buffer.readInt()
                packet.a = result0
                val map1 = buffer.readIntStringMap()
                packet.m = map1
                val result2 = buffer.readPacket(103) as ObjectB
                packet.objectB = result2
                if (buffer.compatibleRead(beforeReadIndex, length)) {
                    val result3 = buffer.readInt()
                    packet.innerCompatibleValue = result3
                }
                if (length > 0) {
                    buffer.setReadOffset(beforeReadIndex + length)
                }
                return packet
            }
        }
    }
}