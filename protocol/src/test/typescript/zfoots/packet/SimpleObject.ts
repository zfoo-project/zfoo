import IByteBuffer from '../IByteBuffer';
import IProtocolRegistration from '../IProtocolRegistration';


class SimpleObject {
    c: number = 0;
    g: boolean = false;
}

export class SimpleObjectRegistration implements IProtocolRegistration<SimpleObject> {
    protocolId(): number {
        return 104;
    }

    write(buffer: IByteBuffer, packet: SimpleObject | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeInt(packet.c);
        buffer.writeBoolean(packet.g);
    }

    read(buffer: IByteBuffer): SimpleObject | null {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new SimpleObject();
        const result0 = buffer.readInt();
        packet.c = result0;
        const result1 = buffer.readBoolean(); 
        packet.g = result1;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}

export default SimpleObject;