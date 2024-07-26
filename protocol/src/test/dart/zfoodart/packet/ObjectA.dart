import '../IProtocolRegistration.dart';
import '../IByteBuffer.dart';
import './ObjectB.dart';

class ObjectA {
  int a = 0;
  Map<int, String> m = Map();
  ObjectB? objectB = null;
  int innerCompatibleValue = 0;
}

class ObjectARegistration implements IProtocolRegistration<ObjectA> {
  @override
  int protocolId() {
    return 102;
  }

  @override
  void write(IByteBuffer buffer, ObjectA? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    var beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(201);
    buffer.writeInt(packet.a);
    buffer.writeIntStringMap(packet.m);
    buffer.writePacket(packet.objectB, 103);
    buffer.writeInt(packet.innerCompatibleValue);
    buffer.adjustPadding(201, beforeWriteIndex);
  }


  @override
  ObjectA read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = ObjectA();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    var result0 = buffer.readInt();
    packet.a = result0;
    var map1 = buffer.readIntStringMap();
    packet.m = map1;
    var result2 = buffer.readPacket(103) as ObjectB;
    packet.objectB = result2;
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        var result3 = buffer.readInt();
        packet.innerCompatibleValue = result3;
    }
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}