import './IProtocolRegistration.dart';
import 'IByteBuffer.dart';
import 'packet/EmptyObject.dart';
import 'packet/VeryBigObject.dart';
import 'packet/ComplexObject.dart';
import 'packet/NormalObject.dart';
import 'packet/ObjectA.dart';
import 'packet/ObjectB.dart';
import 'packet/SimpleObject.dart';


class Protocolmanager {
  static Map<int, IProtocolRegistration> protocols = Map();
  static Map<Object, int> protocolIdMap = Map();


  static void initProtocol() {
    // initProtocol
    protocols[0] = EmptyObjectRegistration();
    protocolIdMap[EmptyObject] = 0;
    protocols[1] = VeryBigObjectRegistration();
    protocolIdMap[VeryBigObject] = 1;
    protocols[100] = ComplexObjectRegistration();
    protocolIdMap[ComplexObject] = 100;
    protocols[101] = NormalObjectRegistration();
    protocolIdMap[NormalObject] = 101;
    protocols[102] = ObjectARegistration();
    protocolIdMap[ObjectA] = 102;
    protocols[103] = ObjectBRegistration();
    protocolIdMap[ObjectB] = 103;
    protocols[104] = SimpleObjectRegistration();
    protocolIdMap[SimpleObject] = 104;
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