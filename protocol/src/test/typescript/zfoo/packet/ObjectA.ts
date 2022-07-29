import ObjectB from './ObjectB';

// @author godotg
// @version 3.0
class ObjectA {

    a: number = 0;
    m: Map<number, string> | null = null;
    objectB: ObjectB | null = null;

    protocolId(): number {
        return 102;
    }

    static write(buffer: any, packet: ObjectA | null) {
        if (buffer.writePacketFlag(packet)) {
            return;
        }
        if (packet === null) {
            return;
        }

        buffer.writeInt(packet.a);
        buffer.writeIntStringMap(packet.m);
        buffer.writePacket(packet.objectB, 103);
    }

    static read(buffer: any): ObjectA | null {
        if (!buffer.readBoolean()) {
            return null;
        }
        const packet = new ObjectA();
        const result0 = buffer.readInt();
        packet.a = result0;
        const map1 = buffer.readIntStringMap();
        packet.m = map1;
        const result2 = buffer.readPacket(103);
        packet.objectB = result2;
        return packet;
    }
}

export default ObjectA;
