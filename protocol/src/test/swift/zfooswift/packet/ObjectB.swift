import Foundation


class ObjectB : IProtocol {
    var flag: Bool = false
    var innerCompatibleValue: Int = 0
    
    func protocolId() -> Int {
        return 103
    }
}

class ObjectBRegistration : IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?) {
        if (packet == nil) {
            buffer.writeInt(0)
            return
        }
        let message = packet as! ObjectB
        let beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(4)
        buffer.writeBool(message.flag)
        buffer.writeInt(message.innerCompatibleValue)
        buffer.adjustPadding(4, beforeWriteIndex)
    }
    
    func read(_ buffer: ByteBuffer) -> Any {
        let length = buffer.readInt()
        let packet = ObjectB()
        if (length == 0) {
            return packet
        }
        let beforeReadIndex = buffer.getReadOffset()
        let result0 = buffer.readBool()
        packet.flag = result0
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            let result1 = buffer.readInt()
            packet.innerCompatibleValue = result1
        }
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}