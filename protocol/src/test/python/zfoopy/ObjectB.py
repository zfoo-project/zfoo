
class ObjectB:

    flag = False  # bool
    innerCompatibleValue = 0  # int

    def protocolId(self):
        return 103

    @classmethod
    def write(cls, buffer, packet):
        if packet is None:
            buffer.writeInt(0)
            return
        beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(4)
        buffer.writeBool(packet.flag)
        buffer.writeInt(packet.innerCompatibleValue)
        buffer.adjustPadding(4, beforeWriteIndex)
        pass

    @classmethod
    def read(cls, buffer):
        length = buffer.readInt()
        if length == 0:
            return None
        beforeReadIndex = buffer.getReadOffset()
        packet = ObjectB()
        result0 = buffer.readBool() 
        packet.flag = result0
        if buffer.compatibleRead(beforeReadIndex, length):
            result1 = buffer.readInt()
            packet.innerCompatibleValue = result1
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet

