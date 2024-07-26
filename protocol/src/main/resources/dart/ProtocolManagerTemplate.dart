import './IProtocolRegistration.dart';
import 'IByteBuffer.dart';
${protocol_imports}


class Protocolmanager {
  static Map<int, IProtocolRegistration> protocols = Map();
  static Map<Object, int> protocolIdMap = Map();


  static void initProtocol() {
    // initProtocol
    ${protocol_manager_registrations}
  }

  static int getProtocolId(Object clazz) {
    var protocolId = protocolIdMap[clazz];
    if (protocolId == null) {
      throw Exception("protocol:[$protocolId] not exist");
    }
    return protocolId;
  }

  static IProtocolRegistration getProtocol(int protocolId) {
    var protocol = protocols[protocolId];
    if (protocol == null) {
      throw Exception("protocol:[$protocolId] not exist");
    }
    return protocol;
  }

  static void write(IByteBuffer buffer, Object packet) {
    var protocolId = getProtocolId(packet.runtimeType);
    buffer.writeShort(protocolId);
    var protocol = getProtocol(protocolId);
    protocol.write(buffer, packet);
  }

  static Object read(IByteBuffer buffer) {
    var protocolId = buffer.readShort();
    var protocol = getProtocol(protocolId);
    var packet = protocol.read(buffer);
    return packet;
  }
}