class_name EmptyObject

const ByteBuffer = preload("../ByteBuffer.gd")




func protocolId() -> int:
	return 0

func _to_string() -> String:
	const jsonTemplate = "{}"
	var params = []
	return jsonTemplate.format(params, "{}")

class EmptyObjectRegistration:
	func write(buffer: ByteBuffer, packet: EmptyObject):
		if (packet == null):
			buffer.writeInt(0)
			return
		buffer.writeInt(-1)
		pass

	func read(buffer: ByteBuffer) -> EmptyObject:
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet: EmptyObject = buffer.newInstance(0)
		
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet