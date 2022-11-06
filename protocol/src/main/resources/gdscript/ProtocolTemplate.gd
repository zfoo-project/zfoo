const PROTOCOL_ID = {}
{}
{}
{}

func get_class() -> String:
	return "{}"

static func write(buffer, packet):
	if (buffer.writePacketFlag(packet)):
		return
	{}

static func read(buffer):
	if (!buffer.readBool()):
		return null
	var packet = buffer.newInstance(PROTOCOL_ID)
	{}
	return packet
