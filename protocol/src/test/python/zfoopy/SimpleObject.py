
class SimpleObject:

    c = 0  # int
    g = False  # bool

    def protocolId(self):
        return 104

    @classmethod
    def write(cls, buffer, packet):
        if packet is None:
            buffer.writeInt(0)
            return
        buffer.writeInt(-1)
        buffer.writeInt(packet.c)
        buffer.writeBool(packet.g)
        pass

    @classmethod
    def read(cls, buffer):
        length = buffer.readInt()
        if length == 0:
            return None
        beforeReadIndex = buffer.getReadOffset()
        packet = SimpleObject()
        result0 = buffer.readInt()
        packet.c = result0
        result1 = buffer.readBool() 
        packet.g = result1
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet

