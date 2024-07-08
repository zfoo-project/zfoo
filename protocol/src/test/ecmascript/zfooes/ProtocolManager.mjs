import EmptyObject from './packet/EmptyObject.mjs';
import { EmptyObjectRegistration } from './packet/EmptyObject.mjs';
import VeryBigObject from './packet/VeryBigObject.mjs';
import { VeryBigObjectRegistration } from './packet/VeryBigObject.mjs';
import ComplexObject from './packet/ComplexObject.mjs';
import { ComplexObjectRegistration } from './packet/ComplexObject.mjs';
import NormalObject from './packet/NormalObject.mjs';
import { NormalObjectRegistration } from './packet/NormalObject.mjs';
import ObjectA from './packet/ObjectA.mjs';
import { ObjectARegistration } from './packet/ObjectA.mjs';
import ObjectB from './packet/ObjectB.mjs';
import { ObjectBRegistration } from './packet/ObjectB.mjs';
import SimpleObject from './packet/SimpleObject.mjs';
import { SimpleObjectRegistration } from './packet/SimpleObject.mjs';

const protocols = new Map();
const protocolIdMap = new Map();

// initProtocol
protocols.set(0, new EmptyObjectRegistration());
protocolIdMap.set(EmptyObject, 0);
protocols.set(1, new VeryBigObjectRegistration());
protocolIdMap.set(VeryBigObject, 1);
protocols.set(100, new ComplexObjectRegistration());
protocolIdMap.set(ComplexObject, 100);
protocols.set(101, new NormalObjectRegistration());
protocolIdMap.set(NormalObject, 101);
protocols.set(102, new ObjectARegistration());
protocolIdMap.set(ObjectA, 102);
protocols.set(103, new ObjectBRegistration());
protocolIdMap.set(ObjectB, 103);
protocols.set(104, new SimpleObjectRegistration());
protocolIdMap.set(SimpleObject, 104);

class ProtocolManager {
    static getProtocolId(clazz) {
        const protocolId = protocolIdMap.get(clazz);
        if (protocolId === null || protocolId === undefined) {
            throw '[protocol:' + clazz + '] not exist';
        }
        return protocolId;
    }
    static getProtocol(protocolId) {
        const protocol = protocols.get(protocolId);
        if (protocol === null) {
            throw new Error('[protocolId:' + protocolId + '] not exist');
        }
        return protocol;
    }

    static write(buffer, packet) {
        const protocolId = ProtocolManager.getProtocolId(packet.constructor);
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