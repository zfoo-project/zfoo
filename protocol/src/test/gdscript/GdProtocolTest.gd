extends Node2D


const ProtocolManager = preload("res://gdProtocol/ProtocolManager.gd")
const ByteBuffer = preload("res://gdProtocol/buffer/ByteBuffer.gd")
const FileUtils = preload("res://zfoo/util/FileUtils.gd")

func _ready():
	var buffer = ByteBuffer.new()
	
	buffer.writeBool(true)
	buffer.writeBool(false)
	assert(true == buffer.readBool())
	assert(false == buffer.readBool())
	
	buffer.writeByte(-128)
	buffer.writeByte(99)
	buffer.writeByte(127)
	assert(-128 == buffer.readByte())
	assert(99 == buffer.readByte())
	assert(127 == buffer.readByte())

	buffer.writeShort(-32768)
	buffer.writeShort(0)
	buffer.writeShort(32767)
	assert(-32768 == buffer.readShort())
	assert(0 == buffer.readShort())
	assert(32767 == buffer.readShort())
	
	buffer.writeInt(-2147483648)
	buffer.writeInt(-999999)
	buffer.writeInt(0)
	buffer.writeInt(999999)
	buffer.writeInt(2147483647)
	assert(-2147483648 == buffer.readInt())
	assert(-999999 == buffer.readInt())
	assert(0 == buffer.readInt())
	assert(999999 == buffer.readInt())
	assert(2147483647 == buffer.readInt())
	
	var maxLong = int("9223372036854775807")
	var minLong = int("-9223372036854775808")
	buffer.writeLong(maxLong)
	buffer.writeLong(9999999999999999)
	buffer.writeLong(99999999)
	buffer.writeLong(0)
	buffer.writeLong(-99999999)
	buffer.writeLong(-9999999999999999)
	buffer.writeLong(minLong)

	assert(maxLong == buffer.readLong())
	assert(9999999999999999 == buffer.readLong())
	assert(99999999 == buffer.readLong())
	assert(0 == buffer.readLong())
	assert(-99999999 == buffer.readLong())
	assert(-9999999999999999 == buffer.readLong())
	assert(minLong == buffer.readLong())
	
	buffer.writeFloat(-1234.5678)
	buffer.writeFloat(0)
	buffer.writeFloat(1234.5678)
	assert(abs(-1234.5678 - buffer.readFloat()) < 0.001)
	assert(0 == buffer.readFloat())
	assert(abs(1234.5678 - buffer.readFloat()) < 0.001)

	buffer.writeDouble(-1234.5678)
	buffer.writeDouble(0)
	buffer.writeDouble(1234.5678)
	assert(abs(-1234.5678 - buffer.readDouble()) < 0.001)
	assert(0 == buffer.readDouble())
	assert(abs(1234.5678 - buffer.readDouble()) < 0.001)
	
	var strValue = "你好 hello world"
	buffer.writeString(strValue)
	assert(strValue == buffer.readString())
	
	buffer.writeChar(strValue)
	assert(strValue[0] == buffer.readChar())
	buffer.writeChar("")
	assert(ByteBuffer.EMPTY == buffer.readChar())
	test()


func test():
	ProtocolManager.initProtocol()
	var buffer = ByteBuffer.new()
	var poolByteArray = FileUtils.readFileToByteArray("user://ComplexObject.bytes")
	buffer.writePoolByteArray(poolByteArray)
	
	var packet = ProtocolManager.read(buffer)
	print(packet)
	packet.myCompatible = 0
	var newByteBuffer = ByteBuffer.new()
	ProtocolManager.write(newByteBuffer, packet);
	print(newByteBuffer.getWriteOffset())
	
	var newPacket = ProtocolManager.read(newByteBuffer);
	print(newPacket)






