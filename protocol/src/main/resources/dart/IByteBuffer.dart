import 'dart:typed_data';

abstract class IByteBuffer {
  Int8List getBuffer();

  int getWriteOffset();

  void setWriteOffset(int writeIndex);

  int getReadOffset();

  void setReadOffset(int readIndex);

  bool isReadable();

  int getCapacity();

  void ensureCapacity(int capacity);

  void writeBytes(Int8List bytes);

  Int8List readBytes(int length);

  void writeBool(bool value);

  bool readBool();

  void writeByte(int value);

  int readByte();

  void writeShort(int value);

  int readShort();

  void writeRawInt(int value);

  int readRawInt();

  void writeInt(int value);

  int writeVarInt(int value);

  int readInt();

  void writeLong(int value);

  int readLong();

  void writeFloat(double value);

  double readFloat();

  void writeDouble(double value);

  double readDouble();

  void writeString(String value);

  String readString();

  void writePacket(Object? packet, int protocolId);

  Object readPacket(int protocolId);
}
