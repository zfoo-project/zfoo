using System;
using System.IO;

namespace zfoocs
{
    class Program
    {
        static void Main(string[] args)
        {
            ProtocolManager.InitProtocol();
            ByteBufferTest();
            complexObjectTest();
            compatibleTest();
        }
        
        public static void compatibleTest()
        {
            // var bytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes");
            // var bytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes");
            // var bytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes");
            // var bytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes");
            var bytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes");
            var buffer = ByteBuffer.ValueOf();
            buffer.WriteBytes(bytes);
            var packet = ProtocolManager.Read(buffer);

            var newBuffer = ByteBuffer.ValueOf();
            ProtocolManager.Write(newBuffer, packet);
            var newBytes = newBuffer.ToBytes();

            Console.Out.WriteLine("source size " + bytes.Length);
            Console.Out.WriteLine("target size " + newBytes.Length);
        }

        public static void complexObjectTest()
        {
            // 获取复杂对象的字节流
            var complexObjectBytes = File.ReadAllBytes("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes");
            var buffer = ByteBuffer.ValueOf();
            buffer.WriteBytes(complexObjectBytes);
            var packet = ProtocolManager.Read(buffer);

            var newBuffer = ByteBuffer.ValueOf();
            ProtocolManager.Write(newBuffer, packet);
            var bytes = newBuffer.ToBytes();

            // set和map是无序的，所以有的时候输入和输出的字节流有可能不一致，但是长度一定是一致的
        }
        
        public static void ByteBufferTest()
        {
            byteTest();
            bytesTest();
            shortTest();
            intTest();
            longTest();
            floatTest();
            doubleTest();
            stringTest();
        }


        public static void byteTest()
        {
            byte value = 9;
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteByte(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            byte readValue = readerByteBuffer.ReadByte();
            AssertEquals(value, readValue);
        }

        public static void bytesTest()
        {
            var value = new byte[] {1, 2, 3};
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteBytes(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            var readValue = readerByteBuffer.ReadBytes(3);
            AssertEquals<byte>(value, readValue);
        }

        public static void shortTest()
        {
            short value = 9999;
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteShort(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            short readValue = readerByteBuffer.ReadShort();
            AssertEquals(value, readValue);
        }

        public static void intTest()
        {
            int value = 99999999;
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteInt(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            int readValue = readerByteBuffer.ReadInt();
            AssertEquals(value, readValue);
        }

        public static void longTest()
        {
            long value = 9999999999999999L;
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteLong(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            long readValue = readerByteBuffer.ReadLong();
            AssertEquals(value, readValue);
        }

        public static void floatTest()
        {
            float value = 999999.56F;
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteFloat(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            float readValue = readerByteBuffer.ReadFloat();
            AssertEquals(value, readValue);
        }

        public static void doubleTest()
        {
            double value = 999999.56;
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteDouble(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            double readValue = readerByteBuffer.ReadDouble();
            AssertEquals(value, readValue);
        }

        public static void stringTest()
        {
            string value = "aaa";
            ByteBuffer writerByteBuffer = ByteBuffer.ValueOf();
            writerByteBuffer.WriteString(value);
            byte[] bytes = writerByteBuffer.ToBytes();

            ByteBuffer readerByteBuffer = ByteBuffer.ValueOf();
            readerByteBuffer.WriteBytes(bytes);
            string readValue = readerByteBuffer.ReadString();
            AssertEquals(value, readValue);
        }

        public static void AssertEquals(object a, object b)
        {
            if (a.Equals(b))
            {
                return;
            }

            throw new Exception("a is not equals b");
        }

        public static void AssertEquals<T>(T[] a, T[] b)
        {
            if (a == b)
            {
                return;
            }


            if (a != null && b != null && a.Length == b.Length)
            {
                for (var i = 0; i < a.Length; i++)
                {
                    AssertEquals(a[i], b[i]);
                }

                return;
            }

            throw new Exception("a is not equals b");
        }
    }
}
