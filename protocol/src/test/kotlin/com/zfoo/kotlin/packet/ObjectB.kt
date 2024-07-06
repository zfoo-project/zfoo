package com.zfoo.kotlin.packet
import com.zfoo.kotlin.IProtocolRegistration
import com.zfoo.kotlin.ByteBuffer

class ObjectB {
    var flag: Boolean = false
    var innerCompatibleValue: Int = 0

    companion object {
        @JvmField
        val registrationObjectB: IProtocolRegistration = object : IProtocolRegistration {
            override fun protocolId(): Short {
                return 103
            }
    
            override fun write(buffer: ByteBuffer, packet: Any?) {
                if (packet == null) {
                    buffer.writeInt(0)
                    return
                }
                val message = packet as ObjectB
                val beforeWriteIndex = buffer.writeOffset()
                buffer.writeInt(4)
                buffer.writeBool(message.flag)
                buffer.writeInt(message.innerCompatibleValue)
                buffer.adjustPadding(4, beforeWriteIndex)
            }
    
            override fun read(buffer: ByteBuffer): Any {
                val length = buffer.readInt()
                val packet = ObjectB()
                if (length == 0) {
                    return packet
                }
                val beforeReadIndex = buffer.readOffset()
                val result0 = buffer.readBool()
                packet.flag = result0
                if (buffer.compatibleRead(beforeReadIndex, length)) {
                    val result1 = buffer.readInt()
                    packet.innerCompatibleValue = result1
                }
                if (length > 0) {
                    buffer.setReadOffset(beforeReadIndex + length)
                }
                return packet
            }
        }
    }
}