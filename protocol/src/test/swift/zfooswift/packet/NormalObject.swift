import Foundation

// 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject : IProtocol {
    var a: Int8 = 0
    var aaa: Array<Int8> = []
    var b: Int16 = 0
    // 整数类型
    var c: Int = 0
    var d: Int64 = 0
    var e: Float32 = 0.0
    var f: Float64 = 0.0
    var g: Bool = false
    var jj: String = ""
    var kk: ObjectA? = nil
    var l: Array<Int> = []
    var ll: Array<Int64> = []
    var lll: Array<ObjectA> = []
    var llll: Array<String> = []
    var m: Dictionary<Int, String> = [:]
    var mm: Dictionary<Int, ObjectA> = [:]
    var s: Set<Int> = []
    var ssss: Set<String> = []
    var outCompatibleValue: Int = 0
    var outCompatibleValue2: Int = 0
    
    func protocolId() -> Int {
        return 101
    }
}

class NormalObjectRegistration : IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?) {
        if (packet == nil) {
            buffer.writeInt(0)
            return
        }
        let message = packet as! NormalObject
        let beforeWriteIndex = buffer.getWriteOffset()
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
        buffer.writeIntArray(message.l)
        buffer.writeLongArray(message.ll)
        buffer.writePacketArray(message.lll, 102)
        buffer.writeStringArray(message.llll)
        buffer.writeIntStringMap(message.m)
        buffer.writeIntPacketMap(message.mm, 102)
        buffer.writeIntSet(message.s)
        buffer.writeStringSet(message.ssss)
        buffer.writeInt(message.outCompatibleValue)
        buffer.writeInt(message.outCompatibleValue2)
        buffer.adjustPadding(857, beforeWriteIndex)
    }
    
    func read(_ buffer: ByteBuffer) -> Any {
        let length = buffer.readInt()
        let packet = NormalObject()
        if (length == 0) {
            return packet
        }
        let beforeReadIndex = buffer.getReadOffset()
        let result0 = buffer.readByte()
        packet.a = result0
        let array1 = buffer.readByteArray()
        packet.aaa = array1
        let result2 = buffer.readShort()
        packet.b = result2
        let result3 = buffer.readInt()
        packet.c = result3
        let result4 = buffer.readLong()
        packet.d = result4
        let result5 = buffer.readFloat()
        packet.e = result5
        let result6 = buffer.readDouble()
        packet.f = result6
        let result7 = buffer.readBool()
        packet.g = result7
        let result8 = buffer.readString()
        packet.jj = result8
        let result9 = buffer.readPacket(102) as! ObjectA
        packet.kk = result9
        let list10 = buffer.readIntArray()
        packet.l = list10
        let list11 = buffer.readLongArray()
        packet.ll = list11
        let list12 = buffer.readPacketArray(102) as! Array<ObjectA>
        packet.lll = list12
        let list13 = buffer.readStringArray()
        packet.llll = list13
        let map14 = buffer.readIntStringMap()
        packet.m = map14
        let map15 = buffer.readIntPacketMap(102) as! Dictionary<Int, ObjectA>
        packet.mm = map15
        let set16 = buffer.readIntSet()
        packet.s = set16
        let set17 = buffer.readStringSet()
        packet.ssss = set17
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            let result18 = buffer.readInt()
            packet.outCompatibleValue = result18
        }
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            let result19 = buffer.readInt()
            packet.outCompatibleValue2 = result19
        }
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}