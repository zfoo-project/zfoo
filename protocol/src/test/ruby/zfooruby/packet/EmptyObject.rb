
class EmptyObject
    
    def initialize()
        
    end
end

class EmptyObjectRegistration
    def protocolId()
        return 0
    end

    def write(buffer, packet)
        if packet.nil?
            buffer.writeInt(0)
            return
        end
        buffer.writeInt(-1)
    end

    def read(buffer)
        length = buffer.readInt()
        if length == 0
            return nil
        end
        beforeReadIndex = buffer.getReadOffset()
        packet = EmptyObject.new()
        
        if length > 0
            buffer.setReadOffset(beforeReadIndex + length)
        end
        return packet
    end
end