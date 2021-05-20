-- 复杂的对象
-- 包括了各种复杂的结构，数组，List，Set，Map
--
-- @author jaysunxiao
-- @version 1.0
-- @since 2017 10.14 11:19

local ProtocolManager = require("LuaProtocol.ProtocolManager")

local ComplexObject = {}

function ComplexObject:new(a, aa, aaa, aaaa, b, bb, bbb, bbbb, c, cc, ccc, cccc, d, dd, ddd, dddd, e, ee, eee, eeee, f, ff, fff, ffff, g, gg, ggg, gggg, h, hh, hhh, hhhh, jj, jjj, kk, kkk, l, ll, lll, llll, lllll, m, mm, mmm, mmmm, mmmmm, s, ss, sss, ssss, sssss)
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
        sssss = sssss -- java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ComplexObject:protocolId()
    return 1160
end

function ComplexObject:write(byteBuffer, packet)
    if packet == null then
        byteBuffer:writeBoolean(false)
        return
    end
    byteBuffer:writeBoolean(true)
    byteBuffer:writeByte(packet.a)
    byteBuffer:writeByte(packet.aa)
    if packet.aaa == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.aaa);
        for index0, element1 in pairs(packet.aaa) do
            byteBuffer:writeByte(element1)
        end
    end
    if packet.aaaa == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.aaaa);
        for index2, element3 in pairs(packet.aaaa) do
            byteBuffer:writeByte(element3)
        end
    end
    byteBuffer:writeShort(packet.b)
    byteBuffer:writeShort(packet.bb)
    if packet.bbb == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.bbb);
        for index4, element5 in pairs(packet.bbb) do
            byteBuffer:writeShort(element5)
        end
    end
    if packet.bbbb == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.bbbb);
        for index6, element7 in pairs(packet.bbbb) do
            byteBuffer:writeShort(element7)
        end
    end
    byteBuffer:writeInt(packet.c)
    byteBuffer:writeInt(packet.cc)
    if packet.ccc == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ccc);
        for index8, element9 in pairs(packet.ccc) do
            byteBuffer:writeInt(element9)
        end
    end
    if packet.cccc == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.cccc);
        for index10, element11 in pairs(packet.cccc) do
            byteBuffer:writeInt(element11)
        end
    end
    byteBuffer:writeLong(packet.d)
    byteBuffer:writeLong(packet.dd)
    if packet.ddd == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ddd);
        for index12, element13 in pairs(packet.ddd) do
            byteBuffer:writeLong(element13)
        end
    end
    if packet.dddd == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.dddd);
        for index14, element15 in pairs(packet.dddd) do
            byteBuffer:writeLong(element15)
        end
    end
    byteBuffer:writeFloat(packet.e)
    byteBuffer:writeFloat(packet.ee)
    if packet.eee == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.eee);
        for index16, element17 in pairs(packet.eee) do
            byteBuffer:writeFloat(element17)
        end
    end
    if packet.eeee == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.eeee);
        for index18, element19 in pairs(packet.eeee) do
            byteBuffer:writeFloat(element19)
        end
    end
    byteBuffer:writeDouble(packet.f)
    byteBuffer:writeDouble(packet.ff)
    if packet.fff == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.fff);
        for index20, element21 in pairs(packet.fff) do
            byteBuffer:writeDouble(element21)
        end
    end
    if packet.ffff == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ffff);
        for index22, element23 in pairs(packet.ffff) do
            byteBuffer:writeDouble(element23)
        end
    end
    byteBuffer:writeBoolean(packet.g)
    byteBuffer:writeBoolean(packet.gg)
    if packet.ggg == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ggg);
        for index24, element25 in pairs(packet.ggg) do
            byteBuffer:writeBoolean(element25)
        end
    end
    if packet.gggg == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.gggg);
        for index26, element27 in pairs(packet.gggg) do
            byteBuffer:writeBoolean(element27)
        end
    end
    byteBuffer:writeChar(packet.h)
    byteBuffer:writeChar(packet.hh)
    if packet.hhh == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.hhh);
        for index28, element29 in pairs(packet.hhh) do
            byteBuffer:writeChar(element29)
        end
    end
    if packet.hhhh == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.hhhh);
        for index30, element31 in pairs(packet.hhhh) do
            byteBuffer:writeChar(element31)
        end
    end
    byteBuffer:writeString(packet.jj)
    if packet.jjj == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.jjj);
        for index32, element33 in pairs(packet.jjj) do
            byteBuffer:writeString(element33)
        end
    end
    ProtocolManager.getProtocol(1116):write(byteBuffer, packet.kk)
    if packet.kkk == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.kkk);
        for index34, element35 in pairs(packet.kkk) do
            ProtocolManager.getProtocol(1116):write(byteBuffer, element35)
        end
    end
    if packet.l == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.l)
        for index36, element37 in pairs(packet.l) do
            byteBuffer:writeInt(element37)
        end
    end
    if packet.ll == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.ll)
        for index38, element39 in pairs(packet.ll) do
            if element39 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(#element39)
                for index40, element41 in pairs(element39) do
                    if element41 == null then
                        byteBuffer:writeInt(0)
                    else
                        byteBuffer:writeInt(#element41)
                        for index42, element43 in pairs(element41) do
                            byteBuffer:writeInt(element43)
                        end
                    end
                end
            end
        end
    end
    if packet.lll == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.lll)
        for index44, element45 in pairs(packet.lll) do
            if element45 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(#element45)
                for index46, element47 in pairs(element45) do
                    ProtocolManager.getProtocol(1116):write(byteBuffer, element47)
                end
            end
        end
    end
    if packet.llll == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.llll)
        for index48, element49 in pairs(packet.llll) do
            byteBuffer:writeString(element49)
        end
    end
    if packet.lllll == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(#packet.lllll)
        for index50, element51 in pairs(packet.lllll) do
            if element51 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(table.mapSize(element51))
                for key52, value53 in pairs(element51) do
                    byteBuffer:writeInt(key52)
                    byteBuffer:writeString(value53)
                end
            end
        end
    end
    if packet.m == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.m))
        for key54, value55 in pairs(packet.m) do
            byteBuffer:writeInt(key54)
            byteBuffer:writeString(value55)
        end
    end
    if packet.mm == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.mm))
        for key56, value57 in pairs(packet.mm) do
            byteBuffer:writeInt(key56)
            ProtocolManager.getProtocol(1116):write(byteBuffer, value57)
        end
    end
    if packet.mmm == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.mmm))
        for key58, value59 in pairs(packet.mmm) do
            ProtocolManager.getProtocol(1116):write(byteBuffer, key58)
            if value59 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(#value59)
                for index60, element61 in pairs(value59) do
                    byteBuffer:writeInt(element61)
                end
            end
        end
    end
    if packet.mmmm == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.mmmm))
        for key62, value63 in pairs(packet.mmmm) do
            if key62 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(#key62)
                for index64, element65 in pairs(key62) do
                    if element65 == null then
                        byteBuffer:writeInt(0)
                    else
                        byteBuffer:writeInt(#element65)
                        for index66, element67 in pairs(element65) do
                            ProtocolManager.getProtocol(1116):write(byteBuffer, element67)
                        end
                    end
                end
            end
            if value63 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(#value63)
                for index68, element69 in pairs(value63) do
                    if element69 == null then
                        byteBuffer:writeInt(0)
                    else
                        byteBuffer:writeInt(#element69)
                        for index70, element71 in pairs(element69) do
                            if element71 == null then
                                byteBuffer:writeInt(0)
                            else
                                byteBuffer:writeInt(#element71)
                                for index72, element73 in pairs(element71) do
                                    byteBuffer:writeInt(element73)
                                end
                            end
                        end
                    end
                end
            end
        end
    end
    if packet.mmmmm == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.mmmmm))
        for key74, value75 in pairs(packet.mmmmm) do
            if key74 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(#key74)
                for index76, element77 in pairs(key74) do
                    if element77 == null then
                        byteBuffer:writeInt(0)
                    else
                        byteBuffer:writeInt(table.mapSize(element77))
                        for key78, value79 in pairs(element77) do
                            byteBuffer:writeInt(key78)
                            byteBuffer:writeString(value79)
                        end
                    end
                end
            end
            if value75 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(table.setSize(value75))
                for index80, element81 in pairs(value75) do
                    if element81 == null then
                        byteBuffer:writeInt(0)
                    else
                        byteBuffer:writeInt(table.mapSize(element81))
                        for key82, value83 in pairs(element81) do
                            byteBuffer:writeInt(key82)
                            byteBuffer:writeString(value83)
                        end
                    end
                end
            end
        end
    end
    if packet.s == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.s))
        for index84, element85 in pairs(packet.s) do
            byteBuffer:writeInt(element85)
        end
    end
    if packet.ss == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.ss))
        for index86, element87 in pairs(packet.ss) do
            if element87 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(table.setSize(element87))
                for index88, element89 in pairs(element87) do
                    if element89 == null then
                        byteBuffer:writeInt(0)
                    else
                        byteBuffer:writeInt(#element89)
                        for index90, element91 in pairs(element89) do
                            byteBuffer:writeInt(element91)
                        end
                    end
                end
            end
        end
    end
    if packet.sss == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.sss))
        for index92, element93 in pairs(packet.sss) do
            if element93 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(table.setSize(element93))
                for index94, element95 in pairs(element93) do
                    ProtocolManager.getProtocol(1116):write(byteBuffer, element95)
                end
            end
        end
    end
    if packet.ssss == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.ssss))
        for index96, element97 in pairs(packet.ssss) do
            byteBuffer:writeString(element97)
        end
    end
    if packet.sssss == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.setSize(packet.sssss))
        for index98, element99 in pairs(packet.sssss) do
            if element99 == null then
                byteBuffer:writeInt(0)
            else
                byteBuffer:writeInt(table.mapSize(element99))
                for key100, value101 in pairs(element99) do
                    byteBuffer:writeInt(key100)
                    byteBuffer:writeString(value101)
                end
            end
        end
    end
