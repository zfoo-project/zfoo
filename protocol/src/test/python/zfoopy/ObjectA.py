
class ObjectA:

    a = 0  # int
    m = {}  # Dictionary<int, string>
    objectB = None  # ObjectB
    innerCompatibleValue = 0  # int

    def protocolId(self):
        return 102

    @classmethod
    def write(cls, buffer, packet):
        if packet is None:
            buffer.writeInt(0)
            return
        beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(201)
        buffer.writeInt(packet.a)
        buffer.writeIntStringMap(packet.m)
        buffer.writePacket(packet.objectB, 103)
        buffer.writeInt(packet.innerCompatibleValue)
        buffer.adjustPadding(201, beforeWriteIndex)
        pass

    @classmethod
    def read(cls, buffer):
        length = buffer.readInt()
        if length == 0:
            return None
        beforeReadIndex = buffer.getReadOffset()
        packet = ObjectA()
        result0 = buffer.readInt()
        packet.a = result0
        map1 = buffer.readIntStringMap()
        packet.m = map1
        result2 = buffer.readPacket(103)
        packet.objectB = result2
        if buffer.compatibleRead(beforeReadIndex, length):
            result3 = buffer.readInt()
            packet.innerCompatibleValue = result3
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet

