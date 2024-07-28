import Foundation

class ProtocolManager {
    static var protocols = Dictionary<Int, IProtocolRegistration>()

    static func initProtocol() {
        // initProtocol
        protocols[0] = EmptyObjectRegistration()
        protocols[101] = NormalObjectRegistration()
        protocols[102] = ObjectARegistration()
        protocols[103] = ObjectBRegistration()
        protocols[104] = SimpleObjectRegistration()
    }
    
    static func getProtocol(_ protocolId: Int) -> IProtocolRegistration {
        return protocols[protocolId]!
    }
    
    static func write(_ buffer: ByteBuffer, _ packet: Any) {
        let p = packet as! IProtocol
        let protocolId = p.protocolId()
        let pro = getProtocol(protocolId)
        buffer.writeShort(Int16(protocolId))
        pro.write(buffer, p)
    }
    
    static func read(_ buffer: ByteBuffer) -> Any {
        let protocolId = buffer.readShort()
        let pro = getProtocol(Int(protocolId))
        return pro.read(buffer)
    }
}