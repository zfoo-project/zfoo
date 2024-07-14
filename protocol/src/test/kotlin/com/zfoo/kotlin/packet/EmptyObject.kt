package com.zfoo.kotlin.packet
import com.zfoo.kotlin.IProtocolRegistration
import com.zfoo.kotlin.ByteBuffer


class EmptyObject {
    
}

class EmptyObjectRegistration : IProtocolRegistration {
    override fun protocolId(): Short {
        return 0
    }

    override fun write(buffer: ByteBuffer, packet: Any?) {
        if (packet == null) {
            buffer.writeInt(0)
            return
        }
        val message = packet as EmptyObject
        buffer.writeInt(-1)
    }

    override fun read(buffer: ByteBuffer): Any {
        val length = buffer.readInt()
        val packet = EmptyObject()
        if (length == 0) {
            return packet
        }
        val beforeReadIndex = buffer.getReadOffset()
        
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}