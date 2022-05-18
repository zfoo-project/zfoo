# @author jaysunxiao
# @version 3.0

var a # byte
var aaa # byte[]
var b # short
var c # int
var d # long
var e # float
var f # double
var g # boolean
var jj # java.lang.String
var kk # com.zfoo.protocol.packet.ObjectA
var l # java.util.List<java.lang.Integer>
var ll # java.util.List<java.lang.Long>
var lll # java.util.List<com.zfoo.protocol.packet.ObjectA>
var llll # java.util.List<java.lang.String>
var m # java.util.Map<java.lang.Integer, java.lang.String>
var mm # java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
var s # java.util.Set<java.lang.Integer>
var ssss # java.util.Set<java.lang.String>

const PROTOCOL_ID = 101

static func write(buffer, packet):
	if (buffer.writePacketFlag(packet)):
		return
	buffer.writeByte(packet.a)
	buffer.writeByteArray(packet.aaa)
	buffer.writeShort(packet.b)
	buffer.writeInt(packet.c)
	buffer.writeLong(packet.d)
	buffer.writeFloat(packet.e)
	buffer.writeDouble(packet.f)
	buffer.writeBool(packet.g)
	buffer.writeString(packet.jj)
	buffer.writePacket(packet.kk, 102)
	buffer.writeIntArray(packet.l)
	buffer.writeLongArray(packet.ll)
	buffer.writePacketArray(packet.lll, 102)
	buffer.writeStringArray(packet.llll)
	buffer.writeIntStringMap(packet.m)
	buffer.writeIntPacketMap(packet.mm, 102)
	buffer.writeIntArray(packet.s)
	buffer.writeStringArray(packet.ssss)


static func read(buffer):
	if (!buffer.readBool()):
		return null
	var packet = buffer.newInstance(PROTOCOL_ID)
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
	var result9 = buffer.readPacket(102)
	packet.kk = result9
	var list10 = buffer.readIntArray()
	packet.l = list10
	var list11 = buffer.readLongArray()
	packet.ll = list11
	var list12 = buffer.readPacketArray(102)
	packet.lll = list12
	var list13 = buffer.readStringArray()
	packet.llll = list13
	var map14 = buffer.readIntStringMap()
	packet.m = map14
	var map15 = buffer.readIntPacketMap(102)
	packet.mm = map15
	var set16 = buffer.readIntArray()
	packet.s = set16
	var set17 = buffer.readStringArray()
	packet.ssss = set17
	return packet
