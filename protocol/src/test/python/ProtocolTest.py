# encoding: UTF-8

from unittest import TestCase
import struct

from zfoopy import ProtocolManager
from zfoopy import ByteBuffer

def print_bytearray(array):
    signed_byte_array = struct.unpack('b' * len(array), array)
    for byte in signed_byte_array:
        print(byte)  # 以字节形式输出每个字节的值


class ByteBufferTestCase(TestCase):
    def test_complex_object(self):
        # 打开文件并读取内容
        # with open('D://github//zfoo//protocol//src//test//resources//compatible//normal-no-compatible.bytes', 'rb') as file:
        # with open('D://github//zfoo//protocol//src//test//resources//compatible//normal-out-compatible.bytes', 'rb') as file:
        # with open('D://github//zfoo//protocol//src//test//resources//compatible//normal-inner-compatible.bytes', 'rb') as file:
        # with open('D://github//zfoo//protocol//src//test//resources//compatible//normal-out-inner-compatible.bytes', 'rb') as file:
        with open('D://github//zfoo//protocol//src//test//resources//compatible//normal-out-inner-inner-compatible.bytes', 'rb') as file:
            content = file.read()

        # 将内容转为bytearray
        byte_array = bytearray(content)
        buffer = ByteBuffer.ByteBuffer()
        buffer.writeBytes(byte_array)
        obj = ProtocolManager.read(buffer)
        buffer.clear()
        ProtocolManager.write(buffer, obj)
        newObj = ProtocolManager.read(buffer)
        bytes = buffer.toBytes()

        # 打印bytearray
        print(byte_array)
        pass

    def test_bytearray(self):
        buffer = bytearray(10)
        for byte in buffer:
            print(byte)  # 以字节形式输出每个字节的值
        pass

    def test_buffer_raw_int(self):
        byteBuffer = ByteBuffer.ByteBuffer()
        byteBuffer.writeRawInt(2147483647)
        self.assertEqual(byteBuffer.readRawInt(), 2147483647)

    def test_buffer_int(self):
        byteBuffer = ByteBuffer.ByteBuffer()
        byteBuffer.writeInt(2147483647)
        self.assertEqual(byteBuffer.readInt(), 2147483647)
        byteBuffer.writeInt(-2147483648)
        self.assertEqual(byteBuffer.readInt(), -2147483648)
        pass

    def test_buffer_long(self):
        byteBuffer = ByteBuffer.ByteBuffer()
        byteBuffer.writeLong(9223372036854775807)
        self.assertEqual(byteBuffer.readLong(), 9223372036854775807)
        print_bytearray(byteBuffer.buffer)
        byteBuffer.writeLong(-9223372036854775808)
        self.assertEqual(byteBuffer.readLong(), -9223372036854775808)
        pass

    def test_buffer(self):
        byteBuffer = ByteBuffer.ByteBuffer()
        print(byteBuffer.writeOffset)
        print(byteBuffer.readOffset)
        print(byteBuffer.getCapacity())
        byteBuffer.writeBool(True)
        self.assertEqual(byteBuffer.readBool(), True)
        byteBuffer.writeByte(-100)
        self.assertEqual(byteBuffer.readByte(), -100)
        byteBuffer.writeShort(100)

        self.assertEqual(byteBuffer.readShort(), 100)
        byteBuffer.writeInt(9999)
        self.assertEqual(byteBuffer.readInt(), 9999)
        byteBuffer.writeInt(-9999)
        self.assertEqual(byteBuffer.readInt(), -9999)

        byteBuffer.writeLong(9999)
        self.assertEqual(byteBuffer.readLong(), 9999)
        byteBuffer.writeLong(-9999)
        self.assertEqual(byteBuffer.readLong(), -9999)

        byteBuffer.writeFloat(99.9)
        self.assertTrue(abs(byteBuffer.readFloat() - 99.9) < 0.001)

        str = "Hello World!你好"
        byteBuffer.writeString(str)
        self.assertEqual(byteBuffer.readString(), str)

        charStr = "A"
        byteBuffer.writeChar(charStr)
        self.assertEqual(byteBuffer.readChar(), charStr)
        print("----------------------------------------------------------------")
        print_bytearray(byteBuffer.buffer)
