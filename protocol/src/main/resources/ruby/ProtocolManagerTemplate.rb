${protocol_imports}


class ProtocolManager
	@@protocols = Array.new(32767)
	@@protocolIdMap = Hash.new()

	def self.initProtocol()
		${protocol_manager_registrations}
	end

	def self.getProtocol(protocolId)
		return @@protocols[protocolId]
	end

	def self.write(buffer, packet)
		protocolId = @@protocolIdMap[packet.class]
		buffer.writeShort(protocolId)
		protocol = @@protocols[protocolId]
		protocol.write(buffer, packet)
	end

	def self.read(buffer)
		protocolId = buffer.readShort()
		protocol = @@protocols[protocolId]
		packet = protocol.read(buffer)
		return packet
	end
end