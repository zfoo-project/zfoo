const PROTOCOL_ID = 102
const PROTOCOL_CLASS_NAME = "ObjectA"
const ObjectB = preload("res://zfoogd/packet/ObjectB.gd")


var a: int
var m: Dictionary	# Map<number, string>
var objectB: ObjectB

func _to_string() -> String:
	const jsonTemplate = "{a:{}, m:{}, objectB:{}}"
	var params = [self.a, JSON.stringify(self.m), self.objectB]
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeInt(packet.a)
	buffer.writeIntStringMap(packet.m)
	buffer.writePacket(packet.objectB, 103)
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readInt()
	packet.a = result0
	var map1 = buffer.readIntStringMap()
	packet.m = map1
	var result2 = buffer.readPacket(103)
	packet.objectB = result2
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
