import 'dart:convert';
import 'dart:typed_data';

import 'IByteBuffer.dart';
import 'ProtocolManager.dart';

class ByteBuffer implements IByteBuffer {
  Int8List buffer = Int8List(128);
  int writeOffset = 0;
  int readOffset = 0;

  @override
  void adjustPadding(int predictionLength, int beforeWriteIndex) {
    var currentWriteIndex = getWriteOffset();
    var predictionCount = writeIntCount(predictionLength);
    var length = currentWriteIndex - beforeWriteIndex - predictionCount;
    var lengthCount = writeIntCount(length);
    var padding = lengthCount - predictionCount;
    if (padding == 0) {
      setWriteOffset(beforeWriteIndex);
      writeInt(length);
      setWriteOffset(currentWriteIndex);
    } else {
      var retainedByteBuf =
          buffer.sublist(currentWriteIndex - length, currentWriteIndex);
      setWriteOffset(beforeWriteIndex);
      writeInt(length);
      writeBytes(retainedByteBuf);
    }
  }

  @override
  bool compatibleRead(int beforeReadIndex, int length) {
    return length != -1 && getReadOffset() < length + beforeReadIndex;
  }

  @override
  Int8List getBuffer() {
    return buffer;
  }

  @override
  int getWriteOffset() {
    return writeOffset;
  }

  @override
  void setWriteOffset(int writeIndex) {
    if (writeIndex > buffer.length) {
      throw Exception(
          "writeIndex[${writeIndex}] out of bounds exception: readOffset: [${readOffset}], writeOffset: [${writeOffset}](expected: 0 <= readOffset <= writeOffset <= capacity:${buffer.length})");
    }
    writeOffset = writeIndex;
  }

  @override
  int getReadOffset() {
    return readOffset;
  }

  @override
  void setReadOffset(int readIndex) {
    if (readIndex > writeOffset) {
      throw Exception(
          "readIndex[${readIndex}] out of bounds exception: readOffset: [${readOffset}], writeOffset: [${writeOffset}](expected: 0 <= readOffset <= writeOffset <= capacity:${buffer.length})");
    }
    readOffset = readIndex;
  }

  @override
  bool isReadable() {
    return writeOffset > readOffset;
  }

  @override
  int getCapacity() {
    return buffer.length - writeOffset;
  }

  @override
  void ensureCapacity(int capacity) {
    while (capacity - getCapacity() > 0) {
      var newSize = buffer.length * 2;
      var newBytes = Int8List(newSize);
      newBytes.setRange(0, buffer.length, buffer);
      buffer = newBytes;
    }
  }

  @override
  void writeBytes(Int8List bytes) {
    var length = bytes.length;
    ensureCapacity(length);
    buffer.setAll(writeOffset, bytes);
    writeOffset += length;
  }

  @override
  Int8List readBytes(int length) {
    var value = buffer.sublist(readOffset, readOffset + length);
    readOffset += length;
    return value;
  }

  @override
  void writeBool(bool value) {
    ensureCapacity(1);
    buffer[writeOffset++] = value ? 1 : 0;
  }

  @override
  bool readBool() {
    return buffer[readOffset++] == 1;
  }

  @override
  void writeByte(int value) {
    ensureCapacity(1);
    buffer.buffer.asByteData().setInt8(writeOffset, value);
    writeOffset += 1;
  }

  @override
  int readByte() {
    var value = buffer.buffer.asByteData().getInt8(readOffset);
    readOffset += 1;
    return value;
  }

  @override
  void writeShort(int value) {
    ensureCapacity(2);
    buffer.buffer.asByteData().setInt16(writeOffset, value);
    writeOffset += 2;
  }

  @override
  int readShort() {
    var value = buffer.buffer.asByteData().getInt16(readOffset);
    readOffset += 2;
    return value;
  }

  @override
  void writeRawInt(int value) {
    ensureCapacity(4);
    buffer.buffer.asByteData().setInt32(writeOffset, value);
    writeOffset += 4;
  }

  @override
  int readRawInt() {
    var value = buffer.buffer.asByteData().getInt32(readOffset);
    readOffset += 4;
    return value;
  }

  @override
  int writeIntCount(int value) {
    value = (value << 1) ^ (value >> 31);

    if (value >>> 7 == 0) {
      return 1;
    }

    if (value >>> 14 == 0) {
      return 2;
    }

    if (value >>> 21 == 0) {
      return 3;
    }

    if (value >>> 28 == 0) {
      return 4;
    }

    return 5;
  }

  @override
  void writeInt(int value) {
    writeLong(value);
  }

