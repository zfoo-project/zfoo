# 复杂的对象
# 包括了各种复杂的结构，数组，List，Set，Map
#
# @author jaysunxiao
# @version 3.0

# byte类型，最简单的整形
var a # byte
# byte的包装类型
# 优先使用基础类型，包装类型会有装箱拆箱
var aa # java.lang.Byte
# 数组类型
var aaa # byte[]
var aaaa # java.lang.Byte[]
var b # short
var bb # java.lang.Short
var bbb # short[]
var bbbb # java.lang.Short[]
var c # int
var cc # java.lang.Integer
var ccc # int[]
var cccc # java.lang.Integer[]
var d # long
var dd # java.lang.Long
var ddd # long[]
var dddd # java.lang.Long[]
var e # float
var ee # java.lang.Float
var eee # float[]
var eeee # java.lang.Float[]
var f # double
var ff # java.lang.Double
var fff # double[]
var ffff # java.lang.Double[]
var g # boolean
var gg # java.lang.Boolean
var ggg # boolean[]
var gggg # java.lang.Boolean[]
var h # char
var hh # java.lang.Character
var hhh # char[]
var hhhh # java.lang.Character[]
var jj # java.lang.String
var jjj # java.lang.String[]
var kk # com.zfoo.protocol.packet.ObjectA
var kkk # com.zfoo.protocol.packet.ObjectA[]
var l # java.util.List<java.lang.Integer>
var ll # java.util.List<java.util.List<java.util.List<java.lang.Integer>>>
var lll # java.util.List<java.util.List<com.zfoo.protocol.packet.ObjectA>>
var llll # java.util.List<java.lang.String>
var lllll # java.util.List<java.util.Map<java.lang.Integer, java.lang.String>>
var m # java.util.Map<java.lang.Integer, java.lang.String>
var mm # java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
var mmm # java.util.Map<com.zfoo.protocol.packet.ObjectA, java.util.List<java.lang.Integer>>
var mmmm # java.util.Map<java.util.List<java.util.List<com.zfoo.protocol.packet.ObjectA>>, java.util.List<java.util.List<java.util.List<java.lang.Integer>>>>
var mmmmm # java.util.Map<java.util.List<java.util.Map<java.lang.Integer, java.lang.String>>, java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>>
var s # java.util.Set<java.lang.Integer>
var ss # java.util.Set<java.util.Set<java.util.List<java.lang.Integer>>>
var sss # java.util.Set<java.util.Set<com.zfoo.protocol.packet.ObjectA>>
var ssss # java.util.Set<java.lang.String>
var sssss # java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>
# 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
var myCompatible # int
var myObject # com.zfoo.protocol.packet.ObjectA

const PROTOCOL_ID = 100

static func write(buffer, packet):
	if (buffer.writePacketFlag(packet)):
		return
	buffer.writeByte(packet.a)
	buffer.writeByte(packet.aa)
	buffer.writeByteArray(packet.aaa)
	buffer.writeByteArray(packet.aaaa)
	buffer.writeShort(packet.b)
	buffer.writeShort(packet.bb)
	buffer.writeShortArray(packet.bbb)
	buffer.writeShortArray(packet.bbbb)
	buffer.writeInt(packet.c)
	buffer.writeInt(packet.cc)
	buffer.writeIntArray(packet.ccc)
	buffer.writeIntArray(packet.cccc)
	buffer.writeLong(packet.d)
	buffer.writeLong(packet.dd)
	buffer.writeLongArray(packet.ddd)
	buffer.writeLongArray(packet.dddd)
	buffer.writeFloat(packet.e)
	buffer.writeFloat(packet.ee)
	buffer.writeFloatArray(packet.eee)
	buffer.writeFloatArray(packet.eeee)
	buffer.writeDouble(packet.f)
	buffer.writeDouble(packet.ff)
	buffer.writeDoubleArray(packet.fff)
	buffer.writeDoubleArray(packet.ffff)
	buffer.writeBool(packet.g)
	buffer.writeBool(packet.gg)
	buffer.writeBooleanArray(packet.ggg)
	buffer.writeBooleanArray(packet.gggg)
	buffer.writeChar(packet.h)
	buffer.writeChar(packet.hh)
	buffer.writeCharArray(packet.hhh)
	buffer.writeCharArray(packet.hhhh)
	buffer.writeString(packet.jj)
	buffer.writeStringArray(packet.jjj)
	buffer.writePacket(packet.kk, 102)
	buffer.writePacketArray(packet.kkk, 102)
	buffer.writeIntArray(packet.l)
	if (packet.ll == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.ll.size())
		for element0 in packet.ll:
			if (element0 == null):
				buffer.writeInt(0)
			else:
				buffer.writeInt(element0.size())
				for element1 in element0:
					buffer.writeIntArray(element1)
	if (packet.lll == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.lll.size())
		for element2 in packet.lll:
			buffer.writePacketArray(element2, 102)
	buffer.writeStringArray(packet.llll)
	if (packet.lllll == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.lllll.size())
		for element3 in packet.lllll:
			buffer.writeIntStringMap(element3)
	buffer.writeIntStringMap(packet.m)
	buffer.writeIntPacketMap(packet.mm, 102)
	if (packet.mmm == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.mmm.size())
		for key4 in packet.mmm:
			var value5 = packet.mmm[key4]
			buffer.writePacket(key4, 102)
			buffer.writeIntArray(value5)
	if (packet.mmmm == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.mmmm.size())
		for key6 in packet.mmmm:
			var value7 = packet.mmmm[key6]
			if (key6 == null):
				buffer.writeInt(0)
			else:
				buffer.writeInt(key6.size())
				for element8 in key6:
					buffer.writePacketArray(element8, 102)
			if (value7 == null):
				buffer.writeInt(0)
			else:
				buffer.writeInt(value7.size())
				for element9 in value7:
					if (element9 == null):
						buffer.writeInt(0)
					else:
						buffer.writeInt(element9.size())
						for element10 in element9:
							buffer.writeIntArray(element10)
	if (packet.mmmmm == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.mmmmm.size())
		for key11 in packet.mmmmm:
			var value12 = packet.mmmmm[key11]
			if (key11 == null):
				buffer.writeInt(0)
			else:
				buffer.writeInt(key11.size())
				for element13 in key11:
					buffer.writeIntStringMap(element13)
			if (value12 == null):
				buffer.writeInt(0)
			else:
				buffer.writeInt(value12.size())
				for element14 in value12:
					buffer.writeIntStringMap(element14)
	buffer.writeIntArray(packet.s)
	if (packet.ss == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.ss.size())
		for element15 in packet.ss:
			if (element15 == null):
				buffer.writeInt(0)
			else:
				buffer.writeInt(element15.size())
				for element16 in element15:
					buffer.writeIntArray(element16)
	if (packet.sss == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.sss.size())
		for element17 in packet.sss:
			buffer.writePacketArray(element17, 102)
	buffer.writeStringArray(packet.ssss)
	if (packet.sssss == null):
		buffer.writeInt(0)
	else:
		buffer.writeInt(packet.sssss.size())
		for element18 in packet.sssss:
			buffer.writeIntStringMap(element18)
	buffer.writeInt(packet.myCompatible)
	buffer.writePacket(packet.myObject, 102)


