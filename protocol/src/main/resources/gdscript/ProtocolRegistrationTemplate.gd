class ${protocol_name}Registration:
	func write(buffer: ByteBuffer, packet: Object):
		if (packet == null):
			buffer.writeInt(0)
			return
		${protocol_write_serialization}
		pass

	func read(buffer: ByteBuffer):
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet = buffer.newInstance(${protocol_id})
		${protocol_read_deserialization}
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet