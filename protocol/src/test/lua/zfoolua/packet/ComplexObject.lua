-- 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
local ComplexObject = {}

function ComplexObject:new()
    local obj = {
        -- byte类型，最简单的整形
        a = 0, -- byte
        -- byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
        aa = 0, -- byte
        -- 数组类型
        aaa = {}, -- byte[]
        aaaa = {}, -- byte[]
        b = 0, -- short
        bb = 0, -- short
        bbb = {}, -- short[]
        bbbb = {}, -- short[]
        c = 0, -- int
        cc = 0, -- int
        ccc = {}, -- int[]
        cccc = {}, -- int[]
        d = 0, -- long
        dd = 0, -- long
        ddd = {}, -- long[]
        dddd = {}, -- long[]
        e = 0, -- float
        ee = 0, -- float
        eee = {}, -- float[]
        eeee = {}, -- float[]
        f = 0, -- double
        ff = 0, -- double
        fff = {}, -- double[]
        ffff = {}, -- double[]
        g = false, -- bool
        gg = false, -- bool
        ggg = {}, -- bool[]
        gggg = {}, -- bool[]
        jj = "", -- string
        jjj = {}, -- string[]
        kk = nil, -- ObjectA
        kkk = {}, -- ObjectA[]
        l = {}, -- List<int>
        ll = {}, -- List<List<List<int>>>
        lll = {}, -- List<List<ObjectA>>
        llll = {}, -- List<string>
        lllll = {}, -- List<Dictionary<int, string>>
        m = {}, -- Dictionary<int, string>
        mm = {}, -- Dictionary<int, ObjectA>
        mmm = {}, -- Dictionary<ObjectA, List<int>>
        mmmm = {}, -- Dictionary<List<List<ObjectA>>, List<List<List<int>>>>
        mmmmm = {}, -- Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>>
        s = {}, -- HashSet<int>
        ss = {}, -- HashSet<HashSet<List<int>>>
        sss = {}, -- HashSet<HashSet<ObjectA>>
        ssss = {}, -- HashSet<string>
        sssss = {}, -- HashSet<Dictionary<int, string>>
        -- 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
        myCompatible = 0, -- int
        myObject = nil -- ObjectA
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ComplexObject:protocolId()
    return 100
end

function ComplexObject:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    local beforeWriteIndex = buffer:getWriteOffset()
    buffer:writeInt(36962)
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
    buffer:adjustPadding(36962, beforeWriteIndex)
end

function ComplexObject:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
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
    local result60 = buffer:readString()
    packet.jj = result60
    local array61 = buffer:readStringArray()
    packet.jjj = array61
    local result62 = buffer:readPacket(102)
    packet.kk = result62
    local array63 = buffer:readPacketArray(102)
    packet.kkk = array63
    local list64 = buffer:readIntArray()
    packet.l = list64
    local result65 = {}
    local size66 = buffer:readInt()
    if size66 > 0 then
        for index67 = 1, size66 do
            local result68 = {}
            local size69 = buffer:readInt()
            if size69 > 0 then
                for index70 = 1, size69 do
                    local list71 = buffer:readIntArray()
                    table.insert(result68, list71)
                end
            end
            table.insert(result65, result68)
        end
    end
    packet.ll = result65
    local result72 = {}
    local size73 = buffer:readInt()
    if size73 > 0 then
        for index74 = 1, size73 do
            local list75 = buffer:readPacketArray(102)
            table.insert(result72, list75)
        end
    end
    packet.lll = result72
    local list76 = buffer:readStringArray()
    packet.llll = list76
    local result77 = {}
    local size78 = buffer:readInt()
    if size78 > 0 then
        for index79 = 1, size78 do
            local map80 = buffer:readIntStringMap()
            table.insert(result77, map80)
        end
    end
    packet.lllll = result77
    local map81 = buffer:readIntStringMap()
    packet.m = map81
    local map82 = buffer:readIntPacketMap(102)
    packet.mm = map82
    local result83 = {}
    local size84 = buffer:readInt()
    if size84 > 0 then
        for index85 = 1, size84 do
            local result86 = buffer:readPacket(102)
            local list87 = buffer:readIntArray()
            result83[result86] = list87
        end
    end
    packet.mmm = result83
    local result88 = {}
    local size89 = buffer:readInt()
    if size89 > 0 then
        for index90 = 1, size89 do
            local result91 = {}
            local size92 = buffer:readInt()
            if size92 > 0 then
                for index93 = 1, size92 do
                    local list94 = buffer:readPacketArray(102)
                    table.insert(result91, list94)
                end
            end
            local result95 = {}
            local size96 = buffer:readInt()
            if size96 > 0 then
                for index97 = 1, size96 do
                    local result98 = {}
                    local size99 = buffer:readInt()
                    if size99 > 0 then
                        for index100 = 1, size99 do
                            local list101 = buffer:readIntArray()
                            table.insert(result98, list101)
                        end
                    end
                    table.insert(result95, result98)
                end
            end
            result88[result91] = result95
        end
    end
    packet.mmmm = result88
    local result102 = {}
    local size103 = buffer:readInt()
    if size103 > 0 then
        for index104 = 1, size103 do
            local result105 = {}
            local size106 = buffer:readInt()
            if size106 > 0 then
                for index107 = 1, size106 do
                    local map108 = buffer:readIntStringMap()
                    table.insert(result105, map108)
                end
            end
            local result109 = {}
            local size110 = buffer:readInt()
            if size110 > 0 then
                for index111 = 1, size110 do
                    local map112 = buffer:readIntStringMap()
                    table.insert(result109, map112)
                end
            end
            result102[result105] = result109
        end
    end
    packet.mmmmm = result102
    local set113 = buffer:readIntArray()
    packet.s = set113
    local result114 = {}
    local size115 = buffer:readInt()
    if size115 > 0 then
        for index116 = 1, size115 do
            local result117 = {}
            local size118 = buffer:readInt()
            if size118 > 0 then
                for index119 = 1, size118 do
                    local list120 = buffer:readIntArray()
                    table.insert(result117, list120)
                end
            end
            table.insert(result114, result117)
        end
    end
    packet.ss = result114
    local result121 = {}
    local size122 = buffer:readInt()
    if size122 > 0 then
        for index123 = 1, size122 do
            local set124 = buffer:readPacketArray(102)
            table.insert(result121, set124)
        end
    end
    packet.sss = result121
    local set125 = buffer:readStringArray()
    packet.ssss = set125
    local result126 = {}
    local size127 = buffer:readInt()
    if size127 > 0 then
        for index128 = 1, size127 do
            local map129 = buffer:readIntStringMap()
            table.insert(result126, map129)
        end
    end
    packet.sssss = result126
    if buffer:compatibleRead(beforeReadIndex, length) then
        local result130 = buffer:readInt()
        packet.myCompatible = result130
    end
    if buffer:compatibleRead(beforeReadIndex, length) then
        local result131 = buffer:readPacket(102)
        packet.myObject = result131
    end
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return ComplexObject
