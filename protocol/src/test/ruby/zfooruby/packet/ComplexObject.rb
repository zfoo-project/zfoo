# 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
class ComplexObject
  # byte类型，最简单的整形
  attr_accessor :a  # byte
  # byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
  attr_accessor :aa  # byte
  # 数组类型
  attr_accessor :aaa  # byte[]
  attr_accessor :aaaa  # byte[]
  attr_accessor :b  # short
  attr_accessor :bb  # short
  attr_accessor :bbb  # short[]
  attr_accessor :bbbb  # short[]
  attr_accessor :c  # int
  attr_accessor :cc  # int
  attr_accessor :ccc  # int[]
  attr_accessor :cccc  # int[]
  attr_accessor :d  # long
  attr_accessor :dd  # long
  attr_accessor :ddd  # long[]
  attr_accessor :dddd  # long[]
  attr_accessor :e  # float
  attr_accessor :ee  # float
  attr_accessor :eee  # float[]
  attr_accessor :eeee  # float[]
  attr_accessor :f  # double
  attr_accessor :ff  # double
  attr_accessor :fff  # double[]
  attr_accessor :ffff  # double[]
  attr_accessor :g  # bool
  attr_accessor :gg  # bool
  attr_accessor :ggg  # bool[]
  attr_accessor :gggg  # bool[]
  attr_accessor :jj  # string
  attr_accessor :jjj  # string[]
  attr_accessor :kk  # ObjectA
  attr_accessor :kkk  # ObjectA[]
  attr_accessor :l  # List<int>
  attr_accessor :ll  # List<List<List<int>>>
  attr_accessor :lll  # List<List<ObjectA>>
  attr_accessor :llll  # List<string>
  attr_accessor :lllll  # List<Dictionary<int, string>>
  attr_accessor :m  # Dictionary<int, string>
  attr_accessor :mm  # Dictionary<int, ObjectA>
  attr_accessor :mmm  # Dictionary<ObjectA, List<int>>
  attr_accessor :mmmm  # Dictionary<List<List<ObjectA>>, List<List<List<int>>>>
  attr_accessor :mmmmm  # Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>>
  attr_accessor :s  # HashSet<int>
  attr_accessor :ss  # HashSet<HashSet<List<int>>>
  attr_accessor :sss  # HashSet<HashSet<ObjectA>>
  attr_accessor :ssss  # HashSet<string>
  attr_accessor :sssss  # HashSet<Dictionary<int, string>>
  # 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
  attr_accessor :myCompatible  # int
  attr_accessor :myObject  # ObjectA
  def initialize()
    @a = 0
    @aa = 0
    @aaa = Array.new()
    @aaaa = Array.new()
    @b = 0
    @bb = 0
    @bbb = Array.new()
    @bbbb = Array.new()
    @c = 0
    @cc = 0
    @ccc = Array.new()
    @cccc = Array.new()
    @d = 0
    @dd = 0
    @ddd = Array.new()
    @dddd = Array.new()
    @e = 0.0
    @ee = 0.0
    @eee = Array.new()
    @eeee = Array.new()
    @f = 0.0
    @ff = 0.0
    @fff = Array.new()
    @ffff = Array.new()
    @g = false
    @gg = false
    @ggg = Array.new()
    @gggg = Array.new()
    @jj = ""
    @jjj = Array.new()
    @kk = nil
    @kkk = Array.new()
    @l = Array.new()
    @ll = Array.new()
    @lll = Array.new()
    @llll = Array.new()
    @lllll = Array.new()
    @m = Hash.new()
    @mm = Hash.new()
    @mmm = Hash.new()
    @mmmm = Hash.new()
    @mmmmm = Hash.new()
    @s = Set.new()
    @ss = Set.new()
    @sss = Set.new()
    @ssss = Set.new()
    @sssss = Set.new()
    @myCompatible = 0
    @myObject = nil
  end
end

