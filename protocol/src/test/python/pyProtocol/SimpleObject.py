
class SimpleObject:

    c = 0  # int
    g = False  # bool

    def protocolId(self):
        return 104

    @classmethod
    def write(cls, buffer, packet):
        if buffer.writePacketFlag(packet):
            return
        buffer.writeInt(packet.c)
        buffer.writeBool(packet.g)
        pass

    @classmethod
    def read(cls, buffer):
        if not buffer.readBool():
            return None
        packet = SimpleObject()
        result0 = buffer.readInt()
        packet.c = result0
        result1 = buffer.readBool() 
        packet.g = result1
        return packet

