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
# 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
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
	var result0 = buffer.readByte()
	packet.a = result0
	var result1 = buffer.readByte()
	packet.aa = result1
	var array2 = buffer.readByteArray()
	packet.aaa = array2
	var array3 = buffer.readByteArray()
	packet.aaaa = array3
	var result4 = buffer.readShort()
	packet.b = result4
	var result5 = buffer.readShort()
	packet.bb = result5
	var array6 = buffer.readShortArray()
	packet.bbb = array6
	var array7 = buffer.readShortArray()
	packet.bbbb = array7
	var result8 = buffer.readInt()
	packet.c = result8
	var result9 = buffer.readInt()
	packet.cc = result9
	var array10 = buffer.readIntArray()
	packet.ccc = array10
	var array11 = buffer.readIntArray()
	packet.cccc = array11
	var result12 = buffer.readLong()
	packet.d = result12
	var result13 = buffer.readLong()
	packet.dd = result13
	var array14 = buffer.readLongArray()
	packet.ddd = array14
	var array15 = buffer.readLongArray()
	packet.dddd = array15
	var result16 = buffer.readFloat()
	packet.e = result16
	var result17 = buffer.readFloat()
	packet.ee = result17
	var array18 = buffer.readFloatArray()
	packet.eee = array18
	var array19 = buffer.readFloatArray()
	packet.eeee = array19
	var result20 = buffer.readDouble()
	packet.f = result20
	var result21 = buffer.readDouble()
	packet.ff = result21
	var array22 = buffer.readDoubleArray()
	packet.fff = array22
	var array23 = buffer.readDoubleArray()
	packet.ffff = array23
	var result24 = buffer.readBool() 
	packet.g = result24
	var result25 = buffer.readBool() 
	packet.gg = result25
	var array26 = buffer.readBooleanArray()
	packet.ggg = array26
	var array27 = buffer.readBooleanArray()
	packet.gggg = array27
	var result28 = buffer.readString()
	packet.jj = result28
	var array29 = buffer.readStringArray()
	packet.jjj = array29
	var result30 = buffer.readPacket(102)
	packet.kk = result30
	var array31 = buffer.readPacketArray(102)
	packet.kkk = array31
	var list32 = buffer.readIntArray()
	packet.l = list32
	var result33 = []
	var size35 = buffer.readInt()
	if (size35 > 0):
		for index34 in range(size35):
			var result36 = []
			var size38 = buffer.readInt()
			if (size38 > 0):
				for index37 in range(size38):
					var list39 = buffer.readIntArray()
					result36.append(list39)
			result33.append(result36)
	packet.ll = result33
	var result40 = []
	var size42 = buffer.readInt()
	if (size42 > 0):
		for index41 in range(size42):
			var list43 = buffer.readPacketArray(102)
			result40.append(list43)
	packet.lll = result40
	var list44 = buffer.readStringArray()
	packet.llll = list44
	var result45 = []
	var size47 = buffer.readInt()
	if (size47 > 0):
		for index46 in range(size47):
			var map48 = buffer.readIntStringMap()
			result45.append(map48)
	packet.lllll = result45
	var map49 = buffer.readIntStringMap()
	packet.m = map49
	var map50 = buffer.readIntPacketMap(102)
	packet.mm = map50
	var result51 = {}
	var size52 = buffer.readInt()
	if (size52 > 0):
		for index53 in range(size52):
			var result54 = buffer.readPacket(102)
			var list55 = buffer.readIntArray()
			result51[result54] = list55
	packet.mmm = result51
	var result56 = {}
	var size57 = buffer.readInt()
	if (size57 > 0):
		for index58 in range(size57):
			var result59 = []
			var size61 = buffer.readInt()
			if (size61 > 0):
				for index60 in range(size61):
					var list62 = buffer.readPacketArray(102)
					result59.append(list62)
			var result63 = []
			var size65 = buffer.readInt()
			if (size65 > 0):
				for index64 in range(size65):
					var result66 = []
					var size68 = buffer.readInt()
					if (size68 > 0):
						for index67 in range(size68):
							var list69 = buffer.readIntArray()
							result66.append(list69)
					result63.append(result66)
			result56[result59] = result63
	packet.mmmm = result56
	var result70 = {}
	var size71 = buffer.readInt()
	if (size71 > 0):
		for index72 in range(size71):
			var result73 = []
			var size75 = buffer.readInt()
			if (size75 > 0):
				for index74 in range(size75):
					var map76 = buffer.readIntStringMap()
					result73.append(map76)
			var result77 = []
			var size79 = buffer.readInt()
			if (size79 > 0):
				for index78 in range(size79):
					var map80 = buffer.readIntStringMap()
					result77.append(map80)
			result70[result73] = result77
	packet.mmmmm = result70
	var set81 = buffer.readIntArray()
	packet.s = set81
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
	packet.ss = result82
	var result89 = []
	var size91 = buffer.readInt()
	if (size91 > 0):
		for index90 in range(size91):
			var set92 = buffer.readPacketArray(102)
			result89.append(set92)
	packet.sss = result89
	var set93 = buffer.readStringArray()
	packet.ssss = set93
	var result94 = []
	var size96 = buffer.readInt()
	if (size96 > 0):
		for index95 in range(size96):
			var map97 = buffer.readIntStringMap()
			result94.append(map97)
	packet.sssss = result94
	if buffer.compatibleRead(beforeReadIndex, length):
		var result98 = buffer.readInt()
		packet.myCompatible = result98;
	if buffer.compatibleRead(beforeReadIndex, length):
		var result99 = buffer.readPacket(102)
		packet.myObject = result99;
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet