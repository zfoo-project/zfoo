package com.zfoo.scala.packet
import com.zfoo.scala.packet.ObjectA
import com.zfoo.scala.packet.ObjectB
import com.zfoo.scala.IProtocolRegistration
import com.zfoo.scala.ByteBuffer
import scala.collection.mutable

// 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
class ComplexObject {
  // byte类型，最简单的整形
  var a: Byte = 0
  // byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
  var aa: Byte = 0
  // 数组类型
  var aaa: Array[Byte] = _
  var aaaa: Array[Byte] = _
  var b: Short = 0
  var bb: Short = 0
  var bbb: Array[Short] = _
  var bbbb: Array[Short] = _
  var c: Int = 0
  var cc: Int = 0
  var ccc: Array[Int] = _
  var cccc: Array[Int] = _
  var d: Long = 0L
  var dd: Long = 0L
  var ddd: Array[Long] = _
  var dddd: Array[Long] = _
  var e: Float = 0f
  var ee: Float = 0f
  var eee: Array[Float] = _
  var eeee: Array[Float] = _
  var f: Double = 0D
  var ff: Double = 0D
  var fff: Array[Double] = _
  var ffff: Array[Double] = _
  var g: Boolean = false
  var gg: Boolean = false
  var ggg: Array[Boolean] = _
  var gggg: Array[Boolean] = _
  var jj: String = _
  var jjj: Array[String] = _
  var kk: ObjectA = _
  var kkk: Array[ObjectA] = _
  var l: List[Int] = _
  var ll: List[List[List[Int]]] = _
  var lll: List[List[ObjectA]] = _
  var llll: List[String] = _
  var lllll: List[Map[Int, String]] = _
  var m: Map[Int, String] = _
  var mm: Map[Int, ObjectA] = _
  var mmm: Map[ObjectA, List[Int]] = _
  var mmmm: Map[List[List[ObjectA]], List[List[List[Int]]]] = _
  var mmmmm: Map[List[Map[Int, String]], Set[Map[Int, String]]] = _
  var s: Set[Int] = _
  var ss: Set[Set[List[Int]]] = _
  var sss: Set[Set[ObjectA]] = _
  var ssss: Set[String] = _
  var sssss: Set[Map[Int, String]] = _
  // 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
  var myCompatible: Int = 0
  var myObject: ObjectA = _
}

