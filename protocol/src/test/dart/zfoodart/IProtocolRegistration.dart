import './IByteBuffer.dart';

abstract class IProtocolRegistration<T> {

  int protocolId();

  void write(IByteBuffer buffer, T? packet);

  T read(IByteBuffer buffer);

}