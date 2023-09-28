# 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
class ComplexObject:

    # byte类型，最简单的整形
    a = 0  # byte
    # byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
    aa = 0  # byte
    # 数组类型
    aaa = []  # byte[]
    aaaa = []  # byte[]
    b = 0  # short
    bb = 0  # short
    bbb = []  # short[]
    bbbb = []  # short[]
    c = 0  # int
    cc = 0  # int
    ccc = []  # int[]
    cccc = []  # int[]
    d = 0  # long
    dd = 0  # long
    ddd = []  # long[]
    dddd = []  # long[]
    e = 0.0  # float
    ee = 0.0  # float
    eee = []  # float[]
    eeee = []  # float[]
    f = 0.0  # double
    ff = 0.0  # double
    fff = []  # double[]
    ffff = []  # double[]
    g = False  # bool
    gg = False  # bool
    ggg = []  # bool[]
    gggg = []  # bool[]
    jj = ""  # string
    jjj = []  # string[]
    kk = None  # ObjectA
    kkk = []  # ObjectA[]
    l = []  # List<int>
    ll = []  # List<List<List<int>>>
    lll = []  # List<List<ObjectA>>
    llll = []  # List<string>
    lllll = []  # List<Dictionary<int, string>>
    m = {}  # Dictionary<int, string>
    mm = {}  # Dictionary<int, ObjectA>
    mmm = {}  # Dictionary<ObjectA, List<int>>
    mmmm = {}  # Dictionary<List<List<ObjectA>>, List<List<List<int>>>>
    mmmmm = {}  # Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>>
    s = {}  # HashSet<int>
    ss = {}  # HashSet<HashSet<List<int>>>
    sss = {}  # HashSet<HashSet<ObjectA>>
    ssss = {}  # HashSet<string>
    sssss = {}  # HashSet<Dictionary<int, string>>
    # 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
    myCompatible = 0  # int
    myObject = None  # ObjectA

    def protocolId(self):
        return 100

    @classmethod
    def write(cls, buffer, packet):
        if packet is None:
            buffer.writeInt(0)
            return
        beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(36962)
        buffer.writeByte(packet.a)
        buffer.writeByte(packet.aa)
        buffer.writeByteArray(packet.aaa)
        buffer.writeByteArray(packet.aaaa)
        buffer.writeShort(packet.b)
        buffer.writeShort(packet.bb)
        buffer.writeShortArray(packet.bbb)
        buffer.writeShortArray(packet.bbbb)
        buffer.writeInt(packet.c)
        buffer.writeInt(packet.cc)
        buffer.writeIntArray(packet.ccc)
        buffer.writeIntArray(packet.cccc)
        buffer.writeLong(packet.d)
        buffer.writeLong(packet.dd)
        buffer.writeLongArray(packet.ddd)
        buffer.writeLongArray(packet.dddd)
        buffer.writeFloat(packet.e)
        buffer.writeFloat(packet.ee)
        buffer.writeFloatArray(packet.eee)
        buffer.writeFloatArray(packet.eeee)
        buffer.writeDouble(packet.f)
        buffer.writeDouble(packet.ff)
        buffer.writeDoubleArray(packet.fff)
        buffer.writeDoubleArray(packet.ffff)
        buffer.writeBool(packet.g)
        buffer.writeBool(packet.gg)
        buffer.writeBooleanArray(packet.ggg)
        buffer.writeBooleanArray(packet.gggg)
        buffer.writeString(packet.jj)
        buffer.writeStringArray(packet.jjj)
        buffer.writePacket(packet.kk, 102)
        buffer.writePacketArray(packet.kkk, 102)
        buffer.writeIntArray(packet.l)
        if packet.ll is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.ll))
            for element0 in packet.ll:
                if element0 is None:
                    buffer.writeInt(0)
                else:
                    buffer.writeInt(len(element0))
                    for element1 in element0:
                        buffer.writeIntArray(element1)
        if packet.lll is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.lll))
            for element2 in packet.lll:
                buffer.writePacketArray(element2, 102)
        buffer.writeStringArray(packet.llll)
        if packet.lllll is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.lllll))
            for element3 in packet.lllll:
                buffer.writeIntStringMap(element3)
        buffer.writeIntStringMap(packet.m)
        buffer.writeIntPacketMap(packet.mm, 102)
        if packet.mmm is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.mmm))
            for key4 in packet.mmm:
                value5 = packet.mmm[key4]
                buffer.writePacket(key4, 102)
                buffer.writeIntArray(value5)
        if packet.mmmm is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.mmmm))
            for key6 in packet.mmmm:
                value7 = packet.mmmm[key6]
                if key6 is None:
                    buffer.writeInt(0)
                else:
                    buffer.writeInt(len(key6))
                    for element8 in key6:
                        buffer.writePacketArray(element8, 102)
                if value7 is None:
                    buffer.writeInt(0)
                else:
                    buffer.writeInt(len(value7))
                    for element9 in value7:
                        if element9 is None:
                            buffer.writeInt(0)
                        else:
                            buffer.writeInt(len(element9))
                            for element10 in element9:
                                buffer.writeIntArray(element10)
        if packet.mmmmm is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.mmmmm))
            for key11 in packet.mmmmm:
                value12 = packet.mmmmm[key11]
                if key11 is None:
                    buffer.writeInt(0)
                else:
                    buffer.writeInt(len(key11))
                    for element13 in key11:
                        buffer.writeIntStringMap(element13)
                if value12 is None:
                    buffer.writeInt(0)
                else:
                    buffer.writeInt(len(value12))
                    for element14 in value12:
                        buffer.writeIntStringMap(element14)
        buffer.writeIntSet(packet.s)
        if packet.ss is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.ss))
            for element15 in packet.ss:
                if element15 is None:
                    buffer.writeInt(0)
                else:
                    buffer.writeInt(len(element15))
                    for element16 in element15:
                        buffer.writeIntArray(element16)
        if packet.sss is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.sss))
            for element17 in packet.sss:
                buffer.writePacketSet(element17, 102)
        buffer.writeStringSet(packet.ssss)
        if packet.sssss is None:
            buffer.writeInt(0)
        else:
            buffer.writeInt(len(packet.sssss))
            for element18 in packet.sssss:
                buffer.writeIntStringMap(element18)
        buffer.writeInt(packet.myCompatible)
        buffer.writePacket(packet.myObject, 102)
        buffer.adjustPadding(36962, beforeWriteIndex)
        pass

    @classmethod
    def read(cls, buffer):
        length = buffer.readInt()
        if length == 0:
            return None
        beforeReadIndex = buffer.getReadOffset()
        packet = ComplexObject()
        result19 = buffer.readByte()
        packet.a = result19
        result20 = buffer.readByte()
        packet.aa = result20
        array21 = buffer.readByteArray()
        packet.aaa = array21
        array22 = buffer.readByteArray()
        packet.aaaa = array22
        result23 = buffer.readShort()
        packet.b = result23
        result24 = buffer.readShort()
        packet.bb = result24
        array25 = buffer.readShortArray()
        packet.bbb = array25
        array26 = buffer.readShortArray()
        packet.bbbb = array26
        result27 = buffer.readInt()
        packet.c = result27
        result28 = buffer.readInt()
        packet.cc = result28
        array29 = buffer.readIntArray()
        packet.ccc = array29
        array30 = buffer.readIntArray()
        packet.cccc = array30
        result31 = buffer.readLong()
        packet.d = result31
        result32 = buffer.readLong()
        packet.dd = result32
        array33 = buffer.readLongArray()
        packet.ddd = array33
        array34 = buffer.readLongArray()
        packet.dddd = array34
        result35 = buffer.readFloat()
        packet.e = result35
        result36 = buffer.readFloat()
        packet.ee = result36
        array37 = buffer.readFloatArray()
        packet.eee = array37
        array38 = buffer.readFloatArray()
        packet.eeee = array38
        result39 = buffer.readDouble()
        packet.f = result39
        result40 = buffer.readDouble()
        packet.ff = result40
        array41 = buffer.readDoubleArray()
        packet.fff = array41
        array42 = buffer.readDoubleArray()
        packet.ffff = array42
        result43 = buffer.readBool() 
        packet.g = result43
        result44 = buffer.readBool() 
        packet.gg = result44
        array45 = buffer.readBooleanArray()
        packet.ggg = array45
        array46 = buffer.readBooleanArray()
        packet.gggg = array46
        result47 = buffer.readString()
        packet.jj = result47
        array48 = buffer.readStringArray()
        packet.jjj = array48
        result49 = buffer.readPacket(102)
        packet.kk = result49
        array50 = buffer.readPacketArray(102)
        packet.kkk = array50
        list51 = buffer.readIntArray()
        packet.l = list51
        result52 = []
        size54 = buffer.readInt()
        if size54 > 0:
            for index53 in range(size54):
                result55 = []
                size57 = buffer.readInt()
                if size57 > 0:
                    for index56 in range(size57):
                        list58 = buffer.readIntArray()
                        result55.append(list58)
                result52.append(result55)
        packet.ll = result52
        result59 = []
        size61 = buffer.readInt()
        if size61 > 0:
            for index60 in range(size61):
                list62 = buffer.readPacketArray(102)
                result59.append(list62)
        packet.lll = result59
        list63 = buffer.readStringArray()
        packet.llll = list63
        result64 = []
        size66 = buffer.readInt()
        if size66 > 0:
            for index65 in range(size66):
                map67 = buffer.readIntStringMap()
                result64.append(map67)
        packet.lllll = result64
        map68 = buffer.readIntStringMap()
        packet.m = map68
        map69 = buffer.readIntPacketMap(102)
        packet.mm = map69
        result70 = {}
        size71 = buffer.readInt()
        if size71 > 0:
            for index72 in range(size71):
                result73 = buffer.readPacket(102)
                list74 = buffer.readIntArray()
                result70[result73] = list74
        packet.mmm = result70
        result75 = {}
        size76 = buffer.readInt()
        if size76 > 0:
            for index77 in range(size76):
                result78 = []
                size80 = buffer.readInt()
                if size80 > 0:
                    for index79 in range(size80):
                        list81 = buffer.readPacketArray(102)
                        result78.append(list81)
                result82 = []
                size84 = buffer.readInt()
                if size84 > 0:
                    for index83 in range(size84):
                        result85 = []
                        size87 = buffer.readInt()
                        if size87 > 0:
                            for index86 in range(size87):
                                list88 = buffer.readIntArray()
                                result85.append(list88)
                        result82.append(result85)
                result75[result78] = result82
        packet.mmmm = result75
        result89 = {}
        size90 = buffer.readInt()
        if size90 > 0:
            for index91 in range(size90):
                result92 = []
                size94 = buffer.readInt()
                if size94 > 0:
                    for index93 in range(size94):
                        map95 = buffer.readIntStringMap()
                        result92.append(map95)
                result96 = []
                size98 = buffer.readInt()
                if size98 > 0:
                    for index97 in range(size98):
                        map99 = buffer.readIntStringMap()
                        result96.append(map99)
                result89[result92] = result96
        packet.mmmmm = result89
        set100 = buffer.readIntSet()
        packet.s = set100
        result101 = []
        size103 = buffer.readInt()
        if size103 > 0:
            for index102 in range(size103):
                result104 = []
                size106 = buffer.readInt()
                if size106 > 0:
                    for index105 in range(size106):
                        list107 = buffer.readIntArray()
                        result104.append(list107)
                result101.append(result104)
        packet.ss = result101
        result108 = []
        size110 = buffer.readInt()
        if size110 > 0:
            for index109 in range(size110):
                set111 = buffer.readPacketSet(102)
                result108.append(set111)
        packet.sss = result108
        set112 = buffer.readStringSet()
        packet.ssss = set112
        result113 = []
        size115 = buffer.readInt()
        if size115 > 0:
            for index114 in range(size115):
                map116 = buffer.readIntStringMap()
                result113.append(map116)
        packet.sssss = result113
        if buffer.compatibleRead(beforeReadIndex, length):
            result117 = buffer.readInt()
            packet.myCompatible = result117
        if buffer.compatibleRead(beforeReadIndex, length):
            result118 = buffer.readPacket(102)
            packet.myObject = result118
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet

