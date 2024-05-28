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
        -- 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
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

function ComplexObject:protocolName()
    return "ComplexObject"
end

function ComplexObject:__tostring()
    return table.serializeTableToJson(self)
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
    local result0 = buffer:readByte()
    packet.a = result0
    local result1 = buffer:readByte()
    packet.aa = result1
    local array2 = buffer:readByteArray()
    packet.aaa = array2
    local array3 = buffer:readByteArray()
    packet.aaaa = array3
    local result4 = buffer:readShort()
    packet.b = result4
    local result5 = buffer:readShort()
    packet.bb = result5
    local array6 = buffer:readShortArray()
    packet.bbb = array6
    local array7 = buffer:readShortArray()
    packet.bbbb = array7
    local result8 = buffer:readInt()
    packet.c = result8
    local result9 = buffer:readInt()
    packet.cc = result9
    local array10 = buffer:readIntArray()
    packet.ccc = array10
    local array11 = buffer:readIntArray()
    packet.cccc = array11
    local result12 = buffer:readLong()
    packet.d = result12
    local result13 = buffer:readLong()
    packet.dd = result13
    local array14 = buffer:readLongArray()
    packet.ddd = array14
    local array15 = buffer:readLongArray()
    packet.dddd = array15
    local result16 = buffer:readFloat()
    packet.e = result16
    local result17 = buffer:readFloat()
    packet.ee = result17
    local array18 = buffer:readFloatArray()
    packet.eee = array18
    local array19 = buffer:readFloatArray()
    packet.eeee = array19
    local result20 = buffer:readDouble()
    packet.f = result20
    local result21 = buffer:readDouble()
    packet.ff = result21
    local array22 = buffer:readDoubleArray()
    packet.fff = array22
    local array23 = buffer:readDoubleArray()
    packet.ffff = array23
    local result24 = buffer:readBoolean()
    packet.g = result24
    local result25 = buffer:readBoolean()
    packet.gg = result25
    local array26 = buffer:readBooleanArray()
    packet.ggg = array26
    local array27 = buffer:readBooleanArray()
    packet.gggg = array27
    local result28 = buffer:readString()
    packet.jj = result28
    local array29 = buffer:readStringArray()
    packet.jjj = array29
    local result30 = buffer:readPacket(102)
    packet.kk = result30
    local array31 = buffer:readPacketArray(102)
    packet.kkk = array31
    local list32 = buffer:readIntArray()
    packet.l = list32
    local result33 = {}
    local size34 = buffer:readInt()
    if size34 > 0 then
        for index35 = 1, size34 do
            local result36 = {}
            local size37 = buffer:readInt()
            if size37 > 0 then
                for index38 = 1, size37 do
                    local list39 = buffer:readIntArray()
                    table.insert(result36, list39)
                end
            end
            table.insert(result33, result36)
        end
    end
    packet.ll = result33
    local result40 = {}
    local size41 = buffer:readInt()
    if size41 > 0 then
        for index42 = 1, size41 do
            local list43 = buffer:readPacketArray(102)
            table.insert(result40, list43)
        end
    end
    packet.lll = result40
    local list44 = buffer:readStringArray()
    packet.llll = list44
    local result45 = {}
    local size46 = buffer:readInt()
    if size46 > 0 then
        for index47 = 1, size46 do
            local map48 = buffer:readIntStringMap()
            table.insert(result45, map48)
        end
    end
    packet.lllll = result45
    local map49 = buffer:readIntStringMap()
    packet.m = map49
    local map50 = buffer:readIntPacketMap(102)
    packet.mm = map50
    local result51 = {}
    local size52 = buffer:readInt()
    if size52 > 0 then
        for index53 = 1, size52 do
            local result54 = buffer:readPacket(102)
            local list55 = buffer:readIntArray()
            result51[result54] = list55
        end
    end
    packet.mmm = result51
    local result56 = {}
    local size57 = buffer:readInt()
    if size57 > 0 then
        for index58 = 1, size57 do
            local result59 = {}
            local size60 = buffer:readInt()
            if size60 > 0 then
                for index61 = 1, size60 do
                    local list62 = buffer:readPacketArray(102)
                    table.insert(result59, list62)
                end
            end
            local result63 = {}
            local size64 = buffer:readInt()
            if size64 > 0 then
                for index65 = 1, size64 do
                    local result66 = {}
                    local size67 = buffer:readInt()
                    if size67 > 0 then
                        for index68 = 1, size67 do
                            local list69 = buffer:readIntArray()
                            table.insert(result66, list69)
                        end
                    end
                    table.insert(result63, result66)
                end
            end
            result56[result59] = result63
        end
    end
    packet.mmmm = result56
    local result70 = {}
    local size71 = buffer:readInt()
    if size71 > 0 then
        for index72 = 1, size71 do
            local result73 = {}
            local size74 = buffer:readInt()
            if size74 > 0 then
                for index75 = 1, size74 do
                    local map76 = buffer:readIntStringMap()
                    table.insert(result73, map76)
                end
            end
            local result77 = {}
            local size78 = buffer:readInt()
            if size78 > 0 then
                for index79 = 1, size78 do
                    local map80 = buffer:readIntStringMap()
                    table.insert(result77, map80)
                end
            end
            result70[result73] = result77
        end
    end
    packet.mmmmm = result70
    local set81 = buffer:readIntArray()
    packet.s = set81
    local result82 = {}
    local size83 = buffer:readInt()
    if size83 > 0 then
        for index84 = 1, size83 do
            local result85 = {}
            local size86 = buffer:readInt()
            if size86 > 0 then
                for index87 = 1, size86 do
                    local list88 = buffer:readIntArray()
                    table.insert(result85, list88)
                end
            end
            table.insert(result82, result85)
        end
    end
    packet.ss = result82
    local result89 = {}
    local size90 = buffer:readInt()
    if size90 > 0 then
        for index91 = 1, size90 do
            local set92 = buffer:readPacketArray(102)
            table.insert(result89, set92)
        end
    end
    packet.sss = result89
    local set93 = buffer:readStringArray()
    packet.ssss = set93
    local result94 = {}
    local size95 = buffer:readInt()
    if size95 > 0 then
        for index96 = 1, size95 do
            local map97 = buffer:readIntStringMap()
            table.insert(result94, map97)
        end
    end
    packet.sssss = result94
    if buffer:compatibleRead(beforeReadIndex, length) then
        local result98 = buffer:readInt()
        packet.myCompatible = result98
    end
    if buffer:compatibleRead(beforeReadIndex, length) then
        local result99 = buffer:readPacket(102)
        packet.myObject = result99
    end
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return ComplexObject