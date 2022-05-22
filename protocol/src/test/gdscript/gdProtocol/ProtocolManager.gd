const ComplexObject = preload("res://gdProtocol/packet/ComplexObject.gd")
const NormalObject = preload("res://gdProtocol/packet/NormalObject.gd")
const ObjectA = preload("res://gdProtocol/packet/ObjectA.gd")
const ObjectB = preload("res://gdProtocol/packet/ObjectB.gd")
const SimpleObject = preload("res://gdProtocol/packet/SimpleObject.gd")

const protocols = {}

static func getProtocol(protocolId: int):
	return protocols[protocolId]

static func newInstance(protocolId: int):
	var protocol = protocols[protocolId]
	return protocol.new()

static func write(buffer, packet):
	var protocolId: int = packet.PROTOCOL_ID
	buffer.writeShort(protocolId)
	var protocol = protocols[protocolId]
	protocol.write(buffer, packet)

static func read(buffer):
	var protocolId = buffer.readShort();
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer);
	return packet;

static func initProtocol():
	protocols[100] = ComplexObject
	protocols[101] = NormalObject
	protocols[102] = ObjectA
	protocols[103] = ObjectB
	protocols[104] = SimpleObject
