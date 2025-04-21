class_name NormalObject
const PROTOCOL_ID: int = 101
# 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
var a: int
var aaa: Array[int]
var b: int
# 整数类型
var c: int
var d: int
var e: float
var f: float
var g: bool
var jj: String
var kk: ObjectA
var l: Array[int]
var ll: Array[int]
var lll: Array[ObjectA]
var llll: Array[String]
var m: Dictionary[int, String]
var mm: Dictionary[int, ObjectA]
var s: Array[int]
var ssss: Array[String]

static func write(buffer: ByteBuffer, packet: NormalObject):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeByte(packet.a)
	buffer.writeByteArray(packet.aaa)
	buffer.writeShort(packet.b)
	buffer.writeInt(packet.c)
	buffer.writeLong(packet.d)
	buffer.writeFloat(packet.e)
	buffer.writeDouble(packet.f)
	buffer.writeBool(packet.g)
	buffer.writeString(packet.jj)
	buffer.writePacket(packet.kk, ObjectA)
	buffer.writeIntArray(packet.l)
	buffer.writeLongArray(packet.ll)
	buffer.writePacketArray(packet.lll, ObjectA)
	buffer.writeStringArray(packet.llll)
	buffer.writeIntStringMap(packet.m)
	buffer.writeIntPacketMap(packet.mm, ObjectA)
	buffer.writeIntArray(packet.s)
	buffer.writeStringArray(packet.ssss)
	pass

static func read(buffer: ByteBuffer) -> NormalObject:
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet: NormalObject = NormalObject.new()
	var result0 = buffer.readByte()
	packet.a = result0
	var array1 = buffer.readByteArray()
	packet.aaa = array1
	var result2 = buffer.readShort()
	packet.b = result2
	var result3 = buffer.readInt()
	packet.c = result3
	var result4 = buffer.readLong()
	packet.d = result4
	var result5 = buffer.readFloat()
	packet.e = result5
	var result6 = buffer.readDouble()
	packet.f = result6
	var result7 = buffer.readBool() 
	packet.g = result7
	var result8 = buffer.readString()
	packet.jj = result8
	var result9 = buffer.readPacket(ObjectA)
	packet.kk = result9
	var list10 = buffer.readIntArray()
	packet.l = list10
	var list11 = buffer.readLongArray()
	packet.ll = list11
	var list12 = buffer.readPacketArray(ObjectA)
	packet.lll = list12
	var list13 = buffer.readStringArray()
	packet.llll = list13
	var map14 = buffer.readIntStringMap()
	packet.m = map14
	var map15 = buffer.readIntPacketMap(ObjectA)
	packet.mm = map15
	var set16 = buffer.readIntArray()
	packet.s = set16
	var set17 = buffer.readStringArray()
	packet.ssss = set17
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet