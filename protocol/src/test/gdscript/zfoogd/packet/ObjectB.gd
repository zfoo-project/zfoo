class_name ObjectB

const ByteBuffer = preload("../ByteBuffer.gd")


var flag: bool

func protocolId() -> int:
	return 103

func _to_string() -> String:
	const jsonTemplate = "{flag:{}}"
	var params = [self.flag]
	return jsonTemplate.format(params, "{}")

class ObjectBRegistration:
	func write(buffer: ByteBuffer, packet: ObjectB):
		if (packet == null):
			buffer.writeInt(0)
			return
		buffer.writeInt(-1)
		buffer.writeBool(packet.flag)
		pass

	func read(buffer: ByteBuffer) -> ObjectB:
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet: ObjectB = buffer.newInstance(103)
		var result0 = buffer.readBool() 
		packet.flag = result0
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet