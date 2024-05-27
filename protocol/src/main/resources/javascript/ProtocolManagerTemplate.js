${protocol_imports}

const protocols = new Map();

const ProtocolManager = {};

// initProtocol
${protocol_manager_registrations}

ProtocolManager.getProtocol = function getProtocol(protocolId) {
    const protocol = protocols.get(protocolId);
    if (protocol === null) {
        throw new Error('[protocolId:' + protocolId + '] not exist');
    }
    return protocol;
};

ProtocolManager.write = function write(buffer, packet) {
    const protocolId = packet.protocolId();
    buffer.writeShort(protocolId);
    const protocol = ProtocolManager.getProtocol(protocolId);
    protocol.write(buffer, packet);
};

ProtocolManager.read = function read(buffer) {
    const protocolId = buffer.readShort();
    const protocol = ProtocolManager.getProtocol(protocolId);
    const packet = protocol.read(buffer);
    return packet;
};

export default ProtocolManager;
