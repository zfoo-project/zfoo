
class NormalObject:

    a = 0  # byte
    aaa = []  # byte[]
    b = 0  # short
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

    def protocolId(self):
        return 101

    @classmethod
    def write(cls, buffer, packet):
        if buffer.writePacketFlag(packet):
            return
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
        pass

    @classmethod
    def read(cls, buffer):
        if not buffer.readBool():
            return None
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
        return packet

