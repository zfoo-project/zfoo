local MAX_LONG_4BYTE = 1 << 32
local MIN_INT = -2147483648
local MAX_INT = 2147483647
local MIN_LONG = 0x8000000000000000
local MAX_LONG = 0x7fffffffffffffff
local MIN_LONG_STRING = "-9223372036854775808"
--The natural logarithm of 2.
local LN2 = 0.6931471805599453

Long = {}

function Long:new(low, high)
    local obj = {
        low = low & 0xFFFFFFFF,
        high = high & 0xFFFFFFFF
    }

    setmetatable(obj, self)
    self.__index = self
    return obj
end

local function clone(value)
    return Long:new(value.low, value.high)
end

local function fromBits(lowBits, highBits)
    return Long:new(lowBits, highBits)
end

local function fromInt(value)
    value = math.tointeger(value)
    local param = 0
    if value < 0 then
        param = -1
    end
    return fromBits(value, param)
end

local ZERO = fromInt(0)
local ONE = fromInt(1)
local NEG_ONE = fromInt(-1)
local MAX_VALUE = fromBits(0xFFFFFFFF, 0x7FFFFFFF)
local MIN_VALUE = fromBits(0, 0x80000000)

local function fromNumber(value)
    if (value <= -MIN_LONG) then
        return clone(MIN_VALUE)

    end

    if (value + 1 >= MAX_LONG) then
        return clone(MAX_VALUE)
    end

    if (value < 0) then
        return fromNumber(-value):negate()
    end
    return fromBits(math.floor((value % MAX_LONG_4BYTE)) | 0, math.floor(value / MAX_LONG_4BYTE) | 0)
end

