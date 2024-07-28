import Foundation

protocol IProtocol {
    func protocolId() -> Int
}


protocol IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?)
    
    func read(_ buffer: ByteBuffer) -> Any
}
