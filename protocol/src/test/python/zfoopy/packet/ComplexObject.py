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
    # 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
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
        result0 = buffer.readByte()
        packet.a = result0
        result1 = buffer.readByte()
        packet.aa = result1
        array2 = buffer.readByteArray()
        packet.aaa = array2
        array3 = buffer.readByteArray()
        packet.aaaa = array3
        result4 = buffer.readShort()
        packet.b = result4
        result5 = buffer.readShort()
        packet.bb = result5
        array6 = buffer.readShortArray()
        packet.bbb = array6
        array7 = buffer.readShortArray()
        packet.bbbb = array7
        result8 = buffer.readInt()
        packet.c = result8
        result9 = buffer.readInt()
        packet.cc = result9
        array10 = buffer.readIntArray()
        packet.ccc = array10
        array11 = buffer.readIntArray()
        packet.cccc = array11
        result12 = buffer.readLong()
        packet.d = result12
        result13 = buffer.readLong()
        packet.dd = result13
        array14 = buffer.readLongArray()
        packet.ddd = array14
        array15 = buffer.readLongArray()
        packet.dddd = array15
        result16 = buffer.readFloat()
        packet.e = result16
        result17 = buffer.readFloat()
        packet.ee = result17
        array18 = buffer.readFloatArray()
        packet.eee = array18
        array19 = buffer.readFloatArray()
        packet.eeee = array19
        result20 = buffer.readDouble()
        packet.f = result20
        result21 = buffer.readDouble()
        packet.ff = result21
        array22 = buffer.readDoubleArray()
        packet.fff = array22
        array23 = buffer.readDoubleArray()
        packet.ffff = array23
        result24 = buffer.readBool() 
        packet.g = result24
        result25 = buffer.readBool() 
        packet.gg = result25
        array26 = buffer.readBooleanArray()
        packet.ggg = array26
        array27 = buffer.readBooleanArray()
        packet.gggg = array27
        result28 = buffer.readString()
        packet.jj = result28
        array29 = buffer.readStringArray()
        packet.jjj = array29
        result30 = buffer.readPacket(102)
        packet.kk = result30
        array31 = buffer.readPacketArray(102)
        packet.kkk = array31
        list32 = buffer.readIntArray()
        packet.l = list32
        result33 = []
        size35 = buffer.readInt()
        if size35 > 0:
            for index34 in range(size35):
                result36 = []
                size38 = buffer.readInt()
                if size38 > 0:
                    for index37 in range(size38):
                        list39 = buffer.readIntArray()
                        result36.append(list39)
                result33.append(result36)
        packet.ll = result33
        result40 = []
        size42 = buffer.readInt()
        if size42 > 0:
            for index41 in range(size42):
                list43 = buffer.readPacketArray(102)
                result40.append(list43)
        packet.lll = result40
        list44 = buffer.readStringArray()
        packet.llll = list44
        result45 = []
        size47 = buffer.readInt()
        if size47 > 0:
            for index46 in range(size47):
                map48 = buffer.readIntStringMap()
                result45.append(map48)
        packet.lllll = result45
        map49 = buffer.readIntStringMap()
        packet.m = map49
        map50 = buffer.readIntPacketMap(102)
        packet.mm = map50
        result51 = {}
        size52 = buffer.readInt()
        if size52 > 0:
            for index53 in range(size52):
                result54 = buffer.readPacket(102)
                list55 = buffer.readIntArray()
                result51[result54] = list55
        packet.mmm = result51
        result56 = {}
        size57 = buffer.readInt()
        if size57 > 0:
            for index58 in range(size57):
                result59 = []
                size61 = buffer.readInt()
                if size61 > 0:
                    for index60 in range(size61):
                        list62 = buffer.readPacketArray(102)
                        result59.append(list62)
                result63 = []
                size65 = buffer.readInt()
                if size65 > 0:
                    for index64 in range(size65):
                        result66 = []
                        size68 = buffer.readInt()
                        if size68 > 0:
                            for index67 in range(size68):
                                list69 = buffer.readIntArray()
                                result66.append(list69)
                        result63.append(result66)
                result56[result59] = result63
        packet.mmmm = result56
        result70 = {}
        size71 = buffer.readInt()
        if size71 > 0:
            for index72 in range(size71):
                result73 = []
                size75 = buffer.readInt()
                if size75 > 0:
                    for index74 in range(size75):
                        map76 = buffer.readIntStringMap()
                        result73.append(map76)
                result77 = []
                size79 = buffer.readInt()
                if size79 > 0:
                    for index78 in range(size79):
                        map80 = buffer.readIntStringMap()
                        result77.append(map80)
                result70[result73] = result77
        packet.mmmmm = result70
        set81 = buffer.readIntSet()
        packet.s = set81
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
        packet.ss = result82
        result89 = []
        size91 = buffer.readInt()
        if size91 > 0:
            for index90 in range(size91):
                set92 = buffer.readPacketSet(102)
                result89.append(set92)
        packet.sss = result89
        set93 = buffer.readStringSet()
        packet.ssss = set93
        result94 = []
        size96 = buffer.readInt()
        if size96 > 0:
            for index95 in range(size96):
                map97 = buffer.readIntStringMap()
                result94.append(map97)
        packet.sssss = result94
        if buffer.compatibleRead(beforeReadIndex, length):
            result98 = buffer.readInt()
            packet.myCompatible = result98
        if buffer.compatibleRead(beforeReadIndex, length):
            result99 = buffer.readPacket(102)
            packet.myObject = result99
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet