
class ObjectA:

    a = 0  # int
    m = {}  # Dictionary<int, string>
    objectB = None  # ObjectB

    def protocolId(self):
        return 102

    @classmethod
    def write(cls, buffer, packet):
        if buffer.writePacketFlag(packet):
            return
        buffer.writeInt(packet.a)
        buffer.writeIntStringMap(packet.m)
        buffer.writePacket(packet.objectB, 103)
        pass

    @classmethod
    def read(cls, buffer):
        if not buffer.readBool():
            return None
        packet = ObjectA()
        result0 = buffer.readInt()
        packet.a = result0
        map1 = buffer.readIntStringMap()
        packet.m = map1
        result2 = buffer.readPacket(103)
        packet.objectB = result2
        return packet

