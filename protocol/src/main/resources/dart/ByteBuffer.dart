import 'dart:convert';
import 'dart:typed_data';
import 'IByteBuffer.dart';
import 'ProtocolManager.dart';

class ByteBuffer implements IByteBuffer {
  Int8List buffer = Int8List(128);
  int writeOffset = 0;
  int readOffset = 0;

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
}

