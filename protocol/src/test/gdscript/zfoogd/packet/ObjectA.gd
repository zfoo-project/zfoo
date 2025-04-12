class_name ObjectA


var a: int
var m: Dictionary[int, String]
var objectB: ObjectB

func protocolId() -> int:
	return 102

func _to_string() -> String:
	const jsonTemplate = "{a:{}, m:{}, objectB:{}}"
	var params = [self.a, self.m, self.objectB]
	return jsonTemplate.format(params, "{}")

static func write(buffer: ByteBuffer, packet: ObjectA):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeInt(packet.a)
	buffer.writeIntStringMap(packet.m)
	buffer.writePacket(packet.objectB, ObjectB)
	pass

static func read(buffer: ByteBuffer) -> ObjectA:
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet: ObjectA = ObjectA.new()
	var result0 = buffer.readInt()
	packet.a = result0
	var map1 = buffer.readIntStringMap()
	packet.m = map1
	var result2 = buffer.readPacket(ObjectB)
	packet.objectB = result2
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet