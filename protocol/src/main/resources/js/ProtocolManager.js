const protocols = new Map();

const ProtocolManager = {};

ProtocolManager.getProtocol = function getProtocol(protocolId) {
    const protocol = protocols.get(protocolId);
    if (protocol === null) {
        throw new Error('[protocolId:' + protocolId + ']协议不存在');
    }
    return protocol;
};

ProtocolManager.write = function write(byteBuffer, packet) {
    const protocolId = packet.protocolId();
    byteBuffer.writeShort(protocolId);
    const protocol = ProtocolManager.getProtocol(protocolId);
    protocol.writeObject(byteBuffer, packet);
};

ProtocolManager.read = function read(byteBuffer) {
    const protocolId = byteBuffer.readShort();
    const protocol = ProtocolManager.getProtocol(protocolId);
    const packet = protocol.readObject(byteBuffer);
    return packet;
};

