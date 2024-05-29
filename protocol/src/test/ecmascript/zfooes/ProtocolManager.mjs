import EmptyObject from './packet/EmptyObject.mjs';
import VeryBigObject from './packet/VeryBigObject.mjs';
import ComplexObject from './packet/ComplexObject.mjs';
import NormalObject from './packet/NormalObject.mjs';
import ObjectA from './packet/ObjectA.mjs';
import ObjectB from './packet/ObjectB.mjs';
import SimpleObject from './packet/SimpleObject.mjs';

const protocols = new Map();

// initProtocol
protocols.set(0, EmptyObject);
protocols.set(1, VeryBigObject);
protocols.set(100, ComplexObject);
protocols.set(101, NormalObject);
protocols.set(102, ObjectA);
protocols.set(103, ObjectB);
protocols.set(104, SimpleObject);

class ProtocolManager {
    static getProtocol(protocolId) {
        const protocol = protocols.get(protocolId);
        if (protocol === null) {
            throw new Error('[protocolId:' + protocolId + '] not exist');
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