  @override
  int writeVarInt(int value) {
    int a = value >>> 7;
    if (a == 0) {
      writeByte(value);
      return 1;
    }

    ensureCapacity(5);

    writeByte(value | 0x80);
    int b = value >>> 14;
    if (b == 0) {
      writeByte(a);
      return 2;
    }

    writeByte(a | 0x80);
    a = value >>> 21;
    if (a == 0) {
      writeByte(b);
      return 3;
    }

    writeByte(b | 0x80);
    b = value >>> 28;
    if (b == 0) {
      writeByte(a);
      return 4;
    }

    writeByte(a | 0x80);
    writeByte(b);
    return 5;
  }

  @override
  int readInt() {
    return readLong();
  }

  @override
  void writeLong(int value) {
    int mask = (value << 1) ^ (value >> 63);

    if (mask >>> 32 == 0) {
      writeVarInt(mask);
      return;
    }

    writeByte(mask | 0x80);
    writeByte(mask >>> 7 | 0x80);
    writeByte(mask >>> 14 | 0x80);
    writeByte(mask >>> 21 | 0x80);

    int a = mask >>> 28;
    int b = mask >>> 35;
    if (b == 0) {
      writeByte(a);
      return;
    }

    writeByte(a | 0x80);
    a = mask >>> 42;
    if (a == 0) {
      writeByte(b);
      return;
    }

    writeByte(b | 0x80);
    b = mask >>> 49;
    if (b == 0) {
      writeByte(a);
      return;
    }

    writeByte(a | 0x80);
    a = mask >>> 56;
    if (a == 0) {
      writeByte(b);
      return;
    }

    writeByte(b | 0x80);
    writeByte(a);
  }

  @override
  int readLong() {
    int b = readByte();
    int value = b;
    if (b < 0) {
      b = readByte();
      value = value & 0x000000000000007F | b << 7;
      if (b < 0) {
        b = readByte();
        value = value & 0x0000000000003FFF | b << 14;
        if (b < 0) {
          b = readByte();
          value = value & 0x00000000001FFFFF | b << 21;
          if (b < 0) {
            b = readByte();
            value = value & 0x000000000FFFFFFF | b << 28;
            if (b < 0) {
              b = readByte();
              value = value & 0x00000007FFFFFFFF | b << 35;
              if (b < 0) {
                b = readByte();
                value = value & 0x000003FFFFFFFFFF | b << 42;
                if (b < 0) {
                  b = readByte();
                  value = value & 0x0001FFFFFFFFFFFF | b << 49;
                  if (b < 0) {
                    b = readByte();
                    value = value & 0x00FFFFFFFFFFFFFF | b << 56;
                  }
                }
              }
            }
          }
        }
      }
    }
    return ((value >>> 1) ^ -(value & 1));
  }

  @override
  void writeFloat(double value) {
    ensureCapacity(4);
    buffer.buffer.asByteData().setFloat32(writeOffset, value);
    writeOffset += 4;
  }

  @override
  double readFloat() {
    var value = buffer.buffer.asByteData().getFloat32(readOffset);
    readOffset += 4;
    return value;
  }

  @override
  void writeDouble(double value) {
    ensureCapacity(8);
    buffer.buffer.asByteData().setFloat64(writeOffset, value);
    writeOffset += 8;
  }

  @override
  double readDouble() {
    var value = buffer.buffer.asByteData().getFloat64(readOffset);
    readOffset += 8;
    return value;
  }

  @override
  void writeString(String value) {
    if (value == null || value.isEmpty) {
      writeInt(0);
      return;
    }
    Uint8List uint8list = utf8.encode(value);
    Int8List bytes = Int8List.view(uint8list.buffer);
    writeInt(bytes.length);
    writeBytes(bytes);
  }

  @override
  String readString() {
    var length = readInt();
    if (length <= 0) {
      return "";
    }
    Int8List bytes = readBytes(length);
    Uint8List uint8list = Uint8List.view(bytes.buffer);
    return utf8.decode(uint8list);
  }

  @override
  void writePacket(Object? packet, int protocolId) {
    var protocol = Protocolmanager.getProtocol(protocolId);
    protocol.write(this, packet);
  }

  @override
  Object readPacket(int protocolId) {
    var protocol = Protocolmanager.getProtocol(protocolId);
    return protocol.read(this);
  }

  @override
  void writeBoolArray(List<bool> array) {
    writeBoolList(array);
  }

  @override
  List<bool> readBoolArray() {
    return readBoolList();
  }

  @override
  void writeByteArray(List<int> array) {
    writeByteList(array);
  }

  @override
  List<int> readByteArray() {
    return readByteList();
  }

