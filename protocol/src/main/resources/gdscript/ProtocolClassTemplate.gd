${protocol_note}
class ${protocol_name}:
	${protocol_field_definition}

	const PROTOCOL_ID = ${protocol_id}
	const PROTOCOL_CLASS_NAME = "${protocol_name}"

	func _to_string() -> String:
		const jsonTemplate = "${protocol_json}"
		var params = [${protocol_to_string}]
		return jsonTemplate.format(params, "{}")

	static func write(buffer, packet):
		if (packet == null):
			buffer.writeInt(0)
			return
		${protocol_write_serialization}
		pass

	static func read(buffer):
		var length = buffer.readInt()
		if (length == 0):
			return null
		var beforeReadIndex = buffer.getReadOffset()
		var packet = buffer.newInstance(PROTOCOL_ID)
		${protocol_read_deserialization}
		if (length > 0):
			buffer.setReadOffset(beforeReadIndex + length)
		return packet