static func read(buffer):
	if (!buffer.readBool()):
		return null
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result19 = buffer.readByte()
	packet.a = result19
	var result20 = buffer.readByte()
	packet.aa = result20
	var array21 = buffer.readByteArray()
	packet.aaa = array21
	var array22 = buffer.readByteArray()
	packet.aaaa = array22
	var result23 = buffer.readShort()
	packet.b = result23
	var result24 = buffer.readShort()
	packet.bb = result24
	var array25 = buffer.readShortArray()
	packet.bbb = array25
	var array26 = buffer.readShortArray()
	packet.bbbb = array26
	var result27 = buffer.readInt()
	packet.c = result27
	var result28 = buffer.readInt()
	packet.cc = result28
	var array29 = buffer.readIntArray()
	packet.ccc = array29
	var array30 = buffer.readIntArray()
	packet.cccc = array30
	var result31 = buffer.readLong()
	packet.d = result31
	var result32 = buffer.readLong()
	packet.dd = result32
	var array33 = buffer.readLongArray()
	packet.ddd = array33
	var array34 = buffer.readLongArray()
	packet.dddd = array34
	var result35 = buffer.readFloat()
	packet.e = result35
	var result36 = buffer.readFloat()
	packet.ee = result36
	var array37 = buffer.readFloatArray()
	packet.eee = array37
	var array38 = buffer.readFloatArray()
	packet.eeee = array38
	var result39 = buffer.readDouble()
	packet.f = result39
	var result40 = buffer.readDouble()
	packet.ff = result40
	var array41 = buffer.readDoubleArray()
	packet.fff = array41
	var array42 = buffer.readDoubleArray()
	packet.ffff = array42
	var result43 = buffer.readBool() 
	packet.g = result43
	var result44 = buffer.readBool() 
	packet.gg = result44
	var array45 = buffer.readBooleanArray()
	packet.ggg = array45
	var array46 = buffer.readBooleanArray()
	packet.gggg = array46
	var result47 = buffer.readChar()
	packet.h = result47
	var result48 = buffer.readChar()
	packet.hh = result48
	var array49 = buffer.readCharArray()
	packet.hhh = array49
	var array50 = buffer.readCharArray()
	packet.hhhh = array50
	var result51 = buffer.readString()
	packet.jj = result51
	var array52 = buffer.readStringArray()
	packet.jjj = array52
	var result53 = buffer.readPacket(102)
	packet.kk = result53
	var array54 = buffer.readPacketArray(102)
	packet.kkk = array54
	var list55 = buffer.readIntArray()
	packet.l = list55
	var result56 = []
	var size58 = buffer.readInt()
	if (size58 > 0):
		for index57 in range(size58):
			var result59 = []
			var size61 = buffer.readInt()
			if (size61 > 0):
				for index60 in range(size61):
					var list62 = buffer.readIntArray()
					result59.append(list62)
			result56.append(result59)
	packet.ll = result56
	var result63 = []
	var size65 = buffer.readInt()
	if (size65 > 0):
		for index64 in range(size65):
			var list66 = buffer.readPacketArray(102)
			result63.append(list66)
	packet.lll = result63
	var list67 = buffer.readStringArray()
	packet.llll = list67
	var result68 = []
	var size70 = buffer.readInt()
	if (size70 > 0):
		for index69 in range(size70):
			var map71 = buffer.readIntStringMap()
			result68.append(map71)
	packet.lllll = result68
	var map72 = buffer.readIntStringMap()
	packet.m = map72
	var map73 = buffer.readIntPacketMap(102)
	packet.mm = map73
	var result74 = {}
	var size75 = buffer.readInt()
	if (size75 > 0):
		for index76 in range(size75):
			var result77 = buffer.readPacket(102)
			var list78 = buffer.readIntArray()
			result74[result77] = list78
	packet.mmm = result74
	var result79 = {}
	var size80 = buffer.readInt()
	if (size80 > 0):
		for index81 in range(size80):
			var result82 = []
			var size84 = buffer.readInt()
			if (size84 > 0):
				for index83 in range(size84):
					var list85 = buffer.readPacketArray(102)
					result82.append(list85)
			var result86 = []
			var size88 = buffer.readInt()
			if (size88 > 0):
				for index87 in range(size88):
					var result89 = []
					var size91 = buffer.readInt()
					if (size91 > 0):
						for index90 in range(size91):
							var list92 = buffer.readIntArray()
							result89.append(list92)
					result86.append(result89)
			result79[result82] = result86
	packet.mmmm = result79
	var result93 = {}
	var size94 = buffer.readInt()
	if (size94 > 0):
		for index95 in range(size94):
			var result96 = []
			var size98 = buffer.readInt()
			if (size98 > 0):
				for index97 in range(size98):
					var map99 = buffer.readIntStringMap()
					result96.append(map99)
			var result100 = []
			var size102 = buffer.readInt()
			if (size102 > 0):
				for index101 in range(size102):
					var map103 = buffer.readIntStringMap()
					result100.append(map103)
			result93[result96] = result100
	packet.mmmmm = result93
	var set104 = buffer.readIntArray()
	packet.s = set104
	var result105 = []
	var size107 = buffer.readInt()
	if (size107 > 0):
		for index106 in range(size107):
			var result108 = []
			var size110 = buffer.readInt()
			if (size110 > 0):
				for index109 in range(size110):
					var list111 = buffer.readIntArray()
					result108.append(list111)
			result105.append(result108)
	packet.ss = result105
	var result112 = []
	var size114 = buffer.readInt()
	if (size114 > 0):
		for index113 in range(size114):
			var set115 = buffer.readPacketArray(102)
			result112.append(set115)
	packet.sss = result112
	var set116 = buffer.readStringArray()
	packet.ssss = set116
	var result117 = []
	var size119 = buffer.readInt()
	if (size119 > 0):
		for index118 in range(size119):
			var map120 = buffer.readIntStringMap()
			result117.append(map120)
	packet.sssss = result117
	if (!buffer.isReadable()):
		return packet
	var result121 = buffer.readInt()
	packet.myCompatible = result121
	if (!buffer.isReadable()):
		return packet
	var result122 = buffer.readPacket(102)
	packet.myObject = result122
	return packet
