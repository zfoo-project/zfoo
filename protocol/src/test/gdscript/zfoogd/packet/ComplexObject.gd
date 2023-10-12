const PROTOCOL_ID = 100
const PROTOCOL_CLASS_NAME = "ComplexObject"
const ObjectA = preload("res://zfoogd/packet/ObjectA.gd")
const ObjectB = preload("res://zfoogd/packet/ObjectB.gd")

# 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
# byte类型，最简单的整形
var a: int
# byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
var aa: int
# 数组类型
var aaa: Array[int]
var aaaa: Array[int]
var b: int
var bb: int
var bbb: Array[int]
var bbbb: Array[int]
var c: int
var cc: int
var ccc: Array[int]
var cccc: Array[int]
var d: int
var dd: int
var ddd: Array[int]
var dddd: Array[int]
var e: float
var ee: float
var eee: Array[float]
var eeee: Array[float]
var f: float
var ff: float
var fff: Array[float]
var ffff: Array[float]
var g: bool
var gg: bool
var ggg: Array[bool]
var gggg: Array[bool]
var jj: String
var jjj: Array[String]
var kk: ObjectA
var kkk: Array[ObjectA]
var l: Array[int]
var ll: Array	# Array<Array<Array<number>>>
var lll: Array	# Array<Array<ObjectA>>
var llll: Array[String]
var lllll: Array	# Array<Map<number, string>>
var m: Dictionary	# Map<number, string>
var mm: Dictionary	# Map<number, ObjectA>
var mmm: Dictionary	# Map<ObjectA, Array<number>>
var mmmm: Dictionary	# Map<Array<Array<ObjectA>>, Array<Array<Array<number>>>>
var mmmmm: Dictionary	# Map<Array<Map<number, string>>, Set<Map<number, string>>>
var s: Array[int]
var ss: Array	# Set<Set<Array<number>>>
var sss: Array	# Set<Set<ObjectA>>
var ssss: Array[String]
var sssss: Array	# Set<Map<number, string>>
# 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
var myCompatible: int
var myObject: ObjectA

