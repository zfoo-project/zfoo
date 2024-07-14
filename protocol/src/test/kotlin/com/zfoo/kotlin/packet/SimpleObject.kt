package com.zfoo.kotlin.packet
import com.zfoo.kotlin.IProtocolRegistration
import com.zfoo.kotlin.ByteBuffer


class SimpleObject {
    var c: Int = 0
    var g: Boolean = false
}

class SimpleObjectRegistration : IProtocolRegistration {
    override fun protocolId(): Short {
        return 104
    }

    override fun write(buffer: ByteBuffer, packet: Any?) {
        if (packet == null) {
            buffer.writeInt(0)
            return
        }
        val message = packet as SimpleObject
        buffer.writeInt(-1)
        buffer.writeInt(message.c)
        buffer.writeBool(message.g)
    }

    override fun read(buffer: ByteBuffer): Any {
        val length = buffer.readInt()
        val packet = SimpleObject()
        if (length == 0) {
            return packet
        }
        val beforeReadIndex = buffer.getReadOffset()
        val result0 = buffer.readInt()
        packet.c = result0
        val result1 = buffer.readBool()
        packet.g = result1
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}