import '../IProtocolRegistration.dart';
import '../IByteBuffer.dart';

class SimpleObject {
  int c = 0;
  bool g = false;
}

class SimpleObjectRegistration implements IProtocolRegistration<SimpleObject> {
  @override
  int protocolId() {
    return 104;
  }

  @override
  void write(IByteBuffer buffer, SimpleObject? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    buffer.writeInt(-1);
    buffer.writeInt(packet.c);
    buffer.writeBool(packet.g);
  }


  @override
  SimpleObject read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = SimpleObject();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    var result0 = buffer.readInt();
    packet.c = result0;
    var result1 = buffer.readBool();
    packet.g = result1;
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}