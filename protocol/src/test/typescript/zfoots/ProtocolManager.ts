import EmptyObject from './packet/EmptyObject';
import { EmptyObjectRegistration } from './packet/EmptyObject';
import VeryBigObject from './packet/VeryBigObject';
import { VeryBigObjectRegistration } from './packet/VeryBigObject';
import ComplexObject from './packet/ComplexObject';
import { ComplexObjectRegistration } from './packet/ComplexObject';
import NormalObject from './packet/NormalObject';
import { NormalObjectRegistration } from './packet/NormalObject';
import ObjectA from './packet/ObjectA';
import { ObjectARegistration } from './packet/ObjectA';
import ObjectB from './packet/ObjectB';
import { ObjectBRegistration } from './packet/ObjectB';
import SimpleObject from './packet/SimpleObject';
import { SimpleObjectRegistration } from './packet/SimpleObject';
import IByteBuffer from "./IByteBuffer";
import IProtocolRegistration from "./IProtocolRegistration";

const protocols = new Map<number, IProtocolRegistration<unknown>>();
const protocolIdMap = new Map<any, number>();

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
    static getProtocolId(clazz: any): number {
        const protocolId = protocolIdMap.get(clazz);
        if (protocolId === null || protocolId === undefined) {
            throw '[protocol:' + clazz + '] not exist';
        }
        return protocolId;
    }

    static getProtocol(protocolId: number): IProtocolRegistration<unknown> {
        const protocol = protocols.get(protocolId);
        if (protocol === null || protocol === undefined) {
            throw '[protocolId:' + protocolId + '] not exist';
        }
        return protocol;
    }

    static write(buffer: IByteBuffer, packet: any): void {
        const protocolId = ProtocolManager.getProtocolId(packet.constructor);
        buffer.writeShort(protocolId);
        const protocol = ProtocolManager.getProtocol(protocolId);
        protocol.write(buffer, packet);
    }

    static read(buffer: IByteBuffer): any {
        const protocolId = buffer.readShort();
        const protocol = ProtocolManager.getProtocol(protocolId);
        const packet = protocol.read(buffer);
        return packet;
    }
}

export default ProtocolManager;