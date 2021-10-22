namespace CsProtocol.Buffer
{
    public class BigEndianByteBuffer : ByteBuffer
    {
        // fast int to byte[] conversion and vice versa
        // -> test with 100k conversions:
        //    BitConverter.GetBytes(ushort): 144ms
        //    bit shifting: 11ms
        // -> 10x speed improvement makes this optimization actually worth it
        // -> this way we don't need to allocate BinaryWriter/Reader either
        // -> 4 bytes because some people may want to send messages larger than
        //    64K bytes
        // => big endian is standard for network transmissions, and necessary
        //    for compatibility with erlang
        public static byte[] IntToBytesBigEndian(int value)
        {
            return new byte[]
            {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
            };
        }

        public static int BytesToIntBigEndian(byte[] bytes)
        {
            return (bytes[0] << 24) |
                   (bytes[1] << 16) |
                   (bytes[2] << 8) |
                   bytes[3];
        }

        public override void WriteShort(short value)
        {
            WriteBytes(GetBytes(value));
        }

        public override short ReadShort()
        {
            return GetInt16(ReadBytes(2));
        }

        public override void WriteRawInt(int value)
        {
            WriteBytes(IntToBytesBigEndian(value));
        }

        public override int ReadRawInt()
        {
            return BytesToIntBigEndian(ReadBytes(4));
        }

        public override void WriteFloat(float value)
        {
            WriteBytes(GetBytes(value));
        }

        public override float ReadFloat()
        {
            return GetSingle(ReadBytes(4));
        }

        public override void WriteDouble(double value)
        {
            WriteBytes(GetBytes(value));
        }

        public override double ReadDouble()
        {
            return GetDouble(ReadBytes(8));
        }
    }
}