import Foundation


class ObjectA : IProtocol {
    var a: Int = 0
    var m: Dictionary<Int, String> = [:]
    var objectB: ObjectB? = nil
    var innerCompatibleValue: Int = 0
    
    func protocolId() -> Int {
        return 102
    }
}

class ObjectARegistration : IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?) {
        if (packet == nil) {
            buffer.writeInt(0)
            return
        }
        let message = packet as! ObjectA
        let beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(201)
        buffer.writeInt(message.a)
        buffer.writeIntStringMap(message.m)
        buffer.writePacket(message.objectB, 103)
        buffer.writeInt(message.innerCompatibleValue)
        buffer.adjustPadding(201, beforeWriteIndex)
    }
    
    func read(_ buffer: ByteBuffer) -> Any {
        let length = buffer.readInt()
        let packet = ObjectA()
        if (length == 0) {
            return packet
        }
        let beforeReadIndex = buffer.getReadOffset()
        let result0 = buffer.readInt()
        packet.a = result0
        let map1 = buffer.readIntStringMap()
        packet.m = map1
        let result2 = buffer.readPacket(103) as! ObjectB
        packet.objectB = result2
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            let result3 = buffer.readInt()
            packet.innerCompatibleValue = result3
        }
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}