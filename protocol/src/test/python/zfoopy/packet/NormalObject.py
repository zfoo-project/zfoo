# 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject:
    a = 0  # byte
    aaa = []  # byte[]
    b = 0  # short
    # 整数类型
    c = 0  # int
    d = 0  # long
    e = 0.0  # float
    f = 0.0  # double
    g = False  # bool
    jj = ""  # string
    kk = None  # ObjectA
    l = []  # List<int>
    ll = []  # List<long>
    lll = []  # List<ObjectA>
    llll = []  # List<string>
    m = {}  # Dictionary<int, string>
    mm = {}  # Dictionary<int, ObjectA>
    s = {}  # HashSet<int>
    ssss = {}  # HashSet<string>
    outCompatibleValue = 0  # int
    outCompatibleValue2 = 0  # int
    pass

class NormalObjectRegistration:
    @classmethod
    def protocolId(cls, self):
        return 101

    @classmethod
    def write(cls, buffer, packet):
        if packet is None:
            buffer.writeInt(0)
            return
        beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(857)
        buffer.writeByte(packet.a)
        buffer.writeByteArray(packet.aaa)
        buffer.writeShort(packet.b)
        buffer.writeInt(packet.c)
        buffer.writeLong(packet.d)
        buffer.writeFloat(packet.e)
        buffer.writeDouble(packet.f)
        buffer.writeBool(packet.g)
        buffer.writeString(packet.jj)
        buffer.writePacket(packet.kk, 102)
        buffer.writeIntArray(packet.l)
        buffer.writeLongArray(packet.ll)
        buffer.writePacketArray(packet.lll, 102)
        buffer.writeStringArray(packet.llll)
        buffer.writeIntStringMap(packet.m)
        buffer.writeIntPacketMap(packet.mm, 102)
        buffer.writeIntSet(packet.s)
        buffer.writeStringSet(packet.ssss)
        buffer.writeInt(packet.outCompatibleValue)
        buffer.writeInt(packet.outCompatibleValue2)
        buffer.adjustPadding(857, beforeWriteIndex)
        pass

    @classmethod
    def read(cls, buffer):
        length = buffer.readInt()
        if length == 0:
            return None
        beforeReadIndex = buffer.getReadOffset()
        packet = NormalObject()
        result0 = buffer.readByte()
        packet.a = result0
        array1 = buffer.readByteArray()
        packet.aaa = array1
        result2 = buffer.readShort()
        packet.b = result2
        result3 = buffer.readInt()
        packet.c = result3
        result4 = buffer.readLong()
        packet.d = result4
        result5 = buffer.readFloat()
        packet.e = result5
        result6 = buffer.readDouble()
        packet.f = result6
        result7 = buffer.readBool() 
        packet.g = result7
        result8 = buffer.readString()
        packet.jj = result8
        result9 = buffer.readPacket(102)
        packet.kk = result9
        list10 = buffer.readIntArray()
        packet.l = list10
        list11 = buffer.readLongArray()
        packet.ll = list11
        list12 = buffer.readPacketArray(102)
        packet.lll = list12
        list13 = buffer.readStringArray()
        packet.llll = list13
        map14 = buffer.readIntStringMap()
        packet.m = map14
        map15 = buffer.readIntPacketMap(102)
        packet.mm = map15
        set16 = buffer.readIntSet()
        packet.s = set16
        set17 = buffer.readStringSet()
        packet.ssss = set17
        if buffer.compatibleRead(beforeReadIndex, length):
            result18 = buffer.readInt()
            packet.outCompatibleValue = result18
        if buffer.compatibleRead(beforeReadIndex, length):
            result19 = buffer.readInt()
            packet.outCompatibleValue2 = result19
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet