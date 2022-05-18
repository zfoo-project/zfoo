-- @author jaysunxiao
-- @version 1.0
-- @since 2021-02-07 17:18

local ProtocolManager = require("LuaProtocol.ProtocolManager")

local NormalObject = {}

function NormalObject:new(a, aaa, b, bbb, c, ccc, d, ddd, e, eee, f, fff, g, ggg, h, hhh, jj, jjj, kk, kkk, l, llll, m, mm, s, ssss)
    local obj = {
        a = a, -- byte
        aaa = aaa, -- byte[]
        b = b, -- short
        bbb = bbb, -- short[]
        c = c, -- int
        ccc = ccc, -- int[]
        d = d, -- long
        ddd = ddd, -- long[]
        e = e, -- float
        eee = eee, -- float[]
        f = f, -- double
        fff = fff, -- double[]
        g = g, -- boolean
        ggg = ggg, -- boolean[]
        h = h, -- char
        hhh = hhh, -- char[]
        jj = jj, -- java.lang.String
        jjj = jjj, -- java.lang.String[]
        kk = kk, -- com.zfoo.protocol.packet.ObjectA
        kkk = kkk, -- com.zfoo.protocol.packet.ObjectA[]
        l = l, -- java.util.List<java.lang.Integer>
        llll = llll, -- java.util.List<java.lang.String>
        m = m, -- java.util.Map<java.lang.Integer, java.lang.String>
        mm = mm, -- java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
        s = s, -- java.util.Set<java.lang.Integer>
        ssss = ssss -- java.util.Set<java.lang.String>
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function NormalObject:protocolId()
    return 1161
end

function NormalObject:write(byteBuffer, packet)
    if packet == null then
        byteBuffer:writeBoolean(false)
        return
    end
    byteBuffer:writeBoolean(true)
    byteBuffer:writeByte(packet.a)
    if packet.aaa == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.aaa);
        for index0, element1 in pairs(packet.aaa) do
            byteBuffer:writeByte(element1)
        end
    end
    byteBuffer:writeShort(packet.b)
    if packet.bbb == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.bbb);
        for index2, element3 in pairs(packet.bbb) do
            byteBuffer:writeShort(element3)
        end
    end
    byteBuffer:writeInt(packet.c)
    if packet.ccc == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ccc);
        for index4, element5 in pairs(packet.ccc) do
            byteBuffer:writeInt(element5)
        end
    end
    byteBuffer:writeLong(packet.d)
    if packet.ddd == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ddd);
        for index6, element7 in pairs(packet.ddd) do
            byteBuffer:writeLong(element7)
        end
    end
    byteBuffer:writeFloat(packet.e)
    if packet.eee == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.eee);
        for index8, element9 in pairs(packet.eee) do
            byteBuffer:writeFloat(element9)
        end
    end
    byteBuffer:writeDouble(packet.f)
    if packet.fff == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.fff);
        for index10, element11 in pairs(packet.fff) do
            byteBuffer:writeDouble(element11)
        end
    end
    byteBuffer:writeBoolean(packet.g)
    if packet.ggg == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ggg);
        for index12, element13 in pairs(packet.ggg) do
            byteBuffer:writeBoolean(element13)
        end
    end
    byteBuffer:writeChar(packet.h)
    if packet.hhh == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.hhh);
        for index14, element15 in pairs(packet.hhh) do
            byteBuffer:writeChar(element15)
        end
    end
    byteBuffer:writeString(packet.jj)
    if packet.jjj == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.jjj);
        for index16, element17 in pairs(packet.jjj) do
            byteBuffer:writeString(element17)
        end
    end
    ProtocolManager.getProtocol(1116):write(byteBuffer, packet.kk)
    if packet.kkk == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.kkk);
        for index18, element19 in pairs(packet.kkk) do
            ProtocolManager.getProtocol(1116):write(byteBuffer, element19)
        end
    end
    if packet.l == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.l)
        for index20, element21 in pairs(packet.l) do
            byteBuffer:writeInt(element21)
        end
    end
    if packet.llll == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.llll)
        for index22, element23 in pairs(packet.llll) do
            byteBuffer:writeString(element23)
        end
    end
    if packet.m == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.m))
        for key24, value25 in pairs(packet.m) do
            byteBuffer:writeInt(key24)
            byteBuffer:writeString(value25)
        end
    end
    if packet.mm == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.mm))
        for key26, value27 in pairs(packet.mm) do
            byteBuffer:writeInt(key26)
            ProtocolManager.getProtocol(1116):write(byteBuffer, value27)
        end
    end
    if packet.s == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.s))
        for index28, element29 in pairs(packet.s) do
            byteBuffer:writeInt(element29)
        end
    end
    if packet.ssss == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.ssss))
        for index30, element31 in pairs(packet.ssss) do
            byteBuffer:writeString(element31)
        end
    end
end

