import '../IProtocolRegistration.dart';
import '../IByteBuffer.dart';

class EmptyObject {
  
}

class EmptyObjectRegistration implements IProtocolRegistration<EmptyObject> {
  @override
  int protocolId() {
    return 0;
  }

  @override
  void write(IByteBuffer buffer, EmptyObject? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    buffer.writeInt(-1);
  }


  @override
  EmptyObject read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = EmptyObject();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}