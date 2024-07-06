package com.zfoo.kotlin.packet
import com.zfoo.kotlin.packet.ObjectA
import com.zfoo.kotlin.packet.ObjectB
import com.zfoo.kotlin.IProtocolRegistration
import com.zfoo.kotlin.ByteBuffer
// 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject {
    var a: Byte = 0
    var aaa: Array<Byte> = emptyArray()
    var b: Short = 0
    // 整数类型
    var c: Int = 0
    var d: Long = 0
    var e: Float = 0f
    var f: Double = 0.0
    var g: Boolean = false
    var jj: String = ""
    var kk: ObjectA? = null
    var l: List<Int> = emptyList()
    var ll: List<Long> = emptyList()
    var lll: List<ObjectA> = emptyList()
    var llll: List<String> = emptyList()
    var m: Map<Int, String> = emptyMap()
    var mm: Map<Int, ObjectA> = emptyMap()
    var s: Set<Int> = emptySet()
    var ssss: Set<String> = emptySet()
    var outCompatibleValue: Int = 0
    var outCompatibleValue2: Int = 0

    companion object {
        @JvmField
        val registrationNormalObject: IProtocolRegistration = object : IProtocolRegistration {
            override fun protocolId(): Short {
                return 101
            }
    
            override fun write(buffer: ByteBuffer, packet: Any?) {
                if (packet == null) {
                    buffer.writeInt(0)
                    return
                }
                val message = packet as NormalObject
                val beforeWriteIndex = buffer.writeOffset()
                buffer.writeInt(857)
                buffer.writeByte(message.a)
                buffer.writeByteArray(message.aaa)
                buffer.writeShort(message.b)
                buffer.writeInt(message.c)
                buffer.writeLong(message.d)
                buffer.writeFloat(message.e)
                buffer.writeDouble(message.f)
                buffer.writeBool(message.g)
                buffer.writeString(message.jj)
                buffer.writePacket(message.kk, 102)
                buffer.writeIntList(message.l)
                buffer.writeLongList(message.ll)
                buffer.writePacketList(message.lll, 102)
                buffer.writeStringList(message.llll)
                buffer.writeIntStringMap(message.m)
                buffer.writeIntPacketMap(message.mm, 102)
                buffer.writeIntSet(message.s)
                buffer.writeStringSet(message.ssss)
                buffer.writeInt(message.outCompatibleValue)
                buffer.writeInt(message.outCompatibleValue2)
                buffer.adjustPadding(857, beforeWriteIndex)
            }
    
            override fun read(buffer: ByteBuffer): Any {
                val length = buffer.readInt()
                val packet = NormalObject()
                if (length == 0) {
                    return packet
                }
                val beforeReadIndex = buffer.readOffset()
                val result0 = buffer.readByte()
                packet.a = result0
                val array1 = buffer.readByteArray()
                packet.aaa = array1
                val result2 = buffer.readShort()
                packet.b = result2
                val result3 = buffer.readInt()
                packet.c = result3
                val result4 = buffer.readLong()
                packet.d = result4
                val result5 = buffer.readFloat()
                packet.e = result5
                val result6 = buffer.readDouble()
                packet.f = result6
                val result7 = buffer.readBool()
                packet.g = result7
                val result8 = buffer.readString()
                packet.jj = result8
                val result9 = buffer.readPacket(102) as ObjectA
                packet.kk = result9
                val list10 = buffer.readIntList()
                packet.l = list10
                val list11 = buffer.readLongList()
                packet.ll = list11
                val list12 = buffer.readPacketList(ObjectA::class.java, 102)
                packet.lll = list12
                val list13 = buffer.readStringList()
                packet.llll = list13
                val map14 = buffer.readIntStringMap()
                packet.m = map14
                val map15 = buffer.readIntPacketMap(ObjectA::class.java, 102)
                packet.mm = map15
                val set16 = buffer.readIntSet()
                packet.s = set16
                val set17 = buffer.readStringSet()
                packet.ssss = set17
                if (buffer.compatibleRead(beforeReadIndex, length)) {
                    val result18 = buffer.readInt()
                    packet.outCompatibleValue = result18
                }
                if (buffer.compatibleRead(beforeReadIndex, length)) {
                    val result19 = buffer.readInt()
                    packet.outCompatibleValue2 = result19
                }
                if (length > 0) {
                    buffer.setReadOffset(beforeReadIndex + length)
                }
                return packet
            }
        }
    }
}