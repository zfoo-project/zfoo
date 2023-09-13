-- 复杂的对象
-- 包括了各种复杂的结构，数组，List，Set，Map
--
-- @author godotg

local ComplexObject = {}

function ComplexObject:new(a, aa, aaa, aaaa, b, bb, bbb, bbbb, c, cc, ccc, cccc, d, dd, ddd, dddd, e, ee, eee, eeee, f, ff, fff, ffff, g, gg, ggg, gggg, h, hh, hhh, hhhh, jj, jjj, kk, kkk, l, ll, lll, llll, lllll, m, mm, mmm, mmmm, mmmmm, s, ss, sss, ssss, sssss, myCompatible, myObject)
    local obj = {
        -- byte类型，最简单的整形
        a = a, -- byte
        -- byte的包装类型
        -- 优先使用基础类型，包装类型会有装箱拆箱
        aa = aa, -- java.lang.Byte
        -- 数组类型
        aaa = aaa, -- byte[]
        aaaa = aaaa, -- java.lang.Byte[]
        b = b, -- short
        bb = bb, -- java.lang.Short
        bbb = bbb, -- short[]
        bbbb = bbbb, -- java.lang.Short[]
        c = c, -- int
        cc = cc, -- java.lang.Integer
        ccc = ccc, -- int[]
        cccc = cccc, -- java.lang.Integer[]
        d = d, -- long
        dd = dd, -- java.lang.Long
        ddd = ddd, -- long[]
        dddd = dddd, -- java.lang.Long[]
        e = e, -- float
        ee = ee, -- java.lang.Float
        eee = eee, -- float[]
        eeee = eeee, -- java.lang.Float[]
        f = f, -- double
        ff = ff, -- java.lang.Double
        fff = fff, -- double[]
        ffff = ffff, -- java.lang.Double[]
        g = g, -- boolean
        gg = gg, -- java.lang.Boolean
        ggg = ggg, -- boolean[]
        gggg = gggg, -- java.lang.Boolean[]
        h = h, -- char
        hh = hh, -- java.lang.Character
        hhh = hhh, -- char[]
        hhhh = hhhh, -- java.lang.Character[]
        jj = jj, -- java.lang.String
        jjj = jjj, -- java.lang.String[]
        kk = kk, -- com.zfoo.protocol.packet.ObjectA
        kkk = kkk, -- com.zfoo.protocol.packet.ObjectA[]
        l = l, -- java.util.List<java.lang.Integer>
        ll = ll, -- java.util.List<java.util.List<java.util.List<java.lang.Integer>>>
        lll = lll, -- java.util.List<java.util.List<com.zfoo.protocol.packet.ObjectA>>
        llll = llll, -- java.util.List<java.lang.String>
        lllll = lllll, -- java.util.List<java.util.Map<java.lang.Integer, java.lang.String>>
        m = m, -- java.util.Map<java.lang.Integer, java.lang.String>
        mm = mm, -- java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
        mmm = mmm, -- java.util.Map<com.zfoo.protocol.packet.ObjectA, java.util.List<java.lang.Integer>>
        mmmm = mmmm, -- java.util.Map<java.util.List<java.util.List<com.zfoo.protocol.packet.ObjectA>>, java.util.List<java.util.List<java.util.List<java.lang.Integer>>>>
        mmmmm = mmmmm, -- java.util.Map<java.util.List<java.util.Map<java.lang.Integer, java.lang.String>>, java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>>
        s = s, -- java.util.Set<java.lang.Integer>
        ss = ss, -- java.util.Set<java.util.Set<java.util.List<java.lang.Integer>>>
        sss = sss, -- java.util.Set<java.util.Set<com.zfoo.protocol.packet.ObjectA>>
        ssss = ssss, -- java.util.Set<java.lang.String>
        sssss = sssss, -- java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>
        -- 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
        myCompatible = myCompatible, -- int
        myObject = myObject -- com.zfoo.protocol.packet.ObjectA
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ComplexObject:protocolId()
    return 100
end

function ComplexObject:write(buffer, packet)
    if buffer:writePacketFlag(packet) then
        return
    end
    buffer:writeByte(packet.a)
    buffer:writeByte(packet.aa)
    buffer:writeByteArray(packet.aaa)
    buffer:writeByteArray(packet.aaaa)
    buffer:writeShort(packet.b)
    buffer:writeShort(packet.bb)
    buffer:writeShortArray(packet.bbb)
    buffer:writeShortArray(packet.bbbb)
    buffer:writeInt(packet.c)
    buffer:writeInt(packet.cc)
    buffer:writeIntArray(packet.ccc)
    buffer:writeIntArray(packet.cccc)
    buffer:writeLong(packet.d)
    buffer:writeLong(packet.dd)
    buffer:writeLongArray(packet.ddd)
    buffer:writeLongArray(packet.dddd)
    buffer:writeFloat(packet.e)
    buffer:writeFloat(packet.ee)
    buffer:writeFloatArray(packet.eee)
    buffer:writeFloatArray(packet.eeee)
    buffer:writeDouble(packet.f)
    buffer:writeDouble(packet.ff)
    buffer:writeDoubleArray(packet.fff)
    buffer:writeDoubleArray(packet.ffff)
    buffer:writeBoolean(packet.g)
    buffer:writeBoolean(packet.gg)
    buffer:writeBooleanArray(packet.ggg)
    buffer:writeBooleanArray(packet.gggg)
    buffer:writeChar(packet.h)
    buffer:writeChar(packet.hh)
    buffer:writeCharArray(packet.hhh)
    buffer:writeCharArray(packet.hhhh)
    buffer:writeString(packet.jj)
    buffer:writeStringArray(packet.jjj)
    buffer:writePacket(packet.kk, 102)
    buffer:writePacketArray(packet.kkk, 102)
    buffer:writeIntArray(packet.l)
    if packet.ll == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(#packet.ll)
        for index0, element1 in pairs(packet.ll) do
            if element1 == null then
                buffer:writeInt(0)
            else
                buffer:writeInt(#element1)
                for index2, element3 in pairs(element1) do
                    buffer:writeIntArray(element3)
                end
            end
        end
    end
    if packet.lll == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(#packet.lll)
        for index4, element5 in pairs(packet.lll) do
            buffer:writePacketArray(element5, 102)
        end
    end
    buffer:writeStringArray(packet.llll)
    if packet.lllll == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(#packet.lllll)
        for index6, element7 in pairs(packet.lllll) do
            buffer:writeIntStringMap(element7)
        end
    end
    buffer:writeIntStringMap(packet.m)
    buffer:writeIntPacketMap(packet.mm, 102)
    if packet.mmm == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(table.mapSize(packet.mmm))
        for key8, value9 in pairs(packet.mmm) do
            buffer:writePacket(key8, 102)
            buffer:writeIntArray(value9)
        end
    end
    if packet.mmmm == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(table.mapSize(packet.mmmm))
        for key10, value11 in pairs(packet.mmmm) do
            if key10 == null then
                buffer:writeInt(0)
            else
                buffer:writeInt(#key10)
                for index12, element13 in pairs(key10) do
                    buffer:writePacketArray(element13, 102)
                end
            end
            if value11 == null then
                buffer:writeInt(0)
            else
                buffer:writeInt(#value11)
                for index14, element15 in pairs(value11) do
                    if element15 == null then
                        buffer:writeInt(0)
                    else
                        buffer:writeInt(#element15)
                        for index16, element17 in pairs(element15) do
                            buffer:writeIntArray(element17)
                        end
                    end
                end
            end
        end
    end
    if packet.mmmmm == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(table.mapSize(packet.mmmmm))
        for key18, value19 in pairs(packet.mmmmm) do
            if key18 == null then
                buffer:writeInt(0)
            else
                buffer:writeInt(#key18)
                for index20, element21 in pairs(key18) do
                    buffer:writeIntStringMap(element21)
                end
            end
            if value19 == null then
                buffer:writeInt(0)
            else
                buffer:writeInt(#value19)
                for index22, element23 in pairs(value19) do
                    buffer:writeIntStringMap(element23)
                end
            end
        end
    end
    buffer:writeIntArray(packet.s)
    if packet.ss == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(#packet.ss)
        for index24, element25 in pairs(packet.ss) do
            if element25 == null then
                buffer:writeInt(0)
            else
                buffer:writeInt(#element25)
                for index26, element27 in pairs(element25) do
                    buffer:writeIntArray(element27)
                end
            end
        end
    end
    if packet.sss == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(#packet.sss)
        for index28, element29 in pairs(packet.sss) do
            buffer:writePacketArray(element29, 102)
        end
    end
    buffer:writeStringArray(packet.ssss)
    if packet.sssss == null then
        buffer:writeInt(0)
    else
        buffer:writeInt(#packet.sssss)
        for index30, element31 in pairs(packet.sssss) do
            buffer:writeIntStringMap(element31)
        end
    end
    buffer:writeInt(packet.myCompatible)
    buffer:writePacket(packet.myObject, 102)
end

function ComplexObject:read(buffer)
    if not(buffer:readBoolean()) then
        return nil
    end
    local packet = ComplexObject:new()
    local result32 = buffer:readByte()
    packet.a = result32
    local result33 = buffer:readByte()
    packet.aa = result33
    local array34 = buffer:readByteArray()
    packet.aaa = array34
    local array35 = buffer:readByteArray()
    packet.aaaa = array35
    local result36 = buffer:readShort()
    packet.b = result36
    local result37 = buffer:readShort()
    packet.bb = result37
    local array38 = buffer:readShortArray()
    packet.bbb = array38
    local array39 = buffer:readShortArray()
    packet.bbbb = array39
    local result40 = buffer:readInt()
    packet.c = result40
    local result41 = buffer:readInt()
    packet.cc = result41
    local array42 = buffer:readIntArray()
    packet.ccc = array42
    local array43 = buffer:readIntArray()
    packet.cccc = array43
    local result44 = buffer:readLong()
    packet.d = result44
    local result45 = buffer:readLong()
    packet.dd = result45
    local array46 = buffer:readLongArray()
    packet.ddd = array46
    local array47 = buffer:readLongArray()
    packet.dddd = array47
    local result48 = buffer:readFloat()
    packet.e = result48
    local result49 = buffer:readFloat()
    packet.ee = result49
    local array50 = buffer:readFloatArray()
    packet.eee = array50
    local array51 = buffer:readFloatArray()
    packet.eeee = array51
    local result52 = buffer:readDouble()
    packet.f = result52
    local result53 = buffer:readDouble()
    packet.ff = result53
    local array54 = buffer:readDoubleArray()
    packet.fff = array54
    local array55 = buffer:readDoubleArray()
    packet.ffff = array55
    local result56 = buffer:readBoolean()
    packet.g = result56
    local result57 = buffer:readBoolean()
    packet.gg = result57
    local array58 = buffer:readBooleanArray()
    packet.ggg = array58
    local array59 = buffer:readBooleanArray()
    packet.gggg = array59
    local result60 = buffer:readChar()
    packet.h = result60
    local result61 = buffer:readChar()
    packet.hh = result61
    local array62 = buffer:readCharArray()
    packet.hhh = array62
    local array63 = buffer:readCharArray()
    packet.hhhh = array63
    local result64 = buffer:readString()
    packet.jj = result64
    local array65 = buffer:readStringArray()
    packet.jjj = array65
    local result66 = buffer:readPacket(102)
    packet.kk = result66
    local array67 = buffer:readPacketArray(102)
    packet.kkk = array67
    local list68 = buffer:readIntArray()
    packet.l = list68
    local result69 = {}
    local size70 = buffer:readInt()
    if size70 > 0 then
        for index71 = 1, size70 do
            local result72 = {}
            local size73 = buffer:readInt()
            if size73 > 0 then
                for index74 = 1, size73 do
                    local list75 = buffer:readIntArray()
                    table.insert(result72, list75)
                end
            end
            table.insert(result69, result72)
        end
    end
    packet.ll = result69
    local result76 = {}
    local size77 = buffer:readInt()
    if size77 > 0 then
        for index78 = 1, size77 do
            local list79 = buffer:readPacketArray(102)
            table.insert(result76, list79)
        end
    end
    packet.lll = result76
    local list80 = buffer:readStringArray()
    packet.llll = list80
    local result81 = {}
    local size82 = buffer:readInt()
    if size82 > 0 then
        for index83 = 1, size82 do
            local map84 = buffer:readIntStringMap()
            table.insert(result81, map84)
        end
    end
    packet.lllll = result81
    local map85 = buffer:readIntStringMap()
    packet.m = map85
    local map86 = buffer:readIntPacketMap(102)
    packet.mm = map86
    local result87 = {}
    local size88 = buffer:readInt()
    if size88 > 0 then
        for index89 = 1, size88 do
            local result90 = buffer:readPacket(102)
            local list91 = buffer:readIntArray()
            result87[result90] = list91
        end
    end
    packet.mmm = result87
    local result92 = {}
    local size93 = buffer:readInt()
    if size93 > 0 then
        for index94 = 1, size93 do
            local result95 = {}
            local size96 = buffer:readInt()
            if size96 > 0 then
                for index97 = 1, size96 do
                    local list98 = buffer:readPacketArray(102)
                    table.insert(result95, list98)
                end
            end
            local result99 = {}
            local size100 = buffer:readInt()
            if size100 > 0 then
                for index101 = 1, size100 do
                    local result102 = {}
                    local size103 = buffer:readInt()
                    if size103 > 0 then
                        for index104 = 1, size103 do
                            local list105 = buffer:readIntArray()
                            table.insert(result102, list105)
                        end
                    end
                    table.insert(result99, result102)
                end
            end
            result92[result95] = result99
        end
    end
    packet.mmmm = result92
    local result106 = {}
    local size107 = buffer:readInt()
    if size107 > 0 then
        for index108 = 1, size107 do
            local result109 = {}
            local size110 = buffer:readInt()
            if size110 > 0 then
                for index111 = 1, size110 do
                    local map112 = buffer:readIntStringMap()
                    table.insert(result109, map112)
                end
            end
            local result113 = {}
            local size114 = buffer:readInt()
            if size114 > 0 then
                for index115 = 1, size114 do
                    local map116 = buffer:readIntStringMap()
                    table.insert(result113, map116)
                end
            end
            result106[result109] = result113
        end
    end
    packet.mmmmm = result106
    local set117 = buffer:readIntArray()
    packet.s = set117
    local result118 = {}
    local size119 = buffer:readInt()
    if size119 > 0 then
        for index120 = 1, size119 do
            local result121 = {}
            local size122 = buffer:readInt()
            if size122 > 0 then
                for index123 = 1, size122 do
                    local list124 = buffer:readIntArray()
                    table.insert(result121, list124)
                end
            end
            table.insert(result118, result121)
        end
    end
    packet.ss = result118
    local result125 = {}
    local size126 = buffer:readInt()
    if size126 > 0 then
        for index127 = 1, size126 do
            local set128 = buffer:readPacketArray(102)
            table.insert(result125, set128)
        end
    end
    packet.sss = result125
    local set129 = buffer:readStringArray()
    packet.ssss = set129
    local result130 = {}
    local size131 = buffer:readInt()
    if size131 > 0 then
        for index132 = 1, size131 do
            local map133 = buffer:readIntStringMap()
            table.insert(result130, map133)
        end
    end
    packet.sssss = result130
    if not(buffer:isReadable()) then
        return packet
    end
    local result134 = buffer:readInt()
    packet.myCompatible = result134
    if not(buffer:isReadable()) then
        return packet
    end
    local result135 = buffer:readPacket(102)
    packet.myObject = result135
    return packet
end

return ComplexObject
