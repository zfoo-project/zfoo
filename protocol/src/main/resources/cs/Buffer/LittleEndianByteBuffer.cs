using System;

namespace CsProtocol.Buffer
{
    public class LittleEndianByteBuffer : ByteBuffer
    {
        /**
        * 翻转字节数组，如果本地字节序列为低字节序列，则进行翻转以转换为高字节序列
        */
        private static byte[] reverse(byte[] bytes)
        {
            Array.Reverse(bytes);
            return bytes;
        }

        public override void WriteShort(short value)
        {
            WriteBytes(reverse(GetBytes(value)));
        }

        public override short ReadShort()
        {
            return GetInt16(reverse(ReadBytes(2)));
        }

        public override void WriteRawInt(int value)
        {
            WriteBytes(reverse(GetBytes(value)));
        }

        public override int ReadRawInt()
        {
            return GetInt32(reverse(ReadBytes(4)));
        }

        public override void WriteFloat(float value)
        {
            WriteBytes(reverse(GetBytes(value)));
        }

        public override float ReadFloat()
        {
            return GetSingle(reverse(ReadBytes(4)));
        }

        public override void WriteDouble(double value)
        {
            WriteBytes(reverse(GetBytes(value)));
        }

        public override double ReadDouble()
        {
            return GetDouble(reverse(ReadBytes(8)));
        }
    }
}