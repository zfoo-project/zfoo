import '../IProtocolRegistration.dart';
import '../IByteBuffer.dart';
import './ObjectA.dart';
import './ObjectB.dart';
// 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
class ComplexObject {
  // byte类型，最简单的整形
  int a = 0;
  // byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
  int aa = 0;
  // 数组类型
  List<int> aaa = List.empty();
  List<int> aaaa = List.empty();
  int b = 0;
  int bb = 0;
  List<int> bbb = List.empty();
  List<int> bbbb = List.empty();
  int c = 0;
  int cc = 0;
  List<int> ccc = List.empty();
  List<int> cccc = List.empty();
  int d = 0;
  int dd = 0;
  List<int> ddd = List.empty();
  List<int> dddd = List.empty();
  double e = 0.0;
  double ee = 0.0;
  List<double> eee = List.empty();
  List<double> eeee = List.empty();
  double f = 0.0;
  double ff = 0.0;
  List<double> fff = List.empty();
  List<double> ffff = List.empty();
  bool g = false;
  bool gg = false;
  List<bool> ggg = List.empty();
  List<bool> gggg = List.empty();
  String jj = "";
  List<String> jjj = List.empty();
  ObjectA? kk = null;
  List<ObjectA> kkk = List.empty();
  List<int> l = List.empty();
  List<List<List<int>>> ll = List.empty();
  List<List<ObjectA>> lll = List.empty();
  List<String> llll = List.empty();
  List<Map<int, String>> lllll = List.empty();
  Map<int, String> m = Map();
  Map<int, ObjectA> mm = Map();
  Map<ObjectA, List<int>> mmm = Map();
  Map<List<List<ObjectA>>, List<List<List<int>>>> mmmm = Map();
  Map<List<Map<int, String>>, Set<Map<int, String>>> mmmmm = Map();
  Set<int> s = Set();
  Set<Set<List<int>>> ss = Set();
  Set<Set<ObjectA>> sss = Set();
  Set<String> ssss = Set();
  Set<Map<int, String>> sssss = Set();
  // 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
  int myCompatible = 0;
  ObjectA? myObject = null;
}

class ComplexObjectRegistration implements IProtocolRegistration<ComplexObject> {
  @override
  int protocolId() {
    return 100;
  }

  @override
  void write(IByteBuffer buffer, ComplexObject? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    var beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(36962);
    buffer.writeByte(packet.a);
    buffer.writeByte(packet.aa);
    buffer.writeByteArray(packet.aaa);
    buffer.writeByteArray(packet.aaaa);
    buffer.writeShort(packet.b);
    buffer.writeShort(packet.bb);
    buffer.writeShortArray(packet.bbb);
    buffer.writeShortArray(packet.bbbb);
    buffer.writeInt(packet.c);
    buffer.writeInt(packet.cc);
    buffer.writeIntArray(packet.ccc);
    buffer.writeIntArray(packet.cccc);
    buffer.writeLong(packet.d);
    buffer.writeLong(packet.dd);
    buffer.writeLongArray(packet.ddd);
    buffer.writeLongArray(packet.dddd);
    buffer.writeFloat(packet.e);
    buffer.writeFloat(packet.ee);
    buffer.writeFloatArray(packet.eee);
    buffer.writeFloatArray(packet.eeee);
    buffer.writeDouble(packet.f);
    buffer.writeDouble(packet.ff);
    buffer.writeDoubleArray(packet.fff);
    buffer.writeDoubleArray(packet.ffff);
    buffer.writeBool(packet.g);
    buffer.writeBool(packet.gg);
    buffer.writeBoolArray(packet.ggg);
    buffer.writeBoolArray(packet.gggg);
    buffer.writeString(packet.jj);
    buffer.writeStringArray(packet.jjj);
    buffer.writePacket(packet.kk, 102);
    buffer.writePacketArray(packet.kkk, 102);
    buffer.writeIntList(packet.l);
    buffer.writeInt(packet.ll.length);
    for (var element0 in packet.ll) {
        buffer.writeInt(element0.length);
        for (var element1 in element0) {
            buffer.writeIntList(element1);
        }
    }
    buffer.writeInt(packet.lll.length);
    for (var element2 in packet.lll) {
        buffer.writePacketList(element2, 102);
    }
    buffer.writeStringList(packet.llll);
    buffer.writeInt(packet.lllll.length);
    for (var element3 in packet.lllll) {
        buffer.writeIntStringMap(element3);
    }
    buffer.writeIntStringMap(packet.m);
    buffer.writeIntPacketMap(packet.mm, 102);
    buffer.writeInt(packet.mmm.length);
    packet.mmm.forEach((keyElement4, valueElement5) {
        buffer.writePacket(keyElement4, 102);
        buffer.writeIntList(valueElement5);
    });
    buffer.writeInt(packet.mmmm.length);
    packet.mmmm.forEach((keyElement6, valueElement7) {
        buffer.writeInt(keyElement6.length);
        for (var element8 in keyElement6) {
            buffer.writePacketList(element8, 102);
        }
        buffer.writeInt(valueElement7.length);
        for (var element9 in valueElement7) {
            buffer.writeInt(element9.length);
            for (var element10 in element9) {
                buffer.writeIntList(element10);
            }
        }
    });
    buffer.writeInt(packet.mmmmm.length);
    packet.mmmmm.forEach((keyElement11, valueElement12) {
        buffer.writeInt(keyElement11.length);
        for (var element13 in keyElement11) {
            buffer.writeIntStringMap(element13);
        }
        buffer.writeInt(valueElement12.length);
        for (var i14 in valueElement12) {
            buffer.writeIntStringMap(i14);
        }
    });
    buffer.writeIntSet(packet.s);
    buffer.writeInt(packet.ss.length);
    for (var i15 in packet.ss) {
        buffer.writeInt(i15.length);
        for (var i16 in i15) {
            buffer.writeIntList(i16);
        }
    }
    buffer.writeInt(packet.sss.length);
    for (var i17 in packet.sss) {
        buffer.writePacketSet(i17, 102);
    }
    buffer.writeStringSet(packet.ssss);
    buffer.writeInt(packet.sssss.length);
    for (var i18 in packet.sssss) {
        buffer.writeIntStringMap(i18);
    }
    buffer.writeInt(packet.myCompatible);
    buffer.writePacket(packet.myObject, 102);
    buffer.adjustPadding(36962, beforeWriteIndex);
  }


