import ObjectA from './packet/ObjectA.js';
import ObjectB from './packet/ObjectB.js';
import ComplexObject from './packet/ComplexObject.js';
import NormalObject from './packet/NormalObject.js';
import SimpleObject from './packet/SimpleObject.js';


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

ProtocolManager.initProtocol = function initProtocol() {
    protocols.set(1116, ObjectA);
    protocols.set(1117, ObjectB);
    protocols.set(1160, ComplexObject);
    protocols.set(1161, NormalObject);
    protocols.set(1163, SimpleObject);
};

export default ProtocolManager;
