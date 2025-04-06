class_name SimpleObject

const ByteBuffer = preload("../ByteBuffer.gd")


var c: int
var g: bool

func protocolId() -> int:
	return 104

func _to_string() -> String:
	const jsonTemplate = "{c:{}, g:{}}"
	var params = [self.c, self.g]
	return jsonTemplate.format(params, "{}")

class SimpleObjectRegistration:
	func write(buffer: ByteBuffer, packet: SimpleObject):
		if (packet == null):
			buffer.writeInt(0)
			return
		buffer.writeInt(-1)
		buffer.writeInt(packet.c)
		buffer.writeBool(packet.g)
		pass

	func read(buffer: ByteBuffer) -> SimpleObject:
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet: SimpleObject = buffer.newInstance(104)
		var result0 = buffer.readInt()
		packet.c = result0
		var result1 = buffer.readBool() 
		packet.g = result1
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet