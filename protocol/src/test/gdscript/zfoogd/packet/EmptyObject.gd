const ByteBuffer = preload("res://zfoogd/ByteBuffer.gd")


class EmptyObject:
	

	func _to_string() -> String:
		const jsonTemplate = "{}"
		var params = []
		return jsonTemplate.format(params, "{}")

class EmptyObjectRegistration:
	func getProtocolId():
		return 0

	func write(buffer: ByteBuffer, packet: EmptyObject):
		if (packet == null):
			buffer.writeInt(0)
			return
		buffer.writeInt(-1)
		pass

	func read(buffer: ByteBuffer):
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet = EmptyObject.new()
		
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet