const EmptyObject = preload("./packet/EmptyObject.gd")
const VeryBigObject = preload("./packet/VeryBigObject.gd")
const ComplexObject = preload("./packet/ComplexObject.gd")
const NormalObject = preload("./packet/NormalObject.gd")
const ObjectA = preload("./packet/ObjectA.gd")
const ObjectB = preload("./packet/ObjectB.gd")
const SimpleObject = preload("./packet/SimpleObject.gd")

static var protocols: Dictionary[int, RefCounted] = {}
static var protocolClassMap: Dictionary[int, Object] = {}

static func initProtocol():
	protocols[0] = EmptyObject.EmptyObjectRegistration.new()
	protocolClassMap[0] = EmptyObject
	protocols[1] = VeryBigObject.VeryBigObjectRegistration.new()
	protocolClassMap[1] = VeryBigObject
	protocols[100] = ComplexObject.ComplexObjectRegistration.new()
	protocolClassMap[100] = ComplexObject
	protocols[101] = NormalObject.NormalObjectRegistration.new()
	protocolClassMap[101] = NormalObject
	protocols[102] = ObjectA.ObjectARegistration.new()
	protocolClassMap[102] = ObjectA
	protocols[103] = ObjectB.ObjectBRegistration.new()
	protocolClassMap[103] = ObjectB
	protocols[104] = SimpleObject.SimpleObjectRegistration.new()
	protocolClassMap[104] = SimpleObject
	pass

static func getProtocol(protocolId: int):
	return protocols[protocolId]

static func getProtocolClass(protocolId: int):
	return protocolClassMap[protocolId]

static func newInstance(protocolId: int):
	var protocol = protocolClassMap[protocolId]
	return protocol.new()

static func write(buffer, packet):
	var protocolId: int = packet.protocolId()
	buffer.writeShort(protocolId)
	var protocol = protocols[protocolId]
	protocol.write(buffer, packet)

static func read(buffer):
	var protocolId = buffer.readShort()
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer)
	return packet