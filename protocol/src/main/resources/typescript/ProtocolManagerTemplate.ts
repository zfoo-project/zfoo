${protocol_imports}
import IByteBuffer from "./IByteBuffer";
import IProtocolRegistration from "./IProtocolRegistration";

const protocols = new Map<number, IProtocolRegistration<unknown>>();
const protocolIdMap = new Map<any, number>();

// initProtocol
${protocol_manager_registrations}

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
