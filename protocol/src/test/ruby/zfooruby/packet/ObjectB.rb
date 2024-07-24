
class ObjectB
    attr_accessor :flag  # bool
    attr_accessor :innerCompatibleValue  # int
    def initialize()
        @flag = false
        @innerCompatibleValue = 0
    end
end

class ObjectBRegistration
    def protocolId()
        return 103
    end

    def write(buffer, packet)
        if packet.nil?
            buffer.writeInt(0)
            return
        end
        beforeWriteIndex = buffer.getWriteOffset()
        buffer.writeInt(4)
        buffer.writeBool(packet.flag)
        buffer.writeInt(packet.innerCompatibleValue)
        buffer.adjustPadding(4, beforeWriteIndex)
    end

    def read(buffer)
        length = buffer.readInt()
        if length == 0
            return nil
        end
        beforeReadIndex = buffer.getReadOffset()
        packet = ObjectB.new()
        result0 = buffer.readBool() 
        packet.flag = result0
        if buffer.compatibleRead(beforeReadIndex, length)
            result1 = buffer.readInt()
            packet.innerCompatibleValue = result1
        end
        if length > 0
            buffer.setReadOffset(beforeReadIndex + length)
        end
        return packet
    end
end