class ComplexObjectRegistration
  def protocolId()
    return 100
  end

  def write(buffer, packet)
    if packet.nil?
      buffer.writeInt(0)
      return
    end
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
    buffer.writeBoolArray(packet.ggg)
    buffer.writeBoolArray(packet.gggg)
    buffer.writeString(packet.jj)
    buffer.writeStringArray(packet.jjj)
    buffer.writePacket(packet.kk, 102)
    buffer.writePacketArray(packet.kkk, 102)
    buffer.writeIntArray(packet.l)
    if packet.ll.nil? || packet.ll.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.ll.length)
      for element0 in packet.ll
        if element0.nil? || element0.empty?
          buffer.writeInt(0)
        else
          buffer.writeInt(element0.length)
          for element1 in element0
            buffer.writeIntArray(element1)
          end
        end
      end
    end
    if packet.lll.nil? || packet.lll.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.lll.length)
      for element2 in packet.lll
        buffer.writePacketArray(element2, 102)
      end
    end
    buffer.writeStringArray(packet.llll)
    if packet.lllll.nil? || packet.lllll.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.lllll.length)
      for element3 in packet.lllll
        buffer.writeIntStringMap(element3)
      end
    end
    buffer.writeIntStringMap(packet.m)
    buffer.writeIntPacketMap(packet.mm, 102)
    if packet.mmm.nil? || packet.mmm.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.mmm.length)
      packet.mmm.each do |key4, value5|
        buffer.writePacket(key4, 102)
        buffer.writeIntArray(value5)
      end
    end
    if packet.mmmm.nil? || packet.mmmm.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.mmmm.length)
      packet.mmmm.each do |key6, value7|
        if key6.nil? || key6.empty?
          buffer.writeInt(0)
        else
          buffer.writeInt(key6.length)
          for element8 in key6
            buffer.writePacketArray(element8, 102)
          end
        end
        if value7.nil? || value7.empty?
          buffer.writeInt(0)
        else
          buffer.writeInt(value7.length)
          for element9 in value7
            if element9.nil? || element9.empty?
              buffer.writeInt(0)
            else
              buffer.writeInt(element9.length)
              for element10 in element9
                buffer.writeIntArray(element10)
              end
            end
          end
        end
      end
    end
    if packet.mmmmm.nil? || packet.mmmmm.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.mmmmm.length)
      packet.mmmmm.each do |key11, value12|
        if key11.nil? || key11.empty?
          buffer.writeInt(0)
        else
          buffer.writeInt(key11.length)
          for element13 in key11
            buffer.writeIntStringMap(element13)
          end
        end
        if value12.nil? || value12.empty?
          buffer.writeInt(0)
        else
          buffer.writeInt(value12.length)
          for element14 in value12
            buffer.writeIntStringMap(element14)
          end
        end
      end
    end
    buffer.writeIntSet(packet.s)
    if packet.ss.nil? || packet.ss.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.ss.length)
      for element15 in packet.ss
        if element15.nil? || element15.empty?
          buffer.writeInt(0)
        else
          buffer.writeInt(element15.length)
          for element16 in element15
            buffer.writeIntArray(element16)
          end
        end
      end
    end
    if packet.sss.nil? || packet.sss.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.sss.length)
      for element17 in packet.sss
        buffer.writePacketSet(element17, 102)
      end
    end
    buffer.writeStringSet(packet.ssss)
    if packet.sssss.nil? || packet.sssss.empty?
      buffer.writeInt(0)
    else
      buffer.writeInt(packet.sssss.length)
      for element18 in packet.sssss
        buffer.writeIntStringMap(element18)
      end
    end
    buffer.writeInt(packet.myCompatible)
    buffer.writePacket(packet.myObject, 102)
    buffer.adjustPadding(36962, beforeWriteIndex)
  end

  def read(buffer)
    length = buffer.readInt()
    if length == 0
      return nil
    end
    beforeReadIndex = buffer.getReadOffset()
    packet = ComplexObject.new()
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
    array26 = buffer.readBoolArray()
    packet.ggg = array26
    array27 = buffer.readBoolArray()
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
    result33 = Array.new()
    size35 = buffer.readInt()
    if size35 > 0
      for index34 in 0..size35 - 1
        result36 = Array.new()
        size38 = buffer.readInt()
        if size38 > 0
          for index37 in 0..size38 - 1
            list39 = buffer.readIntArray()
            result36.push(list39)
          end
        end
        result33.push(result36)
      end
    end
    packet.ll = result33
    result40 = Array.new()
    size42 = buffer.readInt()
    if size42 > 0
      for index41 in 0..size42 - 1
        list43 = buffer.readPacketArray(102)
        result40.push(list43)
      end
    end
    packet.lll = result40
    list44 = buffer.readStringArray()
    packet.llll = list44
    result45 = Array.new()
    size47 = buffer.readInt()
    if size47 > 0
      for index46 in 0..size47 - 1
        map48 = buffer.readIntStringMap()
        result45.push(map48)
      end
    end
    packet.lllll = result45
    map49 = buffer.readIntStringMap()
    packet.m = map49
    map50 = buffer.readIntPacketMap(102)
    packet.mm = map50
    result51 = Hash.new()
    size52 = buffer.readInt()
    if size52 > 0
      for index53 in 0..size52 - 1
        result54 = buffer.readPacket(102)
        list55 = buffer.readIntArray()
        result51[result54] = list55
      end
    end
    packet.mmm = result51
    result56 = Hash.new()
    size57 = buffer.readInt()
    if size57 > 0
      for index58 in 0..size57 - 1
        result59 = Array.new()
        size61 = buffer.readInt()
        if size61 > 0
          for index60 in 0..size61 - 1
            list62 = buffer.readPacketArray(102)
            result59.push(list62)
          end
        end
        result63 = Array.new()
        size65 = buffer.readInt()
        if size65 > 0
          for index64 in 0..size65 - 1
            result66 = Array.new()
            size68 = buffer.readInt()
            if size68 > 0
              for index67 in 0..size68 - 1
                list69 = buffer.readIntArray()
                result66.push(list69)
              end
            end
            result63.push(result66)
          end
        end
        result56[result59] = result63
      end
    end
    packet.mmmm = result56
    result70 = Hash.new()
    size71 = buffer.readInt()
    if size71 > 0
      for index72 in 0..size71 - 1
        result73 = Array.new()
        size75 = buffer.readInt()
        if size75 > 0
          for index74 in 0..size75 - 1
            map76 = buffer.readIntStringMap()
            result73.push(map76)
          end
        end
        result77 = Set.new()
        size79 = buffer.readInt()
        if size79 > 0
          for index78 in 0..size79 - 1
            map80 = buffer.readIntStringMap()
            result77.add(map80)
          end
        end
        result70[result73] = result77
      end
    end
    packet.mmmmm = result70
    set81 = buffer.readIntSet()
    packet.s = set81
    result82 = Set.new()
    size84 = buffer.readInt()
    if size84 > 0
      for index83 in 0..size84 - 1
        result85 = Set.new()
        size87 = buffer.readInt()
        if size87 > 0
          for index86 in 0..size87 - 1
            list88 = buffer.readIntArray()
            result85.add(list88)
          end
        end
        result82.add(result85)
      end
    end
    packet.ss = result82
    result89 = Set.new()
    size91 = buffer.readInt()
    if size91 > 0
      for index90 in 0..size91 - 1
        set92 = buffer.readPacketSet(102)
        result89.add(set92)
      end
    end
    packet.sss = result89
    set93 = buffer.readStringSet()
    packet.ssss = set93
    result94 = Set.new()
    size96 = buffer.readInt()
    if size96 > 0
      for index95 in 0..size96 - 1
        map97 = buffer.readIntStringMap()
        result94.add(map97)
      end
    end
    packet.sssss = result94
    if buffer.compatibleRead(beforeReadIndex, length)
      result98 = buffer.readInt()
      packet.myCompatible = result98
    end
    if buffer.compatibleRead(beforeReadIndex, length)
      result99 = buffer.readPacket(102)
      packet.myObject = result99
    end
    if length > 0
      buffer.setReadOffset(beforeReadIndex + length)
    end
    return packet
  end
end