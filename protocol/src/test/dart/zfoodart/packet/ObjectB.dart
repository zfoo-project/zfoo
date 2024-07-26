import '../IProtocolRegistration.dart';
import '../IByteBuffer.dart';

class ObjectB {
  bool flag = false;
  int innerCompatibleValue = 0;
}

class ObjectBRegistration implements IProtocolRegistration<ObjectB> {
  @override
  int protocolId() {
    return 103;
  }

  @override
  void write(IByteBuffer buffer, ObjectB? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    var beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(4);
    buffer.writeBool(packet.flag);
    buffer.writeInt(packet.innerCompatibleValue);
    buffer.adjustPadding(4, beforeWriteIndex);
  }


  @override
  ObjectB read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = ObjectB();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    var result0 = buffer.readBool();
    packet.flag = result0;
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        var result1 = buffer.readInt();
        packet.innerCompatibleValue = result1;
    }
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}