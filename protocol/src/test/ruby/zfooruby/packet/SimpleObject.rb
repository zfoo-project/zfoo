
class SimpleObject
    attr_accessor :c  # int
    attr_accessor :g  # bool
    def initialize()
        @c = 0
        @g = false
    end
end

class SimpleObjectRegistration
    def protocolId()
        return 104
    end

    def write(buffer, packet)
        if packet.nil?
            buffer.writeInt(0)
            return
        end
        buffer.writeInt(-1)
        buffer.writeInt(packet.c)
        buffer.writeBool(packet.g)
    end

    def read(buffer)
        length = buffer.readInt()
        if length == 0
            return nil
        end
        beforeReadIndex = buffer.getReadOffset()
        packet = SimpleObject.new()
        result0 = buffer.readInt()
        packet.c = result0
        result1 = buffer.readBool() 
        packet.g = result1
        if length > 0
            buffer.setReadOffset(beforeReadIndex + length)
        end
        return packet
    end
end