import 'dart:io';
import 'dart:typed_data';
import 'zfoodart/ByteBuffer.dart';
import 'zfoodart/ProtocolManager.dart';

void main() {
  Protocolmanager.initProtocol();
  byteBufferTest();

  // var file = File("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes");
  // var file = File("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes");
  // var file = File("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes");
  // var file = File("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes");
  var file = File("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes");
  var uin8Bytes = file.readAsBytesSync();
  var bytes = Int8List.view(uin8Bytes.buffer);

  var byteBuffer = ByteBuffer();
  byteBuffer.writeBytes(bytes);

  var packet = Protocolmanager.read(byteBuffer);
  var newBuffer = ByteBuffer();
  Protocolmanager.write(newBuffer, packet);
  var newPacket = Protocolmanager.read(newBuffer);

  print(packet);
  print(newPacket);
}


void byteBufferTest() {
  var buffer = ByteBuffer();

  buffer.writeBool(true);
  assert(buffer.readBool() == true);
  buffer.writeBool(false);
  assert(buffer.readBool() == false);
  buffer.writeByte(99);
  assert(buffer.readByte() == 99);
  buffer.writeByte(-99);
  assert(buffer.readByte() == -99);
  buffer.writeShort(9999);
  assert(buffer.readShort() == 9999);
  buffer.writeShort(-9999);
  assert(buffer.readShort() == -9999);
  buffer.writeRawInt(2147483647);
  assert(buffer.readRawInt() == 2147483647);
  buffer.writeRawInt(-2147483648);
  assert(buffer.readRawInt() == -2147483648);
  buffer.writeInt(2147483647);
  assert(buffer.readInt() == 2147483647);
  buffer.writeInt(-2147483648);
  assert(buffer.readInt() == -2147483648);
  buffer.writeLong(9999999999999999);
  assert(buffer.readLong() == 9999999999999999);
  buffer.writeLong(-9223372036854775808);
  assert(buffer.readLong() == -9223372036854775808);
  buffer.writeFloat(99.0);
  assert(buffer.readFloat() == 99.0);
  buffer.writeDouble(99.0);
  assert(buffer.readDouble() == 99.0);
  buffer.writeString("Hello World! 你好");
  assert(buffer.readString() == "Hello World! 你好");
}