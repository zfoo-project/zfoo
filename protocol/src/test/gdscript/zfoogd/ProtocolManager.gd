const EmptyObject = preload("res://zfoogd/packet/EmptyObject.gd")
const VeryBigObject = preload("res://zfoogd/packet/VeryBigObject.gd")
const ComplexObject = preload("res://zfoogd/packet/ComplexObject.gd")
const NormalObject = preload("res://zfoogd/packet/NormalObject.gd")
const ObjectA = preload("res://zfoogd/packet/ObjectA.gd")
const ObjectB = preload("res://zfoogd/packet/ObjectB.gd")
const SimpleObject = preload("res://zfoogd/packet/SimpleObject.gd")

const protocols: Dictionary = {
	0: EmptyObject,
	1: VeryBigObject,
	100: ComplexObject,
	101: NormalObject,
	102: ObjectA,
	103: ObjectB,
	104: SimpleObject
}

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
	var protocolId = buffer.readShort()
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer)
	return packet
