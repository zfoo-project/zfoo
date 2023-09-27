import EmptyObject from './packet/EmptyObject.js';
import ComplexObject from './packet/ComplexObject.js';
import NormalObject from './packet/NormalObject.js';
import ObjectA from './packet/ObjectA.js';
import ObjectB from './packet/ObjectB.js';
import SimpleObject from './packet/SimpleObject.js';

const protocols = new Map();

const ProtocolManager = {};

// initProtocol
protocols.set(0, EmptyObject);
protocols.set(100, ComplexObject);
protocols.set(101, NormalObject);
protocols.set(102, ObjectA);
protocols.set(103, ObjectB);
protocols.set(104, SimpleObject);

ProtocolManager.getProtocol = function getProtocol(protocolId) {
    const protocol = protocols.get(protocolId);
    if (protocol === null) {
        throw new Error('[protocolId:' + protocolId + ']协议不存在');
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