  @override
  ComplexObject read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = ComplexObject();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    var result0 = buffer.readByte();
    packet.a = result0;
    var result1 = buffer.readByte();
    packet.aa = result1;
    var array2 = buffer.readByteArray();
    packet.aaa = array2;
    var array3 = buffer.readByteArray();
    packet.aaaa = array3;
    var result4 = buffer.readShort();
    packet.b = result4;
    var result5 = buffer.readShort();
    packet.bb = result5;
    var array6 = buffer.readShortArray();
    packet.bbb = array6;
    var array7 = buffer.readShortArray();
    packet.bbbb = array7;
    var result8 = buffer.readInt();
    packet.c = result8;
    var result9 = buffer.readInt();
    packet.cc = result9;
    var array10 = buffer.readIntArray();
    packet.ccc = array10;
    var array11 = buffer.readIntArray();
    packet.cccc = array11;
    var result12 = buffer.readLong();
    packet.d = result12;
    var result13 = buffer.readLong();
    packet.dd = result13;
    var array14 = buffer.readLongArray();
    packet.ddd = array14;
    var array15 = buffer.readLongArray();
    packet.dddd = array15;
    var result16 = buffer.readFloat();
    packet.e = result16;
    var result17 = buffer.readFloat();
    packet.ee = result17;
    var array18 = buffer.readFloatArray();
    packet.eee = array18;
    var array19 = buffer.readFloatArray();
    packet.eeee = array19;
    var result20 = buffer.readDouble();
    packet.f = result20;
    var result21 = buffer.readDouble();
    packet.ff = result21;
    var array22 = buffer.readDoubleArray();
    packet.fff = array22;
    var array23 = buffer.readDoubleArray();
    packet.ffff = array23;
    var result24 = buffer.readBool();
    packet.g = result24;
    var result25 = buffer.readBool();
    packet.gg = result25;
    var array26 = buffer.readBoolArray();
    packet.ggg = array26;
    var array27 = buffer.readBoolArray();
    packet.gggg = array27;
    var result28 = buffer.readString();
    packet.jj = result28;
    var array29 = buffer.readStringArray();
    packet.jjj = array29;
    var result30 = buffer.readPacket(102) as ObjectA;
    packet.kk = result30;
    var array31 = buffer.readPacketArray<ObjectA>(102);
    packet.kkk = array31;
    var list32 = buffer.readIntList();
    packet.l = list32;
    var size35 = buffer.readInt();
    List<List<List<int>>> result33 = List.empty(growable: true);
    if (size35 > 0) {
        for (var index34 = 0; index34 < size35; index34++) {
            var size38 = buffer.readInt();
            List<List<int>> result36 = List.empty(growable: true);
            if (size38 > 0) {
                for (var index37 = 0; index37 < size38; index37++) {
                    var list39 = buffer.readIntList();
                    result36.add(list39);
                }
            }
            result33.add(result36);
        }
    }
    packet.ll = result33;
    var size42 = buffer.readInt();
    List<List<ObjectA>> result40 = List.empty(growable: true);
    if (size42 > 0) {
        for (var index41 = 0; index41 < size42; index41++) {
            var list43 = buffer.readPacketList<ObjectA>(102);
            result40.add(list43);
        }
    }
    packet.lll = result40;
    var list44 = buffer.readStringList();
    packet.llll = list44;
    var size47 = buffer.readInt();
    List<Map<int, String>> result45 = List.empty(growable: true);
    if (size47 > 0) {
        for (var index46 = 0; index46 < size47; index46++) {
            var map48 = buffer.readIntStringMap();
            result45.add(map48);
        }
    }
    packet.lllll = result45;
    var map49 = buffer.readIntStringMap();
    packet.m = map49;
    var map50 = buffer.readIntPacketMap<ObjectA>(102);
    packet.mm = map50;
    var size52 = buffer.readInt();
    Map<ObjectA, List<int>> result51 = Map();
    if (size52 > 0) {
        for (var index53 = 0; index53 < size52; index53++) {
            var result54 = buffer.readPacket(102) as ObjectA;
            var list55 = buffer.readIntList();
            result51[result54] = list55;
        }
    }
    packet.mmm = result51;
    var size57 = buffer.readInt();
    Map<List<List<ObjectA>>, List<List<List<int>>>> result56 = Map();
    if (size57 > 0) {
        for (var index58 = 0; index58 < size57; index58++) {
            var size61 = buffer.readInt();
            List<List<ObjectA>> result59 = List.empty(growable: true);
            if (size61 > 0) {
                for (var index60 = 0; index60 < size61; index60++) {
                    var list62 = buffer.readPacketList<ObjectA>(102);
                    result59.add(list62);
                }
            }
            var size65 = buffer.readInt();
            List<List<List<int>>> result63 = List.empty(growable: true);
            if (size65 > 0) {
                for (var index64 = 0; index64 < size65; index64++) {
                    var size68 = buffer.readInt();
                    List<List<int>> result66 = List.empty(growable: true);
                    if (size68 > 0) {
                        for (var index67 = 0; index67 < size68; index67++) {
                            var list69 = buffer.readIntList();
                            result66.add(list69);
                        }
                    }
                    result63.add(result66);
                }
            }
            result56[result59] = result63;
        }
    }
    packet.mmmm = result56;
    var size71 = buffer.readInt();
    Map<List<Map<int, String>>, Set<Map<int, String>>> result70 = Map();
    if (size71 > 0) {
        for (var index72 = 0; index72 < size71; index72++) {
            var size75 = buffer.readInt();
            List<Map<int, String>> result73 = List.empty(growable: true);
            if (size75 > 0) {
                for (var index74 = 0; index74 < size75; index74++) {
                    var map76 = buffer.readIntStringMap();
                    result73.add(map76);
                }
            }
            var size79 = buffer.readInt();
            Set<Map<int, String>> result77 = Set();
            if (size79 > 0) {
                for (var index78 = 0; index78 < size79; index78++) {
                    var map80 = buffer.readIntStringMap();
                    result77.add(map80);
                }
            }
            result70[result73] = result77;
        }
    }
    packet.mmmmm = result70;
    var set81 = buffer.readIntSet();
    packet.s = set81;
    var size84 = buffer.readInt();
    Set<Set<List<int>>> result82 = Set();
    if (size84 > 0) {
        for (var index83 = 0; index83 < size84; index83++) {
            var size87 = buffer.readInt();
            Set<List<int>> result85 = Set();
            if (size87 > 0) {
                for (var index86 = 0; index86 < size87; index86++) {
                    var list88 = buffer.readIntList();
                    result85.add(list88);
                }
            }
            result82.add(result85);
        }
    }
    packet.ss = result82;
    var size91 = buffer.readInt();
    Set<Set<ObjectA>> result89 = Set();
    if (size91 > 0) {
        for (var index90 = 0; index90 < size91; index90++) {
            var set92 = buffer.readPacketSet<ObjectA>(102);
            result89.add(set92);
        }
    }
    packet.sss = result89;
    var set93 = buffer.readStringSet();
    packet.ssss = set93;
    var size96 = buffer.readInt();
    Set<Map<int, String>> result94 = Set();
    if (size96 > 0) {
        for (var index95 = 0; index95 < size96; index95++) {
            var map97 = buffer.readIntStringMap();
            result94.add(map97);
        }
    }
    packet.sssss = result94;
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        var result98 = buffer.readInt();
        packet.myCompatible = result98;
    }
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        var result99 = buffer.readPacket(102) as ObjectA;
        packet.myObject = result99;
    }
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}