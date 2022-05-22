const ProtocolManager = preload("res://gdProtocol/ProtocolManager.gd")

const EMPTY: String = ""

var buffer = StreamPeerBuffer.new()

var writeOffset: int = 0 setget setWriteOffset, getWriteOffset
var readOffset: int = 0 setget setReadOffset, getReadOffset

func _init():
	buffer.big_endian = true

# -------------------------------------------------get/set-------------------------------------------------
func setWriteOffset(writeIndex: int) -> void:
	if (writeIndex > buffer.get_size()):
		var template = "writeIndex[{}] out of bounds exception: readerIndex: {}, writerIndex: {} (expected: 0 <= readerIndex <= writerIndex <= capacity: {})"
		printerr(template.format([writeIndex, readOffset, writeOffset, buffer.size()], "{}"))
		return
	writeOffset = writeIndex

func getWriteOffset() -> int:
	return writeOffset

func setReadOffset(readIndex: int) -> void:
	if (readIndex > writeOffset):
		var template = "readIndex[{}] out of bounds exception: readerIndex: {}, writerIndex: {} (expected: 0 <= readerIndex <= writerIndex <= capacity: {})"
		printerr(template.format([readIndex, readOffset, writeOffset, buffer.size()], "{}"))
		return
	readOffset = readIndex

func getReadOffset() -> int:
	return readOffset

func isReadable() -> bool:
	return writeOffset > readOffset

# -------------------------------------------------write/read-------------------------------------------------
func writePoolByteArray(value: PoolByteArray):
	var length = value.size()
	buffer.put_partial_data(value)
	writeOffset += length
	
func writeBool(value: bool) -> void:
	var byte = 0
	if (value):
		byte = 1
	buffer.seek(writeOffset)
	buffer.put_8(byte)
	writeOffset += 1

func readBool() -> bool:
	buffer.seek(readOffset)
	var byte = buffer.get_8()
	readOffset += 1
	return byte == 1

func writeByte(value: int) -> void:
	buffer.seek(writeOffset)
	buffer.put_8(value)
	writeOffset += 1

func readByte() -> int:
	buffer.seek(readOffset)
	var value = buffer.get_8()
	readOffset += 1
	return value

func writeShort(value: int) -> void:
	buffer.seek(writeOffset)
	buffer.put_16(value)
	writeOffset += 2

func readShort() -> int:
	buffer.seek(readOffset)
	var value = buffer.get_16()
	readOffset += 2
	return value

func writeInt(value) -> void:
	writeLong(value)

func readInt() -> int:
	return readLong()

func writeLong(longValue: int) -> void:
	var value = (longValue << 1) ^ (longValue >> 63)

	if (value >> 7 == 0):
		writeByte(value)
		return

	if (value >> 14 == 0):
		writeByte(value | 0x80)
		writeByte(value >> 7)
		return

	if (value >> 21 == 0):
		writeByte(value | 0x80)
		writeByte((value >> 7) | 0x80)
		writeByte(value >> 14)
		return

	if (value >> 28 == 0):
		writeByte(value | 0x80)
		writeByte((value >> 7) | 0x80)
		writeByte((value >> 14) | 0x80)
		writeByte(value >> 21)
		return

	if (value >> 35 == 0):
		writeByte(value | 0x80)
		writeByte((value >> 7) | 0x80)
		writeByte((value >> 14) | 0x80)
		writeByte((value >> 21) | 0x80)
		writeByte(value >> 28)
		return

	if (value >> 42 == 0):
		writeByte(value | 0x80)
		writeByte((value >> 7) | 0x80)
		writeByte((value >> 14) | 0x80)
		writeByte((value >> 21) | 0x80)
		writeByte((value >> 28) | 0x80)
		writeByte(value >> 35)
		return

	if (value >> 49 == 0):
		writeByte(value | 0x80)
		writeByte((value >> 7) | 0x80)
		writeByte((value >> 14) | 0x80)
		writeByte((value >> 21) | 0x80)
		writeByte((value >> 28) | 0x80)
		writeByte((value >> 35) | 0x80)
		writeByte(value >> 42)
		return

	if (value >> 56 == 0):
		writeByte(value | 0x80)
		writeByte((value >> 7) | 0x80)
		writeByte((value >> 14) | 0x80)
		writeByte((value >> 21) | 0x80)
		writeByte((value >> 28) | 0x80)
		writeByte((value >> 35) | 0x80)
		writeByte((value >> 42) | 0x80)
		writeByte(value >> 49)
		return

	writeByte(value | 0x80)
	writeByte((value >> 7) | 0x80)
	writeByte((value >> 14) | 0x80)
	writeByte((value >> 21) | 0x80)
	writeByte((value >> 28) | 0x80)
	writeByte((value >> 35) | 0x80)
	writeByte((value >> 42) | 0x80)
	writeByte((value >> 49) | 0x80)
	writeByte(value >> 56)

