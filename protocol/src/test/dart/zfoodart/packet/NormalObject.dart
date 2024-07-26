import '../IProtocolRegistration.dart';
import '../IByteBuffer.dart';
import './ObjectA.dart';
import './ObjectB.dart';
// 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject {
  int a = 0;
  List<int> aaa = List.empty();
  int b = 0;
  // 整数类型
  int c = 0;
  int d = 0;
  double e = 0.0;
  double f = 0.0;
  bool g = false;
  String jj = "";
  ObjectA? kk = null;
  List<int> l = List.empty();
  List<int> ll = List.empty();
  List<ObjectA> lll = List.empty();
  List<String> llll = List.empty();
  Map<int, String> m = Map();
  Map<int, ObjectA> mm = Map();
  Set<int> s = Set();
  Set<String> ssss = Set();
  int outCompatibleValue = 0;
  int outCompatibleValue2 = 0;
}

class NormalObjectRegistration implements IProtocolRegistration<NormalObject> {
  @override
  int protocolId() {
    return 101;
  }

  @override
  void write(IByteBuffer buffer, NormalObject? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    var beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(857);
    buffer.writeByte(packet.a);
    buffer.writeByteArray(packet.aaa);
    buffer.writeShort(packet.b);
    buffer.writeInt(packet.c);
    buffer.writeLong(packet.d);
    buffer.writeFloat(packet.e);
    buffer.writeDouble(packet.f);
    buffer.writeBool(packet.g);
    buffer.writeString(packet.jj);
    buffer.writePacket(packet.kk, 102);
    buffer.writeIntList(packet.l);
    buffer.writeLongList(packet.ll);
    buffer.writePacketList(packet.lll, 102);
    buffer.writeStringList(packet.llll);
    buffer.writeIntStringMap(packet.m);
    buffer.writeIntPacketMap(packet.mm, 102);
    buffer.writeIntSet(packet.s);
    buffer.writeStringSet(packet.ssss);
    buffer.writeInt(packet.outCompatibleValue);
    buffer.writeInt(packet.outCompatibleValue2);
    buffer.adjustPadding(857, beforeWriteIndex);
  }


  @override
  NormalObject read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = NormalObject();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    var result0 = buffer.readByte();
    packet.a = result0;
    var array1 = buffer.readByteArray();
    packet.aaa = array1;
    var result2 = buffer.readShort();
    packet.b = result2;
    var result3 = buffer.readInt();
    packet.c = result3;
    var result4 = buffer.readLong();
    packet.d = result4;
    var result5 = buffer.readFloat();
    packet.e = result5;
    var result6 = buffer.readDouble();
    packet.f = result6;
    var result7 = buffer.readBool();
    packet.g = result7;
    var result8 = buffer.readString();
    packet.jj = result8;
    var result9 = buffer.readPacket(102) as ObjectA;
    packet.kk = result9;
    var list10 = buffer.readIntList();
    packet.l = list10;
    var list11 = buffer.readLongList();
    packet.ll = list11;
    var list12 = buffer.readPacketList<ObjectA>(102);
    packet.lll = list12;
    var list13 = buffer.readStringList();
    packet.llll = list13;
    var map14 = buffer.readIntStringMap();
    packet.m = map14;
    var map15 = buffer.readIntPacketMap<ObjectA>(102);
    packet.mm = map15;
    var set16 = buffer.readIntSet();
    packet.s = set16;
    var set17 = buffer.readStringSet();
    packet.ssss = set17;
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        var result18 = buffer.readInt();
        packet.outCompatibleValue = result18;
    }
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        var result19 = buffer.readInt();
        packet.outCompatibleValue2 = result19;
    }
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}