end

function ComplexObject:read(byteBuffer)
    if not(byteBuffer:readBoolean()) then
        return nil
    end
    local packet = ComplexObject:new()
    local result102 = byteBuffer:readByte()
    packet.a = result102
    local result103 = byteBuffer:readByte()
    packet.aa = result103
    local result104 = {}
    local size106 = byteBuffer:readInt()
    if size106 > 0 then
        for index105 = 1, size106 do
            local result107 = byteBuffer:readByte()
            table.insert(result104, result107)
        end
    end
    packet.aaa = result104
    local result108 = {}
    local size110 = byteBuffer:readInt()
    if size110 > 0 then
        for index109 = 1, size110 do
            local result111 = byteBuffer:readByte()
            table.insert(result108, result111)
        end
    end
    packet.aaaa = result108
    local result112 = byteBuffer:readShort()
    packet.b = result112
    local result113 = byteBuffer:readShort()
    packet.bb = result113
    local result114 = {}
    local size116 = byteBuffer:readInt()
    if size116 > 0 then
        for index115 = 1, size116 do
            local result117 = byteBuffer:readShort()
            table.insert(result114, result117)
        end
    end
    packet.bbb = result114
    local result118 = {}
    local size120 = byteBuffer:readInt()
    if size120 > 0 then
        for index119 = 1, size120 do
            local result121 = byteBuffer:readShort()
            table.insert(result118, result121)
        end
    end
    packet.bbbb = result118
    local result122 = byteBuffer:readInt()
    packet.c = result122
    local result123 = byteBuffer:readInt()
    packet.cc = result123
    local result124 = {}
    local size126 = byteBuffer:readInt()
    if size126 > 0 then
        for index125 = 1, size126 do
            local result127 = byteBuffer:readInt()
            table.insert(result124, result127)
        end
    end
    packet.ccc = result124
    local result128 = {}
    local size130 = byteBuffer:readInt()
    if size130 > 0 then
        for index129 = 1, size130 do
            local result131 = byteBuffer:readInt()
            table.insert(result128, result131)
        end
    end
    packet.cccc = result128
    local result132 = byteBuffer:readLong()
    packet.d = result132
    local result133 = byteBuffer:readLong()
    packet.dd = result133
    local result134 = {}
    local size136 = byteBuffer:readInt()
    if size136 > 0 then
        for index135 = 1, size136 do
            local result137 = byteBuffer:readLong()
            table.insert(result134, result137)
        end
    end
    packet.ddd = result134
    local result138 = {}
    local size140 = byteBuffer:readInt()
    if size140 > 0 then
        for index139 = 1, size140 do
            local result141 = byteBuffer:readLong()
            table.insert(result138, result141)
        end
    end
    packet.dddd = result138
    local result142 = byteBuffer:readFloat()
    packet.e = result142
    local result143 = byteBuffer:readFloat()
    packet.ee = result143
    local result144 = {}
    local size146 = byteBuffer:readInt()
    if size146 > 0 then
        for index145 = 1, size146 do
            local result147 = byteBuffer:readFloat()
            table.insert(result144, result147)
        end
    end
    packet.eee = result144
    local result148 = {}
    local size150 = byteBuffer:readInt()
    if size150 > 0 then
        for index149 = 1, size150 do
            local result151 = byteBuffer:readFloat()
            table.insert(result148, result151)
        end
    end
    packet.eeee = result148
    local result152 = byteBuffer:readDouble()
    packet.f = result152
    local result153 = byteBuffer:readDouble()
    packet.ff = result153
    local result154 = {}
    local size156 = byteBuffer:readInt()
    if size156 > 0 then
        for index155 = 1, size156 do
            local result157 = byteBuffer:readDouble()
            table.insert(result154, result157)
        end
    end
    packet.fff = result154
    local result158 = {}
    local size160 = byteBuffer:readInt()
    if size160 > 0 then
        for index159 = 1, size160 do
            local result161 = byteBuffer:readDouble()
            table.insert(result158, result161)
        end
    end
    packet.ffff = result158
    local result162 = byteBuffer:readBoolean()
    packet.g = result162
    local result163 = byteBuffer:readBoolean()
    packet.gg = result163
    local result164 = {}
    local size166 = byteBuffer:readInt()
    if size166 > 0 then
        for index165 = 1, size166 do
            local result167 = byteBuffer:readBoolean()
            table.insert(result164, result167)
        end
    end
    packet.ggg = result164
    local result168 = {}
    local size170 = byteBuffer:readInt()
    if size170 > 0 then
        for index169 = 1, size170 do
            local result171 = byteBuffer:readBoolean()
            table.insert(result168, result171)
        end
    end
    packet.gggg = result168
    local result172 = byteBuffer:readChar()
    packet.h = result172
    local result173 = byteBuffer:readChar()
    packet.hh = result173
    local result174 = {}
    local size176 = byteBuffer:readInt()
    if size176 > 0 then
        for index175 = 1, size176 do
            local result177 = byteBuffer:readChar()
            table.insert(result174, result177)
        end
    end
    packet.hhh = result174
    local result178 = {}
    local size180 = byteBuffer:readInt()
    if size180 > 0 then
        for index179 = 1, size180 do
            local result181 = byteBuffer:readChar()
            table.insert(result178, result181)
        end
    end
    packet.hhhh = result178
    local result182 = byteBuffer:readString()
    packet.jj = result182
    local result183 = {}
    local size185 = byteBuffer:readInt()
    if size185 > 0 then
        for index184 = 1, size185 do
            local result186 = byteBuffer:readString()
            table.insert(result183, result186)
        end
    end
    packet.jjj = result183
    local result187 = ProtocolManager.getProtocol(1116):read(byteBuffer)
    packet.kk = result187
    local result188 = {}
    local size190 = byteBuffer:readInt()
    if size190 > 0 then
        for index189 = 1, size190 do
            local result191 = ProtocolManager.getProtocol(1116):read(byteBuffer)
            table.insert(result188, result191)
        end
    end
    packet.kkk = result188
    local result192 = {}
    local size193 = byteBuffer:readInt()
    if size193 > 0 then
        for index194 = 1, size193 do
            local result195 = byteBuffer:readInt()
            table.insert(result192, result195)
        end
    end
    packet.l = result192
    local result196 = {}
    local size197 = byteBuffer:readInt()
    if size197 > 0 then
        for index198 = 1, size197 do
            local result199 = {}
            local size200 = byteBuffer:readInt()
            if size200 > 0 then
                for index201 = 1, size200 do
                    local result202 = {}
                    local size203 = byteBuffer:readInt()
                    if size203 > 0 then
                        for index204 = 1, size203 do
                            local result205 = byteBuffer:readInt()
                            table.insert(result202, result205)
                        end
                    end
                    table.insert(result199, result202)
                end
            end
            table.insert(result196, result199)
        end
    end
    packet.ll = result196
    local result206 = {}
    local size207 = byteBuffer:readInt()
    if size207 > 0 then
        for index208 = 1, size207 do
            local result209 = {}
            local size210 = byteBuffer:readInt()
            if size210 > 0 then
                for index211 = 1, size210 do
                    local result212 = ProtocolManager.getProtocol(1116):read(byteBuffer)
                    table.insert(result209, result212)
                end
            end
            table.insert(result206, result209)
        end
    end
    packet.lll = result206
    local result213 = {}
    local size214 = byteBuffer:readInt()
    if size214 > 0 then
        for index215 = 1, size214 do
            local result216 = byteBuffer:readString()
            table.insert(result213, result216)
        end
    end
    packet.llll = result213
    local result217 = {}
    local size218 = byteBuffer:readInt()
    if size218 > 0 then
        for index219 = 1, size218 do
            local result220 = {}
            local size221 = byteBuffer:readInt()
            if size221 > 0 then
                for index222 = 1, size221 do
                    local result223 = byteBuffer:readInt()
                    local result224 = byteBuffer:readString()
                    result220[result223] = result224
                end
            end
            table.insert(result217, result220)
        end
    end
    packet.lllll = result217
    local result225 = {}
    local size226 = byteBuffer:readInt()
    if size226 > 0 then
        for index227 = 1, size226 do
            local result228 = byteBuffer:readInt()
            local result229 = byteBuffer:readString()
            result225[result228] = result229
        end
    end
    packet.m = result225
    local result230 = {}
    local size231 = byteBuffer:readInt()
    if size231 > 0 then
        for index232 = 1, size231 do
            local result233 = byteBuffer:readInt()
            local result234 = ProtocolManager.getProtocol(1116):read(byteBuffer)
            result230[result233] = result234
        end
    end
    packet.mm = result230
    local result235 = {}
    local size236 = byteBuffer:readInt()
    if size236 > 0 then
        for index237 = 1, size236 do
            local result238 = ProtocolManager.getProtocol(1116):read(byteBuffer)
            local result239 = {}
            local size240 = byteBuffer:readInt()
            if size240 > 0 then
                for index241 = 1, size240 do
                    local result242 = byteBuffer:readInt()
                    table.insert(result239, result242)
                end
            end
            result235[result238] = result239
        end
    end
    packet.mmm = result235
    local result243 = {}
    local size244 = byteBuffer:readInt()
    if size244 > 0 then
        for index245 = 1, size244 do
            local result246 = {}
            local size247 = byteBuffer:readInt()
            if size247 > 0 then
                for index248 = 1, size247 do
                    local result249 = {}
                    local size250 = byteBuffer:readInt()
                    if size250 > 0 then
                        for index251 = 1, size250 do
                            local result252 = ProtocolManager.getProtocol(1116):read(byteBuffer)
                            table.insert(result249, result252)
                        end
                    end
                    table.insert(result246, result249)
                end
            end
            local result253 = {}
            local size254 = byteBuffer:readInt()
            if size254 > 0 then
                for index255 = 1, size254 do
                    local result256 = {}
                    local size257 = byteBuffer:readInt()
                    if size257 > 0 then
                        for index258 = 1, size257 do
                            local result259 = {}
                            local size260 = byteBuffer:readInt()
                            if size260 > 0 then
                                for index261 = 1, size260 do
                                    local result262 = byteBuffer:readInt()
                                    table.insert(result259, result262)
                                end
                            end
                            table.insert(result256, result259)
                        end
                    end
                    table.insert(result253, result256)
                end
            end
            result243[result246] = result253
        end
    end
    packet.mmmm = result243
    local result263 = {}
    local size264 = byteBuffer:readInt()
    if size264 > 0 then
        for index265 = 1, size264 do
            local result266 = {}
            local size267 = byteBuffer:readInt()
            if size267 > 0 then
                for index268 = 1, size267 do
                    local result269 = {}
                    local size270 = byteBuffer:readInt()
                    if size270 > 0 then
                        for index271 = 1, size270 do
                            local result272 = byteBuffer:readInt()
                            local result273 = byteBuffer:readString()
                            result269[result272] = result273
                        end
                    end
                    table.insert(result266, result269)
                end
            end
            local result274 = {}
            local size275 = byteBuffer:readInt()
            if size275 > 0 then
                for index276 = 1, size275 do
                    local result277 = {}
                    local size278 = byteBuffer:readInt()
                    if size278 > 0 then
                        for index279 = 1, size278 do
                            local result280 = byteBuffer:readInt()
                            local result281 = byteBuffer:readString()
                            result277[result280] = result281
                        end
                    end
                    result274[result277] = result277
                end
            end
            result263[result266] = result274
        end
    end
    packet.mmmmm = result263
    local result282 = {}
    local size283 = byteBuffer:readInt()
    if size283 > 0 then
        for index284 = 1, size283 do
            local result285 = byteBuffer:readInt()
            result282[result285] = result285
        end
    end
    packet.s = result282
    local result286 = {}
    local size287 = byteBuffer:readInt()
    if size287 > 0 then
        for index288 = 1, size287 do
            local result289 = {}
            local size290 = byteBuffer:readInt()
            if size290 > 0 then
                for index291 = 1, size290 do
                    local result292 = {}
                    local size293 = byteBuffer:readInt()
                    if size293 > 0 then
                        for index294 = 1, size293 do
                            local result295 = byteBuffer:readInt()
                            table.insert(result292, result295)
                        end
                    end
                    result289[result292] = result292
                end
            end
            result286[result289] = result289
        end
    end
    packet.ss = result286
    local result296 = {}
    local size297 = byteBuffer:readInt()
    if size297 > 0 then
        for index298 = 1, size297 do
            local result299 = {}
            local size300 = byteBuffer:readInt()
            if size300 > 0 then
                for index301 = 1, size300 do
                    local result302 = ProtocolManager.getProtocol(1116):read(byteBuffer)
                    result299[result302] = result302
                end
            end
            result296[result299] = result299
        end
    end
    packet.sss = result296
    local result303 = {}
    local size304 = byteBuffer:readInt()
    if size304 > 0 then
        for index305 = 1, size304 do
            local result306 = byteBuffer:readString()
            result303[result306] = result306
        end
    end
    packet.ssss = result303
    local result307 = {}
    local size308 = byteBuffer:readInt()
    if size308 > 0 then
        for index309 = 1, size308 do
            local result310 = {}
            local size311 = byteBuffer:readInt()
            if size311 > 0 then
                for index312 = 1, size311 do
                    local result313 = byteBuffer:readInt()
                    local result314 = byteBuffer:readString()
                    result310[result313] = result314
                end
            end
            result307[result310] = result310
        end
    end
    packet.sssss = result307
    return packet
end

return ComplexObject
