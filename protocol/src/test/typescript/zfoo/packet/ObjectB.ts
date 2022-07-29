
// @author godotg
// @version 3.0
class ObjectB {

    flag: boolean = false;

    protocolId(): number {
        return 103;
    }

    static write(buffer: any, packet: ObjectB | null) {
        if (buffer.writePacketFlag(packet)) {
            return;
        }
        if (packet === null) {
            return;
        }

        buffer.writeBoolean(packet.flag);
    }

    static read(buffer: any): ObjectB | null {
        if (!buffer.readBoolean()) {
            return null;
        }
        const packet = new ObjectB();
        const result0 = buffer.readBoolean(); 
        packet.flag = result0;
        return packet;
    }
}

export default ObjectB;
