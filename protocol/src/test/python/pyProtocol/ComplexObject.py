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
    h = ""  # char
    hh = ""  # char
    hhh = []  # char[]
    hhhh = []  # char[]
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
        if buffer.writePacketFlag(packet):
            return
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
        buffer.writeChar(packet.h)
        buffer.writeChar(packet.hh)
        buffer.writeCharArray(packet.hhh)
        buffer.writeCharArray(packet.hhhh)
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
        pass

    @classmethod
    def read(cls, buffer):
        if not buffer.readBool():
            return None
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
        result47 = buffer.readChar()
        packet.h = result47
        result48 = buffer.readChar()
        packet.hh = result48
        array49 = buffer.readCharArray()
        packet.hhh = array49
        array50 = buffer.readCharArray()
        packet.hhhh = array50
        result51 = buffer.readString()
        packet.jj = result51
        array52 = buffer.readStringArray()
        packet.jjj = array52
        result53 = buffer.readPacket(102)
        packet.kk = result53
        array54 = buffer.readPacketArray(102)
        packet.kkk = array54
        list55 = buffer.readIntArray()
        packet.l = list55
        result56 = []
        size58 = buffer.readInt()
        if size58 > 0:
            for index57 in range(size58):
                result59 = []
                size61 = buffer.readInt()
                if size61 > 0:
                    for index60 in range(size61):
                        list62 = buffer.readIntArray()
                        result59.append(list62)
                result56.append(result59)
        packet.ll = result56
        result63 = []
        size65 = buffer.readInt()
        if size65 > 0:
            for index64 in range(size65):
                list66 = buffer.readPacketArray(102)
                result63.append(list66)
        packet.lll = result63
        list67 = buffer.readStringArray()
        packet.llll = list67
        result68 = []
        size70 = buffer.readInt()
        if size70 > 0:
            for index69 in range(size70):
                map71 = buffer.readIntStringMap()
                result68.append(map71)
        packet.lllll = result68
        map72 = buffer.readIntStringMap()
        packet.m = map72
        map73 = buffer.readIntPacketMap(102)
        packet.mm = map73
        result74 = {}
        size75 = buffer.readInt()
        if size75 > 0:
            for index76 in range(size75):
                result77 = buffer.readPacket(102)
                list78 = buffer.readIntArray()
                result74[result77] = list78
        packet.mmm = result74
        result79 = {}
        size80 = buffer.readInt()
        if size80 > 0:
            for index81 in range(size80):
                result82 = []
                size84 = buffer.readInt()
                if size84 > 0:
                    for index83 in range(size84):
                        list85 = buffer.readPacketArray(102)
                        result82.append(list85)
                result86 = []
                size88 = buffer.readInt()
                if size88 > 0:
                    for index87 in range(size88):
                        result89 = []
                        size91 = buffer.readInt()
                        if size91 > 0:
                            for index90 in range(size91):
                                list92 = buffer.readIntArray()
                                result89.append(list92)
                        result86.append(result89)
                result79[result82] = result86
        packet.mmmm = result79
        result93 = {}
        size94 = buffer.readInt()
        if size94 > 0:
            for index95 in range(size94):
                result96 = []
                size98 = buffer.readInt()
                if size98 > 0:
                    for index97 in range(size98):
                        map99 = buffer.readIntStringMap()
                        result96.append(map99)
                result100 = []
                size102 = buffer.readInt()
                if size102 > 0:
                    for index101 in range(size102):
                        map103 = buffer.readIntStringMap()
                        result100.append(map103)
                result93[result96] = result100
        packet.mmmmm = result93
        set104 = buffer.readIntSet()
        packet.s = set104
        result105 = []
        size107 = buffer.readInt()
        if size107 > 0:
            for index106 in range(size107):
                result108 = []
                size110 = buffer.readInt()
                if size110 > 0:
                    for index109 in range(size110):
                        list111 = buffer.readIntArray()
                        result108.append(list111)
                result105.append(result108)
        packet.ss = result105
        result112 = []
        size114 = buffer.readInt()
        if size114 > 0:
            for index113 in range(size114):
                set115 = buffer.readPacketSet(102)
                result112.append(set115)
        packet.sss = result112
        set116 = buffer.readStringSet()
        packet.ssss = set116
        result117 = []
        size119 = buffer.readInt()
        if size119 > 0:
            for index118 in range(size119):
                map120 = buffer.readIntStringMap()
                result117.append(map120)
        packet.sssss = result117
        if not buffer.isReadable():
            return packet
        pass
        result121 = buffer.readInt()
        packet.myCompatible = result121
        if not buffer.isReadable():
            return packet
        pass
        result122 = buffer.readPacket(102)
        packet.myObject = result122
        return packet

