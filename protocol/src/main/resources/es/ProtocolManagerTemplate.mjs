{}

const protocols = new Map();

// initProtocol
{}

class ProtocolManager {
    static getProtocol(protocolId) {
        const protocol = protocols.get(protocolId);
        if (protocol === null) {
            throw new Error('[protocolId:' + protocolId + ']协议不存在');
        }
        return protocol;
    }

    static write(buffer, packet) {
        const protocolId = packet.protocolId();
        buffer.writeShort(protocolId);
        const protocol = ProtocolManager.getProtocol(protocolId);
        protocol.write(buffer, packet);
    }

    static read(buffer) {
        const protocolId = buffer.readShort();
        const protocol = ProtocolManager.getProtocol(protocolId);
        const packet = protocol.read(buffer);
        return packet;
    }
}


export default ProtocolManager;
