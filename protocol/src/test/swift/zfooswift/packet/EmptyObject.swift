import Foundation


class EmptyObject : IProtocol {
    
    
    func protocolId() -> Int {
        return 0
    }
}

class EmptyObjectRegistration : IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?) {
        if (packet == nil) {
            buffer.writeInt(0)
            return
        }
        let message = packet as! EmptyObject
        buffer.writeInt(-1)
    }
    
    func read(_ buffer: ByteBuffer) -> Any {
        let length = buffer.readInt()
        let packet = EmptyObject()
        if (length == 0) {
            return packet
        }
        let beforeReadIndex = buffer.getReadOffset()
        
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}