object ComplexObjectRegistration extends IProtocolRegistration {
  override def protocolId: Short = 100

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[ComplexObject]
    val beforeWriteIndex = buffer.getWriteOffset
    buffer.writeInt(36962)
    buffer.writeByte(message.a)
    buffer.writeByte(message.aa)
    buffer.writeByteArray(message.aaa)
    buffer.writeByteArray(message.aaaa)
    buffer.writeShort(message.b)
    buffer.writeShort(message.bb)
    buffer.writeShortArray(message.bbb)
    buffer.writeShortArray(message.bbbb)
    buffer.writeInt(message.c)
    buffer.writeInt(message.cc)
    buffer.writeIntArray(message.ccc)
    buffer.writeIntArray(message.cccc)
    buffer.writeLong(message.d)
    buffer.writeLong(message.dd)
    buffer.writeLongArray(message.ddd)
    buffer.writeLongArray(message.dddd)
    buffer.writeFloat(message.e)
    buffer.writeFloat(message.ee)
    buffer.writeFloatArray(message.eee)
    buffer.writeFloatArray(message.eeee)
    buffer.writeDouble(message.f)
    buffer.writeDouble(message.ff)
    buffer.writeDoubleArray(message.fff)
    buffer.writeDoubleArray(message.ffff)
    buffer.writeBool(message.g)
    buffer.writeBool(message.gg)
    buffer.writeBooleanArray(message.ggg)
    buffer.writeBooleanArray(message.gggg)
    buffer.writeString(message.jj)
    buffer.writeStringArray(message.jjj)
    buffer.writePacket(message.kk, 102)
    buffer.writeInt(message.kkk.length)
    val length0 = message.kkk.length
    for (i1 <- 0 until length0) {
        val element2 = message.kkk(i1)
        buffer.writePacket(element2, 102)
    }
    buffer.writeIntList(message.l)
    buffer.writeInt(message.ll.size)
    for (element3 <- message.ll) {
        buffer.writeInt(element3.size)
        for (element4 <- element3) {
            buffer.writeIntList(element4)
        }
    }
    buffer.writeInt(message.lll.size)
    for (element5 <- message.lll) {
        buffer.writePacketList(element5, 102)
    }
    buffer.writeStringList(message.llll)
    buffer.writeInt(message.lllll.size)
    for (element6 <- message.lllll) {
        buffer.writeIntStringMap(element6)
    }
    buffer.writeIntStringMap(message.m)
    buffer.writeIntPacketMap(message.mm, 102)
    buffer.writeInt(message.mmm.size)
    for ((keyElement7, valueElement8) <- message.mmm) {
        buffer.writePacket(keyElement7, 102)
        buffer.writeIntList(valueElement8)
    }
    buffer.writeInt(message.mmmm.size)
    for ((keyElement9, valueElement10) <- message.mmmm) {
        buffer.writeInt(keyElement9.size)
        for (element11 <- keyElement9) {
            buffer.writePacketList(element11, 102)
        }
        buffer.writeInt(valueElement10.size)
        for (element12 <- valueElement10) {
            buffer.writeInt(element12.size)
            for (element13 <- element12) {
                buffer.writeIntList(element13)
            }
        }
    }
    buffer.writeInt(message.mmmmm.size)
    for ((keyElement14, valueElement15) <- message.mmmmm) {
        buffer.writeInt(keyElement14.size)
        for (element16 <- keyElement14) {
            buffer.writeIntStringMap(element16)
        }
        buffer.writeInt(valueElement15.size)
        for (i17 <- valueElement15) {
            buffer.writeIntStringMap(i17)
        }
    }
    buffer.writeIntSet(message.s)
    buffer.writeInt(message.ss.size)
    for (i18 <- message.ss) {
        buffer.writeInt(i18.size)
        for (i19 <- i18) {
            buffer.writeIntList(i19)
        }
    }
    buffer.writeInt(message.sss.size)
    for (i20 <- message.sss) {
        buffer.writePacketSet(i20, 102)
    }
    buffer.writeStringSet(message.ssss)
    buffer.writeInt(message.sssss.size)
    for (i21 <- message.sssss) {
        buffer.writeIntStringMap(i21)
    }
    buffer.writeInt(message.myCompatible)
    buffer.writePacket(message.myObject, 102)
    buffer.adjustPadding(36962, beforeWriteIndex)
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: ComplexObject = new ComplexObject
    val result0 = buffer.readByte
    packet.a = result0
    val result1 = buffer.readByte
    packet.aa = result1
    val array2 = buffer.readByteArray
    packet.aaa = array2
    val array3 = buffer.readByteArray
    packet.aaaa = array3
    val result4 = buffer.readShort
    packet.b = result4
    val result5 = buffer.readShort
    packet.bb = result5
    val array6 = buffer.readShortArray
    packet.bbb = array6
    val array7 = buffer.readShortArray
    packet.bbbb = array7
    val result8 = buffer.readInt
    packet.c = result8
    val result9 = buffer.readInt
    packet.cc = result9
    val array10 = buffer.readIntArray
    packet.ccc = array10
    val array11 = buffer.readIntArray
    packet.cccc = array11
    val result12 = buffer.readLong
    packet.d = result12
    val result13 = buffer.readLong
    packet.dd = result13
    val array14 = buffer.readLongArray
    packet.ddd = array14
    val array15 = buffer.readLongArray
    packet.dddd = array15
    val result16 = buffer.readFloat
    packet.e = result16
    val result17 = buffer.readFloat
    packet.ee = result17
    val array18 = buffer.readFloatArray
    packet.eee = array18
    val array19 = buffer.readFloatArray
    packet.eeee = array19
    val result20 = buffer.readDouble
    packet.f = result20
    val result21 = buffer.readDouble
    packet.ff = result21
    val array22 = buffer.readDoubleArray
    packet.fff = array22
    val array23 = buffer.readDoubleArray
    packet.ffff = array23
    val result24 = buffer.readBool
    packet.g = result24
    val result25 = buffer.readBool
    packet.gg = result25
    val array26 = buffer.readBooleanArray
    packet.ggg = array26
    val array27 = buffer.readBooleanArray
    packet.gggg = array27
    val result28 = buffer.readString
    packet.jj = result28
    val array29 = buffer.readStringArray
    packet.jjj = array29
    val result30 = buffer.readPacket(102).asInstanceOf[ObjectA]
    packet.kk = result30
    val size34 = buffer.readInt
    val result31 = new mutable.ArrayBuffer[ObjectA]()
    if (size34 > 0) {
        for (index32 <- 0 until size34) {
            val result35 = buffer.readPacket(102).asInstanceOf[ObjectA]
            result31.addOne(result35)
        }
    }
    packet.kkk = result31.toArray
    val list36 = buffer.readIntList
    packet.l = list36
    val size39 = buffer.readInt
    val result37 = new mutable.ListBuffer[List[List[Int]]]()
    if (size39 > 0) {
        for (index38 <- 0 until size39) {
            val size42 = buffer.readInt
            val result40 = new mutable.ListBuffer[List[Int]]()
            if (size42 > 0) {
                for (index41 <- 0 until size42) {
                    val list43 = buffer.readIntList
                    result40.addOne(list43)
                }
            }
            result37.addOne(result40.toList)
        }
    }
    packet.ll = result37.toList
    val size46 = buffer.readInt
    val result44 = new mutable.ListBuffer[List[ObjectA]]()
    if (size46 > 0) {
        for (index45 <- 0 until size46) {
            val list47 = buffer.readPacketList(classOf[ObjectA], 102)
            result44.addOne(list47)
        }
    }
    packet.lll = result44.toList
    val list48 = buffer.readStringList
    packet.llll = list48
    val size51 = buffer.readInt
    val result49 = new mutable.ListBuffer[Map[Int, String]]()
    if (size51 > 0) {
        for (index50 <- 0 until size51) {
            val map52 = buffer.readIntStringMap
            result49.addOne(map52)
        }
    }
    packet.lllll = result49.toList
    val map53 = buffer.readIntStringMap
    packet.m = map53
    val map54 = buffer.readIntPacketMap(classOf[ObjectA], 102)
    packet.mm = map54
    val size56 = buffer.readInt
    val result55 = new mutable.HashMap[ObjectA, List[Int]]()
    if (size56 > 0) {
        for (index57 <- 0 until size56) {
            val result58 = buffer.readPacket(102).asInstanceOf[ObjectA]
            val list59 = buffer.readIntList
            result55.put(result58, list59)
        }
    }
    packet.mmm = result55.toMap
    val size61 = buffer.readInt
    val result60 = new mutable.HashMap[List[List[ObjectA]], List[List[List[Int]]]]()
    if (size61 > 0) {
        for (index62 <- 0 until size61) {
            val size65 = buffer.readInt
            val result63 = new mutable.ListBuffer[List[ObjectA]]()
            if (size65 > 0) {
                for (index64 <- 0 until size65) {
                    val list66 = buffer.readPacketList(classOf[ObjectA], 102)
                    result63.addOne(list66)
                }
            }
            val size69 = buffer.readInt
            val result67 = new mutable.ListBuffer[List[List[Int]]]()
            if (size69 > 0) {
                for (index68 <- 0 until size69) {
                    val size72 = buffer.readInt
                    val result70 = new mutable.ListBuffer[List[Int]]()
                    if (size72 > 0) {
                        for (index71 <- 0 until size72) {
                            val list73 = buffer.readIntList
                            result70.addOne(list73)
                        }
                    }
                    result67.addOne(result70.toList)
                }
            }
            result60.put(result63.toList, result67.toList)
        }
    }
    packet.mmmm = result60.toMap
    val size75 = buffer.readInt
    val result74 = new mutable.HashMap[List[Map[Int, String]], Set[Map[Int, String]]]()
    if (size75 > 0) {
        for (index76 <- 0 until size75) {
            val size79 = buffer.readInt
            val result77 = new mutable.ListBuffer[Map[Int, String]]()
            if (size79 > 0) {
                for (index78 <- 0 until size79) {
                    val map80 = buffer.readIntStringMap
                    result77.addOne(map80)
                }
            }
            val size83 = buffer.readInt
            val result81 = new mutable.HashSet[Map[Int, String]]()
            if (size83 > 0) {
                for (index82 <- 0 until size83) {
                    val map84 = buffer.readIntStringMap
                    result81.add(map84)
                }
            }
            result74.put(result77.toList, result81.toSet)
        }
    }
    packet.mmmmm = result74.toMap
    val set85 = buffer.readIntSet
    packet.s = set85
    val size88 = buffer.readInt
    val result86 = new mutable.HashSet[Set[List[Int]]]()
    if (size88 > 0) {
        for (index87 <- 0 until size88) {
            val size91 = buffer.readInt
            val result89 = new mutable.HashSet[List[Int]]()
            if (size91 > 0) {
                for (index90 <- 0 until size91) {
                    val list92 = buffer.readIntList
                    result89.add(list92)
                }
            }
            result86.add(result89.toSet)
        }
    }
    packet.ss = result86.toSet
    val size95 = buffer.readInt
    val result93 = new mutable.HashSet[Set[ObjectA]]()
    if (size95 > 0) {
        for (index94 <- 0 until size95) {
            val set96 = buffer.readPacketSet(classOf[ObjectA], 102)
            result93.add(set96)
        }
    }
    packet.sss = result93.toSet
    val set97 = buffer.readStringSet
    packet.ssss = set97
    val size100 = buffer.readInt
    val result98 = new mutable.HashSet[Map[Int, String]]()
    if (size100 > 0) {
        for (index99 <- 0 until size100) {
            val map101 = buffer.readIntStringMap
            result98.add(map101)
        }
    }
    packet.sssss = result98.toSet
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        val result102 = buffer.readInt
        packet.myCompatible = result102
    }
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        val result103 = buffer.readPacket(102).asInstanceOf[ObjectA]
        packet.myObject = result103
    }
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}