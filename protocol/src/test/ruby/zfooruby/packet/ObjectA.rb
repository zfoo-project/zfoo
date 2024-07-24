
class ObjectA
  attr_accessor :a  # int
  attr_accessor :m  # Dictionary<int, string>
  attr_accessor :objectB  # ObjectB
  attr_accessor :innerCompatibleValue  # int
  def initialize()
    @a = 0
    @m = Hash.new()
    @objectB = nil
    @innerCompatibleValue = 0
  end
end

class ObjectARegistration
  def protocolId()
    return 102
  end

  def write(buffer, packet)
    if packet.nil?
      buffer.writeInt(0)
      return
    end
    beforeWriteIndex = buffer.getWriteOffset()
    buffer.writeInt(201)
    buffer.writeInt(packet.a)
    buffer.writeIntStringMap(packet.m)
    buffer.writePacket(packet.objectB, 103)
    buffer.writeInt(packet.innerCompatibleValue)
    buffer.adjustPadding(201, beforeWriteIndex)
  end

  def read(buffer)
    length = buffer.readInt()
    if length == 0
      return nil
    end
    beforeReadIndex = buffer.getReadOffset()
    packet = ObjectA.new()
    result0 = buffer.readInt()
    packet.a = result0
    map1 = buffer.readIntStringMap()
    packet.m = map1
    result2 = buffer.readPacket(103)
    packet.objectB = result2
    if buffer.compatibleRead(beforeReadIndex, length)
      result3 = buffer.readInt()
      packet.innerCompatibleValue = result3
    end
    if length > 0
      buffer.setReadOffset(beforeReadIndex + length)
    end
    return packet
  end
end