  @override
  void writeShortArray(List<int> array) {
    writeShortList(array);
  }

  @override
  List<int> readShortArray() {
    return readShortList();
  }

  @override
  void writeIntArray(List<int> array) {
    writeIntList(array);
  }

  @override
  List<int> readIntArray() {
    return readIntList();
  }

  @override
  void writeLongArray(List<int> array) {
    writeLongList(array);
  }

  @override
  List<int> readLongArray() {
    return readLongList();
  }

  @override
  void writeFloatArray(List<double> array) {
    writeFloatList(array);
  }

  @override
  List<double> readFloatArray() {
    return readFloatList();
  }

  @override
  void writeDoubleArray(List<double> array) {
    writeDoubleList(array);
  }

  @override
  List<double> readDoubleArray() {
    return readDoubleList();
  }

  @override
  void writeStringArray(List<String> array) {
    writeStringList(array);
  }

  @override
  List<String> readStringArray() {
    return readStringList();
  }

  @override
  void writePacketArray(List<Object> array, int protocolId) {
    writePacketList(array, protocolId);
  }

  @override
  List<T> readPacketArray<T>(int protocolId) {
    return readPacketList(protocolId);
  }

  @override
  void writeBoolList(List<bool> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeBool(ele);
      }
    }
  }

  @override
  List<bool> readBoolList() {
    var length = readInt();
    List<bool> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readBool());
      }
    }
    return list;
  }

  @override
  void writeByteList(List<int> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeByte(ele);
      }
    }
  }

  @override
  List<int> readByteList() {
    var length = readInt();
    List<int> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readByte());
      }
    }
    return list;
  }

  @override
  void writeShortList(List<int> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeShort(ele);
      }
    }
  }

  @override
  List<int> readShortList() {
    var length = readInt();
    List<int> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readShort());
      }
    }
    return list;
  }

  @override
  void writeIntList(List<int> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeInt(ele);
      }
    }
  }

  @override
  List<int> readIntList() {
    var length = readInt();
    List<int> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readInt());
      }
    }
    return list;
  }

  @override
  void writeLongList(List<int> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeLong(ele);
      }
    }
  }

  @override
  List<int> readLongList() {
    var length = readInt();
    List<int> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readLong());
      }
    }
    return list;
  }

  @override
  void writeFloatList(List<double> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeFloat(ele);
      }
    }
  }

  @override
  List<double> readFloatList() {
    var length = readInt();
    List<double> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readFloat());
      }
    }
    return list;
  }

  @override
  void writeDoubleList(List<double> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeDouble(ele);
      }
    }
  }

  @override
  List<double> readDoubleList() {
    var length = readInt();
    List<double> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readDouble());
      }
    }
    return list;
  }

  @override
  void writeStringList(List<String> list) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writeString(ele);
      }
    }
  }

  @override
  List<String> readStringList() {
    var length = readInt();
    List<String> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readString());
      }
    }
    return list;
  }

  @override
  void writePacketList(List<Object> list, int protocolId) {
    if (list.isEmpty) {
      writeInt(0);
    } else {
      writeInt(list.length);
      for (var ele in list) {
        writePacket(ele, protocolId);
      }
    }
  }

  @override
  List<T> readPacketList<T>(int protocolId) {
    var length = readInt();
    List<T> list = List.empty(growable: true);
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        list.add(readPacket(protocolId) as T);
      }
    }
    return list;
  }

  @override
  void writeBoolSet(Set<bool> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeBool(ele);
      }
    }
  }

  @override
  Set<bool> readBoolSet() {
    var length = readInt();
    Set<bool> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readBool());
      }
    }
    return set;
  }

  @override
  void writeByteSet(Set<int> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeByte(ele);
      }
    }
  }

  @override
  Set<int> readByteSet() {
    var length = readInt();
    Set<int> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readByte());
      }
    }
    return set;
  }

  @override
  void writeShortSet(Set<int> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeShort(ele);
      }
    }
  }

  @override
  Set<int> readShortSet() {
    var length = readInt();
    Set<int> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readShort());
      }
    }
    return set;
  }

  @override
  void writeIntSet(Set<int> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeInt(ele);
      }
    }
  }

  @override
  Set<int> readIntSet() {
    var length = readInt();
    Set<int> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readInt());
      }
    }
    return set;
  }

  @override
  void writeLongSet(Set<int> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeLong(ele);
      }
    }
  }

  @override
  Set<int> readLongSet() {
    var length = readInt();
    Set<int> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readLong());
      }
    }
    return set;
  }

  @override
  void writeFloatSet(Set<double> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeFloat(ele);
      }
    }
  }

  @override
  Set<double> readFloatSet() {
    var length = readInt();
    Set<double> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readFloat());
      }
    }
    return set;
  }

  @override
  void writeDoubleSet(Set<double> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeDouble(ele);
      }
    }
  }

  @override
  Set<double> readDoubleSet() {
    var length = readInt();
    Set<double> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readDouble());
      }
    }
    return set;
  }

  @override
  void writeStringSet(Set<String> set) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writeString(ele);
      }
    }
  }

  @override
  Set<String> readStringSet() {
    var length = readInt();
    Set<String> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readString());
      }
    }
    return set;
  }

  @override
  void writePacketSet(Set<Object> set, int protocolId) {
    if (set.isEmpty) {
      writeInt(0);
    } else {
      writeInt(set.length);
      for (var ele in set) {
        writePacket(ele, protocolId);
      }
    }
  }

  @override
  Set<T> readPacketSet<T>(int protocolId) {
    var length = readInt();
    Set<T> set = Set();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        set.add(readPacket(protocolId) as T);
      }
    }
    return set;
  }

  @override
  void writeIntIntMap(Map<int, int> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeInt(key);
        writeInt(value);
      });
    }
  }

  @override
  Map<int, int> readIntIntMap() {
    var length = readInt();
    Map<int, int> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readInt();
        var value = readInt();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeIntLongMap(Map<int, int> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeInt(key);
        writeLong(value);
      });
    }
  }

  @override
  Map<int, int> readIntLongMap() {
    var length = readInt();
    Map<int, int> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readInt();
        var value = readLong();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeIntStringMap(Map<int, String> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeInt(key);
        writeString(value);
      });
    }
  }

  @override
  Map<int, String> readIntStringMap() {
    var length = readInt();
    Map<int, String> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readInt();
        var value = readString();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeIntPacketMap(Map<int, Object> map, int protocolId) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeInt(key);
        writePacket(value, protocolId);
      });
    }
  }

  @override
  Map<int, T> readIntPacketMap<T>(int protocolId) {
    var length = readInt();
    Map<int, T> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readInt();
        var value = readPacket(protocolId);
        map[key] = value as T;
      }
    }
    return map;
  }

  @override
  void writeLongIntMap(Map<int, int> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeLong(key);
        writeInt(value);
      });
    }
  }

  @override
  Map<int, int> readLongIntMap() {
    var length = readInt();
    Map<int, int> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readLong();
        var value = readInt();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeLongLongMap(Map<int, int> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeLong(key);
        writeLong(value);
      });
    }
  }

  @override
  Map<int, int> readLongLongMap() {
    var length = readInt();
    Map<int, int> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readLong();
        var value = readLong();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeLongStringMap(Map<int, String> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeLong(key);
        writeString(value);
      });
    }
  }

  @override
  Map<int, String> readLongStringMap() {
    var length = readInt();
    Map<int, String> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readLong();
        var value = readString();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeLongPacketMap(Map<int, Object> map, int protocolId) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeLong(key);
        writePacket(value, protocolId);
      });
    }
  }

  @override
  Map<int, T> readLongPacketMap<T>(int protocolId) {
    var length = readInt();
    Map<int, T> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readInt();
        var value = readPacket(protocolId) as T;
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeStringIntMap(Map<String, int> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeString(key);
        writeInt(value);
      });
    }
  }

  @override
  Map<String, int> readStringIntMap() {
    var length = readInt();
    Map<String, int> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readString();
        var value = readInt();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeStringLongMap(Map<String, int> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeString(key);
        writeLong(value);
      });
    }
  }

  @override
  Map<String, int> readStringLongMap() {
    var length = readInt();
    Map<String, int> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readString();
        var value = readLong();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeStringStringMap(Map<String, String> map) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeString(key);
        writeString(value);
      });
    }
  }

  @override
  Map<String, String> readStringStringMap() {
    var length = readInt();
    Map<String, String> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readString();
        var value = readString();
        map[key] = value;
      }
    }
    return map;
  }

  @override
  void writeStringPacketMap(Map<String, Object> map, int protocolId) {
    if (map.isEmpty) {
      writeInt(0);
    } else {
      writeInt(map.length);
      map.forEach((key, value) {
        writeString(key);
        writePacket(value, protocolId);
      });
    }
  }

  @override
  Map<String, T> readStringPacketMap<T>(int protocolId) {
    var length = readInt();
    Map<String, T> map = Map();
    if (length > 0) {
      for (var i = 0; i < length; i++) {
        var key = readString();
        var value = readPacket(protocolId) as T;
        map[key] = value;
      }
    }
    return map;
  }
}
