${protocol_imports}
import IByteBuffer from "./IByteBuffer";

const protocols = new Map<number, any>();

// initProtocol
${protocol_manager_registrations}

class ProtocolManager {
    static getProtocol(protocolId: number): any {
        const protocol = protocols.get(protocolId);
        if (protocol === null) {
            throw '[protocolId:' + protocolId + '] not exist';
        }
        return protocol;
    }

    static write(buffer: IByteBuffer, packet: any): void {
        const protocolId = packet.protocolId();
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
