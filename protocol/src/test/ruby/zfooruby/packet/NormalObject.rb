# 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject
    attr_accessor :a  # byte
    attr_accessor :aaa  # byte[]
    attr_accessor :b  # short
    # 整数类型
    attr_accessor :c  # int
    attr_accessor :d  # long
    attr_accessor :e  # float
    attr_accessor :f  # double
    attr_accessor :g  # bool
    attr_accessor :jj  # string
    attr_accessor :kk  # ObjectA
    attr_accessor :l  # List<int>
    attr_accessor :ll  # List<long>
    attr_accessor :lll  # List<ObjectA>
    attr_accessor :llll  # List<string>
    attr_accessor :m  # Dictionary<int, string>
    attr_accessor :mm  # Dictionary<int, ObjectA>
    attr_accessor :s  # HashSet<int>
    attr_accessor :ssss  # HashSet<string>
    attr_accessor :outCompatibleValue  # int
    attr_accessor :outCompatibleValue2  # int
    def initialize()
        @a = 0
        @aaa = Array.new()
        @b = 0
        @c = 0
        @d = 0
        @e = 0.0
        @f = 0.0
        @g = false
        @jj = ""
        @kk = nil
        @l = Array.new()
        @ll = Array.new()
        @lll = Array.new()
        @llll = Array.new()
        @m = Hash.new()
        @mm = Hash.new()
        @s = Set.new()
        @ssss = Set.new()
        @outCompatibleValue = 0
        @outCompatibleValue2 = 0
    end
end

class NormalObjectRegistration
    def protocolId()
        return 101
    end

    def write(buffer, packet)
        if packet.nil?
            buffer.writeInt(0)
            return
        end
        beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(857)
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
        buffer.writeInt(packet.outCompatibleValue)
        buffer.writeInt(packet.outCompatibleValue2)
        buffer.adjustPadding(857, beforeWriteIndex)
    end

    def read(buffer)
        length = buffer.readInt()
        if length == 0
            return nil
        end
        beforeReadIndex = buffer.getReadOffset()
        packet = NormalObject.new()
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
        if buffer.compatibleRead(beforeReadIndex, length)
            result18 = buffer.readInt()
            packet.outCompatibleValue = result18
        end
        if buffer.compatibleRead(beforeReadIndex, length)
            result19 = buffer.readInt()
            packet.outCompatibleValue2 = result19
        end
        if length > 0
            buffer.setReadOffset(beforeReadIndex + length)
        end
        return packet
    end
end