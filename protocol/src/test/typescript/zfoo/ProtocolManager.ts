import EmptyObject from './packet/EmptyObject';
import VeryBigObject from './packet/VeryBigObject';
import ComplexObject from './packet/ComplexObject';
import NormalObject from './packet/NormalObject';
import ObjectA from './packet/ObjectA';
import ObjectB from './packet/ObjectB';
import SimpleObject from './packet/SimpleObject';

const protocols = new Map<number, any>();

// initProtocol
protocols.set(0, EmptyObject);
protocols.set(1, VeryBigObject);
protocols.set(100, ComplexObject);
protocols.set(101, NormalObject);
protocols.set(102, ObjectA);
protocols.set(103, ObjectB);
protocols.set(104, SimpleObject);

class ProtocolManager {
    static getProtocol(protocolId: number): any {
        const protocol = protocols.get(protocolId);
        if (protocol === null) {
            throw '[protocolId:' + protocolId + ']协议不存在';
        }
        return protocol;
    }

    static write(buffer: any, packet: any): void {
        const protocolId = packet.protocolId();
        buffer.writeShort(protocolId);
        const protocol = ProtocolManager.getProtocol(protocolId);
        protocol.write(buffer, packet);
    }

    static read(buffer: any): any {
        const protocolId = buffer.readShort();
        const protocol = ProtocolManager.getProtocol(protocolId);
        const packet = protocol.read(buffer);
        return packet;
    }
}

export default ProtocolManager;
