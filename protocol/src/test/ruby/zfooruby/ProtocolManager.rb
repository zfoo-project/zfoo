require_relative 'packet/EmptyObject.rb'
require_relative 'packet/VeryBigObject.rb'
require_relative 'packet/ComplexObject.rb'
require_relative 'packet/NormalObject.rb'
require_relative 'packet/ObjectA.rb'
require_relative 'packet/ObjectB.rb'
require_relative 'packet/SimpleObject.rb'


class ProtocolManager
  @@protocols = Array.new(32767)
  @@protocolIdMap = Hash.new()

  def self.initProtocol()
  	@@protocols[0] = EmptyObjectRegistration.new()
  	@@protocolIdMap[EmptyObject] = 0
  	@@protocols[1] = VeryBigObjectRegistration.new()
  	@@protocolIdMap[VeryBigObject] = 1
  	@@protocols[100] = ComplexObjectRegistration.new()
  	@@protocolIdMap[ComplexObject] = 100
  	@@protocols[101] = NormalObjectRegistration.new()
  	@@protocolIdMap[NormalObject] = 101
  	@@protocols[102] = ObjectARegistration.new()
  	@@protocolIdMap[ObjectA] = 102
  	@@protocols[103] = ObjectBRegistration.new()
  	@@protocolIdMap[ObjectB] = 103
  	@@protocols[104] = SimpleObjectRegistration.new()
  	@@protocolIdMap[SimpleObject] = 104
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