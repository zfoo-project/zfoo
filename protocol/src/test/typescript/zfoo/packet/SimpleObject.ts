
// @author godotg
// @version 3.0
class SimpleObject {

    c: number = 0;
    g: boolean = false;

    protocolId(): number {
        return 104;
    }

    static write(buffer: any, packet: SimpleObject | null) {
        if (buffer.writePacketFlag(packet)) {
            return;
        }
        if (packet === null) {
            return;
        }

        buffer.writeInt(packet.c);
        buffer.writeBoolean(packet.g);
    }

    static read(buffer: any): SimpleObject | null {
        if (!buffer.readBoolean()) {
            return null;
        }
        const packet = new SimpleObject();
        const result0 = buffer.readInt();
        packet.c = result0;
        const result1 = buffer.readBoolean(); 
        packet.g = result1;
        return packet;
    }
}

export default SimpleObject;