func _to_string() -> String:
	const jsonTemplate = "{a:{}, aa:{}, aaa:{}, aaaa:{}, b:{}, bb:{}, bbb:{}, bbbb:{}, c:{}, cc:{}, ccc:{}, cccc:{}, d:{}, dd:{}, ddd:{}, dddd:{}, e:{}, ee:{}, eee:{}, eeee:{}, f:{}, ff:{}, fff:{}, ffff:{}, g:{}, gg:{}, ggg:{}, gggg:{}, jj:'{}', jjj:{}, kk:{}, kkk:{}, l:{}, ll:{}, lll:{}, llll:{}, lllll:{}, m:{}, mm:{}, mmm:{}, mmmm:{}, mmmmm:{}, s:{}, ss:{}, sss:{}, ssss:{}, sssss:{}, myCompatible:{}, myObject:{}}"
	var params = [self.a, self.aa, JSON.stringify(self.aaa), JSON.stringify(self.aaaa), self.b, self.bb, JSON.stringify(self.bbb), JSON.stringify(self.bbbb), self.c, self.cc, JSON.stringify(self.ccc), JSON.stringify(self.cccc), self.d, self.dd, JSON.stringify(self.ddd), JSON.stringify(self.dddd), self.e, self.ee, JSON.stringify(self.eee), JSON.stringify(self.eeee), self.f, self.ff, JSON.stringify(self.fff), JSON.stringify(self.ffff), self.g, self.gg, JSON.stringify(self.ggg), JSON.stringify(self.gggg), self.jj, JSON.stringify(self.jjj), self.kk, JSON.stringify(self.kkk), JSON.stringify(self.l), JSON.stringify(self.ll), JSON.stringify(self.lll), JSON.stringify(self.llll), JSON.stringify(self.lllll), JSON.stringify(self.m), JSON.stringify(self.mm), JSON.stringify(self.mmm), JSON.stringify(self.mmmm), JSON.stringify(self.mmmmm), JSON.stringify(self.s), JSON.stringify(self.ss), JSON.stringify(self.sss), JSON.stringify(self.ssss), JSON.stringify(self.sssss), self.myCompatible, self.myObject]
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	var beforeWriteIndex = buffer.getWriteOffset()
	buffer.writeInt(36962)
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
	buffer.adjustPadding(36962, beforeWriteIndex)
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
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
	var result47 = buffer.readString()
	packet.jj = result47
	var array48 = buffer.readStringArray()
	packet.jjj = array48
	var result49 = buffer.readPacket(102)
	packet.kk = result49
	var array50 = buffer.readPacketArray(102)
	packet.kkk = array50
	var list51 = buffer.readIntArray()
	packet.l = list51
	var result52 = []
	var size54 = buffer.readInt()
	if (size54 > 0):
		for index53 in range(size54):
			var result55 = []
			var size57 = buffer.readInt()
			if (size57 > 0):
				for index56 in range(size57):
					var list58 = buffer.readIntArray()
					result55.append(list58)
			result52.append(result55)
	packet.ll = result52
	var result59 = []
	var size61 = buffer.readInt()
	if (size61 > 0):
		for index60 in range(size61):
			var list62 = buffer.readPacketArray(102)
			result59.append(list62)
	packet.lll = result59
	var list63 = buffer.readStringArray()
	packet.llll = list63
	var result64 = []
	var size66 = buffer.readInt()
	if (size66 > 0):
		for index65 in range(size66):
			var map67 = buffer.readIntStringMap()
			result64.append(map67)
	packet.lllll = result64
	var map68 = buffer.readIntStringMap()
	packet.m = map68
	var map69 = buffer.readIntPacketMap(102)
	packet.mm = map69
	var result70 = {}
	var size71 = buffer.readInt()
	if (size71 > 0):
		for index72 in range(size71):
			var result73 = buffer.readPacket(102)
			var list74 = buffer.readIntArray()
			result70[result73] = list74
	packet.mmm = result70
	var result75 = {}
	var size76 = buffer.readInt()
	if (size76 > 0):
		for index77 in range(size76):
			var result78 = []
			var size80 = buffer.readInt()
			if (size80 > 0):
				for index79 in range(size80):
					var list81 = buffer.readPacketArray(102)
					result78.append(list81)
			var result82 = []
			var size84 = buffer.readInt()
			if (size84 > 0):
				for index83 in range(size84):
					var result85 = []
					var size87 = buffer.readInt()
					if (size87 > 0):
						for index86 in range(size87):
							var list88 = buffer.readIntArray()
							result85.append(list88)
					result82.append(result85)
			result75[result78] = result82
	packet.mmmm = result75
	var result89 = {}
	var size90 = buffer.readInt()
	if (size90 > 0):
		for index91 in range(size90):
			var result92 = []
			var size94 = buffer.readInt()
			if (size94 > 0):
				for index93 in range(size94):
					var map95 = buffer.readIntStringMap()
					result92.append(map95)
			var result96 = []
			var size98 = buffer.readInt()
			if (size98 > 0):
				for index97 in range(size98):
					var map99 = buffer.readIntStringMap()
					result96.append(map99)
			result89[result92] = result96
	packet.mmmmm = result89
	var set100 = buffer.readIntArray()
	packet.s = set100
	var result101 = []
	var size103 = buffer.readInt()
	if (size103 > 0):
		for index102 in range(size103):
			var result104 = []
			var size106 = buffer.readInt()
			if (size106 > 0):
				for index105 in range(size106):
					var list107 = buffer.readIntArray()
					result104.append(list107)
			result101.append(result104)
	packet.ss = result101
	var result108 = []
	var size110 = buffer.readInt()
	if (size110 > 0):
		for index109 in range(size110):
			var set111 = buffer.readPacketArray(102)
			result108.append(set111)
	packet.sss = result108
	var set112 = buffer.readStringArray()
	packet.ssss = set112
	var result113 = []
	var size115 = buffer.readInt()
	if (size115 > 0):
		for index114 in range(size115):
			var map116 = buffer.readIntStringMap()
			result113.append(map116)
	packet.sssss = result113
	if buffer.compatibleRead(beforeReadIndex, length):
		var result117 = buffer.readInt()
		packet.myCompatible = result117;
	if buffer.compatibleRead(beforeReadIndex, length):
		var result118 = buffer.readPacket(102)
		packet.myObject = result118;
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