function NormalObject:read(byteBuffer)
    if not(byteBuffer:readBoolean()) then
        return nil
    end
    local packet = NormalObject:new()
    local result32 = byteBuffer:readByte()
    packet.a = result32
    local result33 = {}
    local size35 = byteBuffer:readInt()
    if size35 > 0 then
        for index34 = 1, size35 do
            local result36 = byteBuffer:readByte()
            table.insert(result33, result36)
        end
    end
    packet.aaa = result33
    local result37 = byteBuffer:readShort()
    packet.b = result37
    local result38 = {}
    local size40 = byteBuffer:readInt()
    if size40 > 0 then
        for index39 = 1, size40 do
            local result41 = byteBuffer:readShort()
            table.insert(result38, result41)
        end
    end
    packet.bbb = result38
    local result42 = byteBuffer:readInt()
    packet.c = result42
    local result43 = {}
    local size45 = byteBuffer:readInt()
    if size45 > 0 then
        for index44 = 1, size45 do
            local result46 = byteBuffer:readInt()
            table.insert(result43, result46)
        end
    end
    packet.ccc = result43
    local result47 = byteBuffer:readLong()
    packet.d = result47
    local result48 = {}
    local size50 = byteBuffer:readInt()
    if size50 > 0 then
        for index49 = 1, size50 do
            local result51 = byteBuffer:readLong()
            table.insert(result48, result51)
        end
    end
    packet.ddd = result48
    local result52 = byteBuffer:readFloat()
    packet.e = result52
    local result53 = {}
    local size55 = byteBuffer:readInt()
    if size55 > 0 then
        for index54 = 1, size55 do
            local result56 = byteBuffer:readFloat()
            table.insert(result53, result56)
        end
    end
    packet.eee = result53
    local result57 = byteBuffer:readDouble()
    packet.f = result57
    local result58 = {}
    local size60 = byteBuffer:readInt()
    if size60 > 0 then
        for index59 = 1, size60 do
            local result61 = byteBuffer:readDouble()
            table.insert(result58, result61)
        end
    end
    packet.fff = result58
    local result62 = byteBuffer:readBoolean()
    packet.g = result62
    local result63 = {}
    local size65 = byteBuffer:readInt()
    if size65 > 0 then
        for index64 = 1, size65 do
            local result66 = byteBuffer:readBoolean()
            table.insert(result63, result66)
        end
    end
    packet.ggg = result63
    local result67 = byteBuffer:readChar()
    packet.h = result67
    local result68 = {}
    local size70 = byteBuffer:readInt()
    if size70 > 0 then
        for index69 = 1, size70 do
            local result71 = byteBuffer:readChar()
            table.insert(result68, result71)
        end
    end
    packet.hhh = result68
    local result72 = byteBuffer:readString()
    packet.jj = result72
    local result73 = {}
    local size75 = byteBuffer:readInt()
    if size75 > 0 then
        for index74 = 1, size75 do
            local result76 = byteBuffer:readString()
            table.insert(result73, result76)
        end
    end
    packet.jjj = result73
    local result77 = ProtocolManager.getProtocol(1116):read(byteBuffer)
    packet.kk = result77
    local result78 = {}
    local size80 = byteBuffer:readInt()
    if size80 > 0 then
        for index79 = 1, size80 do
            local result81 = ProtocolManager.getProtocol(1116):read(byteBuffer)
            table.insert(result78, result81)
        end
    end
    packet.kkk = result78
    local result82 = {}
    local size83 = byteBuffer:readInt()
    if size83 > 0 then
        for index84 = 1, size83 do
            local result85 = byteBuffer:readInt()
            table.insert(result82, result85)
        end
    end
    packet.l = result82
    local result86 = {}
    local size87 = byteBuffer:readInt()
    if size87 > 0 then
        for index88 = 1, size87 do
            local result89 = byteBuffer:readString()
            table.insert(result86, result89)
        end
    end
    packet.llll = result86
    local result90 = {}
    local size91 = byteBuffer:readInt()
    if size91 > 0 then
        for index92 = 1, size91 do
            local result93 = byteBuffer:readInt()
            local result94 = byteBuffer:readString()
            result90[result93] = result94
        end
    end
    packet.m = result90
    local result95 = {}
    local size96 = byteBuffer:readInt()
    if size96 > 0 then
        for index97 = 1, size96 do
            local result98 = byteBuffer:readInt()
            local result99 = ProtocolManager.getProtocol(1116):read(byteBuffer)
            result95[result98] = result99
        end
    end
    packet.mm = result95
    local result100 = {}
    local size101 = byteBuffer:readInt()
    if size101 > 0 then
        for index102 = 1, size101 do
            local result103 = byteBuffer:readInt()
            result100[result103] = result103
        end
    end
    packet.s = result100
    local result104 = {}
    local size105 = byteBuffer:readInt()
    if size105 > 0 then
        for index106 = 1, size105 do
            local result107 = byteBuffer:readString()
            result104[result107] = result107
        end
    end
    packet.ssss = result104
    return packet
end

return NormalObject
