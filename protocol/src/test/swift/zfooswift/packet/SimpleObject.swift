import Foundation


class SimpleObject : IProtocol {
    var c: Int = 0
    var g: Bool = false
    
    func protocolId() -> Int {
        return 104
    }
}

class SimpleObjectRegistration : IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?) {
        if (packet == nil) {
            buffer.writeInt(0)
            return
        }
        let message = packet as! SimpleObject
        buffer.writeInt(-1)
        buffer.writeInt(message.c)
        buffer.writeBool(message.g)
    }
    
    func read(_ buffer: ByteBuffer) -> Any {
        let length = buffer.readInt()
        let packet = SimpleObject()
        if (length == 0) {
            return packet
        }
        let beforeReadIndex = buffer.getReadOffset()
        let result0 = buffer.readInt()
        packet.c = result0
        let result1 = buffer.readBool()
        packet.g = result1
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}