${protocol_note}
class ${protocol_name}:
	${protocol_field_definition}

	func protocolId() -> int:
		return ${protocol_id}

	func _to_string() -> String:
		const jsonTemplate = "${protocol_json}"
		var params = [${protocol_to_string}]
		return jsonTemplate.format(params, "{}")

	static func write(buffer: ByteBuffer, packet: ${protocol_name}):
		if (packet == null):
			buffer.writeInt(0)
			return
		${protocol_write_serialization}
		pass

	static func read(buffer: ByteBuffer) -> ${protocol_name}:
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet: ${protocol_name} = ${protocol_name}.new()
		${protocol_read_deserialization}
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet