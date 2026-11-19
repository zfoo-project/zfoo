using System;

namespace zfoocs
{
    public class LittleEndianByteBuffer : ByteBuffer
    {
        /**
        * Reverse the byte array; if the host byte order is little-endian, reverse it to convert to big-endian
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