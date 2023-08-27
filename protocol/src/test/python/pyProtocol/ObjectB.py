
class ObjectB:

    flag = False  # bool

    def protocolId(self):
        return 103

    @classmethod
    def write(cls, buffer, packet):
        if buffer.writePacketFlag(packet):
            return
        buffer.writeBool(packet.flag)
        pass

    @classmethod
    def read(cls, buffer):
        if not buffer.readBool():
            return None
        packet = ObjectB()
        result0 = buffer.readBool() 
        packet.flag = result0
        return packet

