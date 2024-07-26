import 'dart:typed_data';

abstract class IByteBuffer {
  void adjustPadding(int predictionLength, int beforeWriteIndex);

  bool compatibleRead(int beforeReadIndex, int length);

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

  int writeIntCount(int value);

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

  void writeBoolArray(List<bool> array);

  List<bool> readBoolArray();

  void writeByteArray(List<int> array);

  List<int> readByteArray();

  void writeShortArray(List<int> array);

  List<int> readShortArray();

  void writeIntArray(List<int> array);

  List<int> readIntArray();

  void writeLongArray(List<int> array);

  List<int> readLongArray();

  void writeFloatArray(List<double> array);

  List<double> readFloatArray();

  void writeDoubleArray(List<double> array);

  List<double> readDoubleArray();

  void writeStringArray(List<String> array);

  List<String> readStringArray();

  void writePacketArray(List<Object> array, int protocolId);

  List<T> readPacketArray<T>(int protocolId);

  void writeBoolList(List<bool> list);

  List<bool> readBoolList();

  void writeByteList(List<int> list);

  List<int> readByteList();

  void writeShortList(List<int> list);

  List<int> readShortList();

  void writeIntList(List<int> list);

  List<int> readIntList();

  void writeLongList(List<int> list);

  List<int> readLongList();

  void writeFloatList(List<double> list);

  List<double> readFloatList();

  void writeDoubleList(List<double> list);

  List<double> readDoubleList();

  void writeStringList(List<String> list);

  List<String> readStringList();

  void writePacketList(List<Object> list, int protocolId);

  List<T> readPacketList<T>(int protocolId);

  void writeBoolSet(Set<bool> set);

  Set<bool> readBoolSet();

  void writeByteSet(Set<int> set);

  Set<int> readByteSet();

  void writeShortSet(Set<int> set);

  Set<int> readShortSet();

  void writeIntSet(Set<int> set);

  Set<int> readIntSet();

  void writeLongSet(Set<int> set);

  Set<int> readLongSet();

  void writeFloatSet(Set<double> set);

  Set<double> readFloatSet();

  void writeDoubleSet(Set<double> set);

  Set<double> readDoubleSet();

  void writeStringSet(Set<String> set);

  Set<String> readStringSet();

  void writePacketSet(Set<Object>set, int protocolId);

  Set<T> readPacketSet<T>(int protocolId);

  void writeIntIntMap(Map<int, int> map);

  Map<int, int> readIntIntMap();

  void writeIntLongMap(Map<int, int> map);

  Map<int, int> readIntLongMap();

  void writeIntStringMap(Map<int, String> map);

  Map<int, String> readIntStringMap();

  void writeIntPacketMap(Map<int, Object> map, int protocolId);

  Map<int, T> readIntPacketMap<T>(int protocolId);

  void writeLongIntMap(Map<int, int> map);

  Map<int, int> readLongIntMap();

  void writeLongLongMap(Map<int, int> map);

  Map<int, int> readLongLongMap();

  void writeLongStringMap(Map<int, String> map);

  Map<int, String> readLongStringMap();

  void writeLongPacketMap(Map<int, Object> map, int protocolId);

  Map<int, T> readLongPacketMap<T>(int protocolId);

  void writeStringIntMap(Map<String, int> map);

  Map<String, int> readStringIntMap();

  void writeStringLongMap(Map<String, int> map);

  Map<String, int> readStringLongMap();

  void writeStringStringMap(Map<String, String> map);

  Map<String, String> readStringStringMap();

  void writeStringPacketMap(Map<String, Object> map, int protocolId);

  Map<String, T> readStringPacketMap<T>(int protocolId);
}