function Long:fromString(str, radix)
    if type(radix) == "nil" then
        radix = 10
    end

    if (type(str) ~= "string") then
        error("str不是string类型参数")
    end

    --进制必须在2到36
    if radix < 2 or 36 < radix then
        error("range radix error")
    end

    local p = string.find(str, "-")
    if p ~= nil then
        if (p > 1) then
            error("interior hyphen")
        end

        if (p == 1) then
            return Long:fromString(string.sub(str, 2), radix):negate()
        end
    end

    local radixToPower = fromNumber(radix ^ 8)
    local result = clone(ZERO)
    str = tostring(str)
    for i = 1, #str, 8 do
        local size = math.min(8, #str - i + 1)
        if (size < 8) then
            local value = tonumber(string.sub(str, i), radix)
            local power = fromNumber(radix ^ size)
            result = result:multiply(power):add(fromNumber(value))
        else
            local value = tonumber(string.sub(str, i, i + 7), radix)
            result = result:multiply(radixToPower):add(fromNumber(value))
        end
    end
    return result
end

--转为10进制的string符号的long
function Long:toString()
    local radix = 10
    if (Long:isZero()) then
        return "0"
    end

    if (self:isNegative()) then
        if (self:equals(MIN_VALUE)) then
            return MIN_LONG_STRING
        else
            return '-' .. self:negate():toString(radix)
        end
    end

    local radixToPower = fromNumber(radix ^ 6)
    local rem = self
    local result = ''
    while (true) do
        local remDiv = rem:divide(radixToPower)
        local digits = tostring(rem:subtract(remDiv:multiply(radixToPower)):toInt() & 0xFFFFFFFF)
        rem = remDiv
        if (rem:isZero()) then
            return digits .. result
        else
            while (#digits < 6) do
                digits = '0' .. digits
            end
            result = '' .. digits .. result
        end
    end
end

--Converts the Long to a the nearest floating-point representation of this value (double, 53 bit mantissa).
function Long:toNumber()
    return self.high * MAX_LONG_4BYTE + self.low
end

--Converts the Long to a 32 bit integer, assuming it is a 32 bit integer.
function Long:toInt()
    return self.low
end

function Long:isNegative()
    return (self.high & 0x80000000) ~= 0
end

function Long:negate()
    if self:equals(MIN_VALUE) then
        return clone(MIN_VALUE)
    end
    --正数转为负数的二进制编码，取反加1
    local notSelf = fromBits(~self.low, ~self.high)
    return notSelf:add(ONE)
end

function Long:equals(other)
    return self.high == other.high and self.low == other.low
end

function Long:isZero()
    return self.high == 0 and self.low == 0
end

function Long:add(addend)
    local a48 = (self.high >> 16)
    local a32 = (self.high & 0xFFFF)
    local a16 = (self.low >> 16)
    local a00 = (self.low & 0xFFFF)

    local b48 = (addend.high >> 16)
    local b32 = (addend.high & 0xFFFF)
    local b16 = (addend.low >> 16)
    local b00 = (addend.low & 0xFFFF)

    local c48 = 0
    local c32 = 0
    local c16 = 0
    local c00 = 0
    c00 = c00 + a00 + b00
    c16 = c16 + (c00 >> 16)
    c00 = (c00 & 0xFFFF)
    c16 = c16 + a16 + b16
    c32 = c32 + (c16 >> 16)
    c16 = (c16 & 0xFFFF)
    c32 = c32 + a32 + b32
    c48 = c48 + (c32 >> 16)
    c32 = (c32 & 0xFFFF)
    c48 = c48 + a48 + b48
    c48 = (c48 & 0xFFFF)
    return fromBits((c16 << 16) | c00, (c48 << 16) | c32)
end

function Long:subtract(subtrahend)
    return self:add(subtrahend:negate())
end

function Long:multiply(multiplier)
    if (self:isZero()) then
        return clone(ZERO)
    end

    if (multiplier:isZero()) then
        return clone(ZERO)
    end

    local a48 = (self.high >> 16)
    local a32 = (self.high & 0xFFFF)
    local a16 = (self.low >> 16)
    local a00 = (self.low & 0xFFFF)

    local b48 = (multiplier.high >> 16)
    local b32 = (multiplier.high & 0xFFFF)
    local b16 = (multiplier.low >> 16)
    local b00 = (multiplier.low & 0xFFFF)

    local c48 = 0
    local c32 = 0
    local c16 = 0
    local c00 = 0
    c00 = c00 + a00 * b00
    c16 = c16 + (c00 >> 16)
    c00 = c00 & 0xFFFF
    c16 = c16 + a16 * b00
    c32 = c32 + (c16 >> 16)
    c16 = c16 & 0xFFFF
    c16 = c16 + a00 * b16
    c32 = c32 + (c16 >> 16)
    c16 = c16 & 0xFFFF
    c32 = c32 + a32 * b00
    c48 = c48 + (c32 >> 16)
    c32 = c32 & 0xFFFF
    c32 = c32 + a16 * b16
    c48 = c48 + (c32 >> 16)
    c32 = c32 & 0xFFFF
    c32 = c32 + a00 * b32
    c48 = c48 + (c32 >> 16)
    c32 = c32 & 0xFFFF
    c48 = c48 + a48 * b00 + a32 * b16 + a16 * b32 + a00 * b48
    c48 = c48 & 0xFFFF
    return fromBits((c16 << 16) | c00, (c48 << 16) | c32)
end

function Long:divide(divisor)
    if (divisor:isZero()) then
        error('division by zero')
    end

    if (self:isZero()) then
        return clone(ZERO)
    end

    local approx
    local rem
    local res
    if (self:equals(MIN_VALUE)) then
        if (divisor:equals(ONE) or divisor:equals(NEG_ONE)) then
            return clone(MIN_VALUE)
        elseif (divisor:equals(MIN_VALUE)) then
            return clone(ONE)
        else
            local halfThis = self:shiftRight(1)
            approx = halfThis:divide(divisor):shiftLeft(1)
            if (approx:equals(ZERO)) then
                if (divisor:isNegative()) then
                    return clone(ONE)
                else
                    return clone(NEG_ONE)
                end
            else
                rem = self:subtract(divisor:multiply(approx))
                res = approx:add(rem:divide(divisor))
                return res
            end
        end
    elseif (divisor:equals(MIN_VALUE)) then
        return clone(ZERO)
    end
    if (self:isNegative()) then
        if (divisor:isNegative()) then
            return self:neg():divide(divisor:negate())
        end
        return self:negate():divide(divisor):negate()
    elseif (divisor:isNegative()) then
        return self:divide(divisor:negate()):negate()
    end
    res = clone(ZERO)

    rem = self
    while (rem:greaterThanOrEqual(divisor)) do
        approx = math.max(1, math.floor(rem:toNumber() / divisor:toNumber()))

        local log2 = math.ceil(math.log(approx) / LN2)

        local delta = 1
        if log2 <= 48 then
            delta = 2 ^ (log2 - 48)
        end

        local approxRes = fromNumber(approx)
        local approxRem = approxRes:multiply(divisor)
        while (approxRem:isNegative() or approxRem:greaterThan(rem)) do
            approx = approx - delta
            approxRes = fromNumber(approx)
            approxRem = approxRes:multiply(divisor)
        end

        if (approxRes:isZero()) then
            approxRes = clone(ONE)
        end

        res = res:add(approxRes)
        rem = rem:subtract(approxRem)
    end
    return res
end

function shiftRight(numBits)
    numBits = numBits & 63
    if (numBits == 0) then
        return self
    elseif (numBits < 32) then
        return fromBits((self.low >> numBits) | (self.high << (32 - numBits)), self.high >> numBits)
    else
        if (self.high >= 0) then
            return fromBits(self.high >> (numBits - 32), 0)
        else
            return fromBits(self.high >> (numBits - 32), -1)
        end
    end
end

function shiftLeft(numBits)
    numBits = numBits & 63
    if (numBits == 0) then
        return self
    elseif (numBits < 32) then
        return fromBits(self.low << numBits, (self.high << numBits) | (self.low >> (32 - numBits)))
    else
        return fromBits(0, self.low << (numBits - 32))
    end
end

function Long:compare(other)
    if (self:equals(other)) then
        return 0
    end
    local thisNeg = self:isNegative()
    local otherNeg = other:isNegative()
    if (thisNeg and not (otherNeg)) then
        return -1
    end
    if (not (thisNeg) and otherNeg) then
        return 1
    end
    if self:subtract(other):isNegative() then
        return -1
    else
        return 1
    end
end

function Long:greaterThanOrEqual(other)
    return self:compare(other) >= 0
end

function Long:greaterThan(other)
    return self:compare(other) > 0
end

function Long:encodeZigzagLong()
    local mask = self.high >> 31
    if mask == 1 then
        self.high = ((self.high << 1 | self.low >> 31) ~ 0xFFFFFFFF) & 0xFFFFFFFF
        self.low = ((self.low << 1 | mask) ~ 0xFFFFFFFE) & 0xFFFFFFFF
    else
        self.high = (self.high << 1 | self.low >> 31) & 0xFFFFFFFF
        self.low = (self.low << 1) & 0xFFFFFFFF
    end
    return self
end

function Long:decodeZigzagLong()
    local mask = self.low & 1
    if mask == 1 then
        self.low = (((self.low >> 1) | (self.high << 31)) ~ 0xFFFFFFFF) & 0xFFFFFFFF
        self.high = ((self.high >> 1 | (0x80000000)) ~ 0x7FFFFFFF) & 0xFFFFFFFF
    else
        self.low = ((self.low >> 1) | (self.high << 31)) & 0xFFFFFFFF
        self.high = (self.high >> 1) & 0xFFFFFFFF
    end
    return self
end

function Long:writeLong(byteBuffer, longValue)
    if type(longValue) == "string" then
        local len = #longValue
        if len <= 11 then
            local num = tonumber(longValue)
            if (MIN_INT <= num) and (num <= MAX_INT) then
                byteBuffer:writeInt(num)
                return
            end
        end
    end

    if type(longValue) == number then
        if (MIN_INT <= longValue) and (longValue <= MAX_INT) then
            byteBuffer:writeInt(tonumber(longValue))
            return
        end
    end

    --写入Long
    local value = Long:fromString(longValue)
    value:encodeZigzagLong()
    local count = 0
    while (value.high ~= 0) do
        byteBuffer:writeByte(value.low & 127 | 128)
        value.low = ((value.low >> 7) | (value.high << 25))
        value.high = (value.high >> 7)
        count = count + 7
    end
    while (value.low > 127) do
        if count >= 56 then
            byteBuffer:writeByte(value.low)
            return
        end
        byteBuffer:writeByte(value.low & 127 | 128)
        value.low = value.low >> 7
        count = count + 7
    end
    byteBuffer:writeByte(value.low)
end

local function fromByteBuffer(byteBuffer)
    local bits = Long:new(0, 0)
    local count = #byteBuffer
    local i = 0
    local pos = 1
    if (count > 4) then
        --先读入1到4位
        while i < 4 do
            bits.low = (bits.low | ((byteBuffer[pos] & 127) << (i * 7))) & 0xFFFFFFFF
            i = i + 1
            pos = pos + 1
        end
        --读第5位，第5位底位置读到low，高位置读到high
        bits.low = (bits.low | ((byteBuffer[pos] & 127) << 28)) & 0xFFFFFFFF
        bits.high = (bits.high | ((byteBuffer[pos] & 127) >> 4)) & 0xFFFFFFFF
        if (byteBuffer[pos] < 128) then
            return bits
        end
        i = 0
        pos = pos + 1
    else
        while i < 3 do
            bits.low = (bits.low | ((byteBuffer[pos] & 127) << (i * 7))) & 0xFFFFFFFF
            if (byteBuffer[pos] < 128) then
                return bits
            end
            i = i + 1
            pos = pos + 1
        end
        bits.low = (bits.low | ((byteBuffer[pos] & 127) << (i * 7))) & 0xFFFFFFFF
        return bits
    end

    --读最后4位
    while i < 4 do
        if (pos == 9) then
            bits.high = (bits.high | (byteBuffer[pos] << (i * 7 + 3))) & 0xFFFFFFFF
            return bits
        end
        bits.high = (bits.high | ((byteBuffer[pos] & 127) << (i * 7 + 3))) & 0xFFFFFFFF
        if (byteBuffer[pos] < 128) then
            return bits
        end
        i = i + 1
        pos = pos + 1
    end

    return bits
end

function Long:readLong(buffer)
    local byteBuffer = {}
    local b = buffer:readByte()
    local count = 1

    byteBuffer[count] = b
    count = count + 1
    if ((b & 0x80) ~= 0) then
        b = buffer:readByte()
        byteBuffer[count] = b
        count = count + 1
        if ((b & 0x80) ~= 0) then
            b = buffer:readByte()
            byteBuffer[count] = b
            count = count + 1
            if ((b & 0x80) ~= 0) then
                b = buffer:readByte()
                byteBuffer[count] = b
                count = count + 1
                if ((b & 0x80) ~= 0) then
                    b = buffer:readByte()
                    byteBuffer[count] = b
                    count = count + 1
                    if ((b & 0x80) ~= 0) then
                        b = buffer:readByte()
                        byteBuffer[count] = b
                        count = count + 1
                        if ((b & 0x80) ~= 0) then
                            b = buffer:readByte()
                            byteBuffer[count] = b
                            count = count + 1
                            if ((b & 0x80) ~= 0) then
                                b = buffer:readByte()
                                byteBuffer[count] = b
                                count = count + 1
                                if ((b & 0x80) ~= 0) then
                                    b = buffer:readByte()
                                    byteBuffer[count] = b
                                    count = count + 1
                                end
                            end
                        end
                    end
                end
            end
        end
    end

    local longValue = fromByteBuffer(byteBuffer)
    longValue:decodeZigzagLong()
    return longValue
end

return Long