func readLong() -> int:
	var byte: int = readByte()
	var value: int = byte
	if (byte < 0):
		byte = readByte()
		value = value & 0x00000000_0000007F | byte << 7
		if (byte < 0):
			byte = readByte()
			value = value & 0x00000000_00003FFF | byte << 14
			if (byte < 0):
				byte = readByte()
				value = value & 0x00000000_001FFFFF | byte << 21
				if (byte < 0):
					byte = readByte()
					value = value & 0x00000000_0FFFFFFF | byte << 28
					if (byte < 0):
						byte = readByte()
						value = value & 0x00000007_FFFFFFFF | byte << 35
						if (byte < 0):
							byte = readByte()
							value = value & 0x000003FF_FFFFFFFF | byte << 42
							if (byte < 0):
								byte = readByte()
								value = value & 0x0001FFFF_FFFFFFFF | byte << 49
								if (byte < 0):
									byte = readByte()
									value = value & 0x00FFFFFF_FFFFFFFF | byte << 56

	var mask = value >> 1
	if (mask < 0):
		mask = mask & 0x7FFFFFFF_FFFFFFFF
	return mask ^ -(value & 1)


func writeFloat(value: float) -> void:
	buffer.seek(writeOffset)
	buffer.put_float(value)
	writeOffset += 4

func readFloat() -> float:
	buffer.seek(readOffset)
	var value = buffer.get_float()
	readOffset += 4
	return value

func writeDouble(value: float) -> void:
	buffer.seek(writeOffset)
	buffer.put_double(value)
	writeOffset += 8

func readDouble() -> float:
	buffer.seek(readOffset)
	var value = buffer.get_double()
	readOffset += 8
	return value


func writeString(value: String) -> void:
	if (value == null || value.length() ==0):
		writeInt(0)
		return

	buffer.seek(writeOffset)

	var strBytes = value.to_utf8()
	var length = strBytes.size()
	writeInt(length)
	buffer.put_partial_data(strBytes)
	writeOffset += length

func readString() -> String:
	var length = readInt()
	if (length <= 0):
		return EMPTY

	buffer.seek(readOffset)
	var value = buffer.get_utf8_string(length)
	var strBytes = value.to_utf8()
	readOffset += length
	return value

func writeChar(value) -> void:
	if (value == null || value.length() == 0):
		writeString(EMPTY)
		return
	writeString(value[0])

func readChar() -> String:
	return readString()

func writePacketFlag(packet) -> bool:
	var flag = (packet == null)
	writeBool(!flag)
	return flag

func writePacket(packet, protocolId):
	var protocolRegistration = ProtocolManager.getProtocol(protocolId)
	protocolRegistration.write(self, packet)

func readPacket(protocolId):
	var protocolRegistration = ProtocolManager.getProtocol(protocolId)
	return protocolRegistration.read(self)

func newInstance(protocolId: int):
	return ProtocolManager.newInstance(protocolId)

func writeBooleanArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeBool(element)
			
func readBooleanArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readBool())
	return array
	
func writeByteArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeByte(element)
			
func readByteArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readByte())
	return array
	
func writeShortArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeShort(element)
			
func readShortArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readShort())
	return array
	
func writeIntArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeInt(element)
			
func readIntArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readInt())
	return array
	
func writeLongArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeLong(element)
			
func readLongArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readLong())
	return array
	
func writeFloatArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeFloat(element)
			
func readFloatArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readFloat())
	return array
	
func writeDoubleArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeDouble(element)
			
func readDoubleArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readDouble())
	return array
	
func writeCharArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeChar(element)
			
func readCharArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readChar())
	return array
	
func writeStringArray(array):
	if (array == null):
		writeInt(0)
	else:
		writeInt(array.size());
		for element in array:
			writeString(element)
			
func readStringArray():
	var array = []
	var size = readInt()
	if (size > 0):
		for index in range(size):
			array.append(readString())
	return array

	
func writePacketArray(array, protocolId):
	if (array == null):
		writeInt(0)
	else:
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		writeInt(array.size());
		for element in array:
			protocolRegistration.write(self, element)
			
func readPacketArray(protocolId):
	var array = []
	var size = readInt()
	if (size > 0):
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		for index in range(size):
			array.append(protocolRegistration.read(self))
	return array

func writeIntIntMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeInt(key)
			writeInt(map[key])
			
func readIntIntMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readInt()
			var value = readInt()
			map[key] = value
	return map
	
func writeIntLongMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeInt(map)
			writeLong(map[key])
			
func readIntLongMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readInt()
			var value = readLong()
			map[key] = value
	return map
	
func writeIntStringMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeInt(key)
			writeString(map[key])
			
func readIntStringMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readInt()
			var value = readString()
			map[key] = value
	return map


func writeIntPacketMap(map, protocolId):
	if (map == null):
		writeInt(0)
	else:
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		writeInt(map.size())
		for key in map:
			writeInt(key)
			protocolRegistration.write(self, map[key])

func readIntPacketMap(protocolId):
	var map = {}
	var size = readInt()
	if (size > 0):
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		for index in range(size):
			var key = readInt()
			var value = protocolRegistration.read(self)
			map[key] = value
	return map
	
	
func writeLongIntMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeLong(key)
			writeInt(map[key])
			
func readLongIntMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readLong()
			var value = readInt()
			map[key] = value
	return map
	
func writeLongLongMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeLong(key)
			writeLong(map[key])
			
func readLongLongMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readLong()
			var value = readLong()
			map[key] = value
	return map
	
func writeLongStringMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeLong(key)
			writeString(map[key])
			
func readLongStringMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readLong()
			var value = readString()
			map[key] = value
	return map


func writeLongPacketMap(map, protocolId):
	if (map == null):
		writeInt(0)
	else:
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		writeInt(map.size())
		for key in map:
			writeLong(key)
			protocolRegistration.write(self, map[key])

func readLongPacketMap(protocolId):
	var map = {}
	var size = readInt()
	if (size > 0):
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		for index in range(size):
			var key = readLong()
			var value = protocolRegistration.read(self)
			map[key] = value
	return map
	

func writeStringIntMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeString(key)
			writeInt(map[key])
			
func readStringIntMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readString()
			var value = readInt()
			map[key] = value
	return map
	
func writeStringLongMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeString(key)
			writeLong(map[key])
			
func readStringLongMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readString()
			var value = readLong()
			map[key] = value
	return map
	
func writeStringStringMap(map):
	if (map == null):
		writeInt(0)
	else:
		writeInt(map.size())
		for key in map:
			writeString(key)
			writeString(map[key])
			
func readStringStringMap():
	var map = {}
	var size = readInt()
	if (size > 0):
		for index in range(size):
			var key = readString()
			var value = readString()
			map[key] = value
	return map


func writeStringPacketMap(map, protocolId):
	if (map == null):
		writeInt(0)
	else:
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		writeInt(map.size())
		for key in map:
			writeString(key)
			protocolRegistration.write(self, map[key])

func readStringPacketMap(protocolId):
	var map = {}
	var size = readInt()
	if (size > 0):
		var protocolRegistration = ProtocolManager.getProtocol(protocolId)
		for index in range(size):
			var key = readString()
			var value = protocolRegistration.read(self)
			map[key] = value
	return map
