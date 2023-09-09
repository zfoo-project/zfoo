using System;
using System.Collections.Generic;
using System.Text;

// CSharp字节保存在内存的低地址中是根据操作系统来的，所以有可能是大端模式，也有可能是小端模式
// 右移操作>>是带符号右移
namespace CsProtocol.Buffer
{
    public abstract class ByteBuffer
    {
        private static readonly Queue<ByteBuffer> byteBufferQueue = new Queue<ByteBuffer>();

        private static readonly int INIT_SIZE = 128;
        private static readonly int MAX_SIZE = 655537;

        private byte[] buffer = new byte[INIT_SIZE];
        private int writeOffset = 0;
        private int readOffset = 0;

        public static ByteBuffer ValueOf()
        {
            lock (byteBufferQueue)
            {
                if (byteBufferQueue.Count > 0)
                {
                    return byteBufferQueue.Dequeue();
                }
            }

            if (BitConverter.IsLittleEndian)
            {
                return new LittleEndianByteBuffer();
            }

            return new BigEndianByteBuffer();
        }

        public void Clear()
        {
            lock (byteBufferQueue)
            {
                if (byteBufferQueue.Contains(this))
                {
                    throw new Exception("The reference has been released.");
                }

                byteBufferQueue.Enqueue(this);
                writeOffset = 0;
                readOffset = 0;
            }
        }

        // -------------------------------------------------get/set-------------------------------------------------
        public int WriteOffset()
        {
            return writeOffset;
        }

        public void SetWriteOffset(int writeIndex)
        {
            if (writeIndex > buffer.Length)
            {
                throw new Exception("writeIndex[" + writeIndex + "] out of bounds exception: readerIndex: " +
                                    readOffset +
                                    ", writerIndex: " + writeOffset +
                                    "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + buffer.Length);
            }

            writeOffset = writeIndex;
        }

        public void SetReadOffset(int readIndex)
        {
            if (readIndex > writeOffset)
            {
                throw new Exception("readIndex[" + readIndex + "] out of bounds exception: readerIndex: " + readOffset +
                                    ", writerIndex: " + writeOffset +
                                    "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + buffer.Length);
            }

            readOffset = readIndex;
        }

        public byte[] ToBytes()
        {
            var bytes = new byte[writeOffset];
            Array.Copy(buffer, 0, bytes, 0, writeOffset);
            return bytes;
        }

        public bool IsReadable()
        {
            return writeOffset > readOffset;
        }

        // -------------------------------------------------write/read-------------------------------------------------
        public void WriteBool(bool value)
        {
            EnsureCapacity(1);
            buffer[writeOffset] = value ? (byte) 1 : (byte) 0;
            writeOffset++;
        }

        public bool ReadBool()
        {
            var byteValue = buffer[readOffset];
            readOffset++;
            return byteValue == 1;
        }

        public void WriteByte(byte value)
        {
            EnsureCapacity(1);
            buffer[writeOffset] = value;
            writeOffset++;
        }

        public byte ReadByte()
        {
            var byteValue = buffer[readOffset];
            readOffset++;
            return byteValue;
        }


        public int GetCapacity()
        {
            return buffer.Length - writeOffset;
        }

        public void EnsureCapacity(int capacity)
        {
            while (capacity - GetCapacity() > 0)
            {
                var newSize = buffer.Length * 2;
                if (newSize > MAX_SIZE)
                {
                    throw new Exception("Bytebuf max size is [655537], out of memory error");
                }

                var newBytes = new byte[newSize];
                Array.Copy(buffer, 0, newBytes, 0, buffer.Length);
                this.buffer = newBytes;
            }
        }

        public void WriteBytes(byte[] bytes)
        {
            EnsureCapacity(bytes.Length);
            var length = bytes.Length;
            Array.Copy(bytes, 0, buffer, writeOffset, length);
            writeOffset += length;
        }

        public byte[] ReadBytes(int count)
        {
            var bytes = new byte[count];
            Array.Copy(buffer, readOffset, bytes, 0, count);
            readOffset += count;
            return bytes;
        }

        public abstract void WriteShort(short value);
        public abstract short ReadShort();


        // *******************************************int***************************************************
        public void WriteInt(int intValue)
        {
            // 用Zigzag算法压缩int和long的值
            // 再用Varint紧凑算法表示数字的有效位
            uint value = (uint) ((intValue << 1) ^ (intValue >> 31));

            if (value >> 7 == 0)
            {
                WriteByte((byte) value);
                return;
            }

            if (value >> 14 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) (value >> 7));
                return;
            }

            if (value >> 21 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) (value >> 14));
                return;
            }

            if (value >> 28 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) ((value >> 14) | 0x80));
                WriteByte((byte) (value >> 21));
                return;
            }

            WriteByte((byte) (value | 0x80));
            WriteByte((byte) ((value >> 7) | 0x80));
            WriteByte((byte) ((value >> 14) | 0x80));
            WriteByte((byte) ((value >> 21) | 0x80));
            WriteByte((byte) (value >> 28));
        }

        public int ReadInt()
        {
            uint b = ReadByte();
            uint value = b & 0x7F;
            if ((b & 0x80) != 0)
            {
                b = ReadByte();
                value |= (b & 0x7F) << 7;
                if ((b & 0x80) != 0)
                {
                    b = ReadByte();
                    value |= (b & 0x7F) << 14;
                    if ((b & 0x80) != 0)
                    {
                        b = ReadByte();
                        value |= (b & 0x7F) << 21;
                        if ((b & 0x80) != 0)
                        {
                            b = ReadByte();
                            value |= (b & 0x7F) << 28;
                        }
                    }
                }
            }

            return (int) (value >> 1) ^ -((int) (value) & 1);
        }

        // 写入没有压缩的int
        public abstract void WriteRawInt(int value);

        // 读取没有压缩的int
        public abstract int ReadRawInt();

        // *******************************************long**************************************************
        public long ReadLong()
        {
            ulong b = ReadByte();
            ulong value = b & 0x7F;
            if ((b & 0x80) != 0)
            {
                b = ReadByte();
                value |= (b & 0x7F) << 7;
                if ((b & 0x80) != 0)
                {
                    b = ReadByte();
                    value |= (b & 0x7F) << 14;
                    if ((b & 0x80) != 0)
                    {
                        b = ReadByte();
                        value |= (b & 0x7F) << 21;
                        if ((b & 0x80) != 0)
                        {
                            b = ReadByte();
                            value |= (b & 0x7F) << 28;
                            if ((b & 0x80) != 0)
                            {
                                b = ReadByte();
                                value |= (b & 0x7F) << 35;
                                if ((b & 0x80) != 0)
                                {
                                    b = ReadByte();
                                    value |= (b & 0x7F) << 42;
                                    if ((b & 0x80) != 0)
                                    {
                                        b = ReadByte();
                                        value |= (b & 0x7F) << 49;
                                        if ((b & 0x80) != 0)
                                        {
                                            b = ReadByte();
                                            value |= b << 56;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return (long) (value >> 1) ^ -((long) value & 1);
        }

        public void WriteLong(long longValue)
        {
            ulong value = (ulong) ((longValue << 1) ^ (longValue >> 63));

            if (value >> 7 == 0)
            {
                WriteByte((byte) value);
                return;
            }

            if (value >> 14 == 0)
            {
                WriteByte((byte) ((value & 0x7F) | 0x80));
                WriteByte((byte) (value >> 7));
                return;
            }

            if (value >> 21 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) (value >> 14));
                return;
            }

            if ((value >> 28) == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) ((value >> 14) | 0x80));
                WriteByte((byte) (value >> 21));
                return;
            }

            if (value >> 35 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) ((value >> 14) | 0x80));
                WriteByte((byte) ((value >> 21) | 0x80));
                WriteByte((byte) (value >> 28));
                return;
            }

            if (value >> 42 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) ((value >> 14) | 0x80));
                WriteByte((byte) ((value >> 21) | 0x80));
                WriteByte((byte) ((value >> 28) | 0x80));
                WriteByte((byte) (value >> 35));
                return;
            }

            if (value >> 49 == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) ((value >> 14) | 0x80));
                WriteByte((byte) ((value >> 21) | 0x80));
                WriteByte((byte) ((value >> 28) | 0x80));
                WriteByte((byte) ((value >> 35) | 0x80));
                WriteByte((byte) (value >> 42));
                return;
            }

            if ((value >> 56) == 0)
            {
                WriteByte((byte) (value | 0x80));
                WriteByte((byte) ((value >> 7) | 0x80));
                WriteByte((byte) ((value >> 14) | 0x80));
                WriteByte((byte) ((value >> 21) | 0x80));
                WriteByte((byte) ((value >> 28) | 0x80));
                WriteByte((byte) ((value >> 35) | 0x80));
                WriteByte((byte) ((value >> 42) | 0x80));
                WriteByte((byte) (value >> 49));
                return;
            }

            WriteByte((byte) (value | 0x80));
            WriteByte((byte) ((value >> 7) | 0x80));
            WriteByte((byte) ((value >> 14) | 0x80));
            WriteByte((byte) ((value >> 21) | 0x80));
            WriteByte((byte) ((value >> 28) | 0x80));
            WriteByte((byte) ((value >> 35) | 0x80));
            WriteByte((byte) ((value >> 42) | 0x80));
            WriteByte((byte) ((value >> 49) | 0x80));
            WriteByte((byte) (value >> 56));
        }


        // *******************************************float***************************************************
        public abstract void WriteFloat(float value);
        public abstract float ReadFloat();

        // *******************************************double***************************************************
        public abstract void WriteDouble(double value);
        public abstract double ReadDouble();

        // *******************************************char***************************************************
        public char ReadChar()
        {
            // need check
            var str = ReadString();
            return string.IsNullOrEmpty(str) ? char.MinValue : str[0];
        }

        public void WriteChar(char value)
        {
            // need check
            WriteString(new string(value, 1));
        }

        // *******************************************String***************************************************

        public void WriteString(string value)
        {
            if (string.IsNullOrEmpty(value))
            {
                WriteInt(0);
                return;
            }

            byte[] strBytes = GetBytes(value);

            if (strBytes == null || strBytes.Length <= 0)
            {
                WriteInt(0);
                return;
            }

            WriteInt(strBytes.Length);
            WriteBytes(strBytes);
        }

        public string ReadString()
        {
            int length = ReadInt();
            if (length <= 0)
            {
                return string.Empty;
            }

            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++)
            {
                bytes[i] = ReadByte();
            }

            string str = GetString(bytes);
            return str;
        }


        // -------------------------------------------------Converter-------------------------------------------------
        private static readonly byte[] EMPTY_BYTE_ARRAY = new byte[] { };

        /// <summary>
        /// 以字节数组的形式返回指定的 16 位有符号整数值。
        /// </summary>
        /// <param name="value">要转换的数字。</param>
        /// <returns>长度为 2 的字节数组。</returns>
        public byte[] GetBytes(short value)
        {
            return BitConverter.GetBytes(value);
        }

        /// <summary>
        /// 返回由字节数组中前两个字节转换来的 16 位有符号整数。
        /// </summary>
        /// <param name="value">字节数组。</param>
        /// <returns>由两个字节构成的 16 位有符号整数。</returns>
        public short GetInt16(byte[] value)
        {
            return BitConverter.ToInt16(value, 0);
        }


        /// <summary>
        /// 以字节数组的形式返回指定的 32 位有符号整数值。
        /// </summary>
        /// <param name="value">要转换的数字。</param>
        /// <returns>长度为 4 的字节数组。</returns>
        public byte[] GetBytes(int value)
        {
            return BitConverter.GetBytes(value);
        }


        /// <summary>
        /// 返回由字节数组中前四个字节转换来的 32 位有符号整数。
        /// </summary>
        /// <param name="value">字节数组。</param>
        /// <returns>由四个字节构成的 32 位有符号整数。</returns>
        public int GetInt32(byte[] value)
        {
            return BitConverter.ToInt32(value, 0);
        }


        /// <summary>
        /// 以字节数组的形式返回指定的单精度浮点值。
        /// </summary>
        /// <param name="value">要转换的数字。</param>
        /// <returns>长度为 4 的字节数组。</returns>
        public byte[] GetBytes(float value)
        {
            return BitConverter.GetBytes(value);
        }

        /// <summary>
        /// 返回由字节数组中前四个字节转换来的单精度浮点数。
        /// </summary>
        /// <param name="value">字节数组。</param>
        /// <returns>由四个字节构成的单精度浮点数。</returns>
        public float GetSingle(byte[] value)
        {
            return BitConverter.ToSingle(value, 0);
        }

        /// <summary>
        /// 以字节数组的形式返回指定的双精度浮点值。
        /// </summary>
        /// <param name="value">要转换的数字。</param>
        /// <returns>长度为 8 的字节数组。</returns>
        public byte[] GetBytes(double value)
        {
            return BitConverter.GetBytes(value);
        }

        /// <summary>
        /// 返回由字节数组中前八个字节转换来的双精度浮点数。
        /// </summary>
        /// <param name="value">字节数组。</param>
        /// <returns>由八个字节构成的双精度浮点数。</returns>
        public double GetDouble(byte[] value)
        {
            return BitConverter.ToDouble(value, 0);
        }


        /// <summary>
        /// 以 UTF-8 字节数组的形式返回指定的字符串。
        /// </summary>
        /// <param name="value">要转换的字符串。</param>
        /// <returns>UTF-8 字节数组。</returns>
        public byte[] GetBytes(string value)
        {
            if (value == null)
            {
                return EMPTY_BYTE_ARRAY;
            }

            return Encoding.UTF8.GetBytes(value);
        }

        /// <summary>
        /// 返回由 UTF-8 字节数组转换来的字符串。
        /// </summary>
        /// <param name="value">UTF-8 字节数组。</param>
        /// <returns>字符串。</returns>
        public string GetString(byte[] value)
        {
            if (value == null)
            {
                return string.Empty;
            }

            return Encoding.UTF8.GetString(value, 0, value.Length);
        }

        public bool WritePacketFlag(IProtocol packet)
        {
            bool flag = packet == null;
            WriteBool(!flag);
            return flag;
        }

        public void WriteBooleanArray(bool[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteBool(array[index]);
                }
            }
        }

        public bool[] ReadBooleanArray()
        {
            int size = ReadInt();
            bool[] array = new bool[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadBool();
                }
            }

            return array;
        }

        public void WriteByteArray(byte[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteByte(array[index]);
                }
            }
        }

        public byte[] ReadByteArray()
        {
            int size = ReadInt();
            byte[] array = new byte[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadByte();
                }
            }

            return array;
        }

        public void WriteShortArray(short[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteShort(array[index]);
                }
            }
        }

        public short[] ReadShortArray()
        {
            int size = ReadInt();
            short[] array = new short[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadShort();
                }
            }

            return array;
        }

        public void WriteIntArray(int[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteInt(array[index]);
                }
            }
        }

        public int[] ReadIntArray()
        {
            int size = ReadInt();
            int[] array = new int[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadInt();
                }
            }

            return array;
        }

        public void WriteLongArray(long[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteLong(array[index]);
                }
            }
        }

        public long[] ReadLongArray()
        {
            int size = ReadInt();
            long[] array = new long[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadLong();
                }
            }

            return array;
        }

        public void WriteFloatArray(float[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteFloat(array[index]);
                }
            }
        }

        public float[] ReadFloatArray()
        {
            int size = ReadInt();
            float[] array = new float[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadFloat();
                }
            }

            return array;
        }

        public void WriteDoubleArray(double[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteDouble(array[index]);
                }
            }
        }

        public double[] ReadDoubleArray()
        {
            int size = ReadInt();
            double[] array = new double[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadDouble();
                }
            }

            return array;
        }

        public void WriteCharArray(char[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteChar(array[index]);
                }
            }
        }

        public char[] ReadCharArray()
        {
            int size = ReadInt();
            char[] array = new char[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadChar();
                }
            }

            return array;
        }

        public void WriteStringArray(string[] array)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    WriteString(array[index]);
                }
            }
        }

        public string[] ReadStringArray()
        {
            int size = ReadInt();
            string[] array = new string[size];
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    array[index] = ReadString();
                }
            }

            return array;
        }

        public void WritePacketArray<T>(T[] array, short protocolId)
        {
            if ((array == null) || (array.Length == 0))
            {
                WriteInt(0);
            }
            else
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                WriteInt(array.Length);
                int length = array.Length;
                for (int index = 0; index < length; index++)
                {
                    protocolRegistration.Write(this, (IProtocol) array[index]);
                }
            }
        }

        public T[] ReadPacketArray<T>(short protocolId)
        {
            int size = ReadInt();
            T[] array = new T[size];
            if (size > 0)
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                for (int index = 0; index < size; index++)
                {
                    array[index] = (T) protocolRegistration.Read(this);
                }
            }

            return array;
        }

        public void WriteBooleanList(List<bool> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteBool(list[index]);
                }
            }
        }

        public List<bool> ReadBooleanList()
        {
            int size = ReadInt();
            List<bool> list = new List<bool>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadBool());
                }
            }

            return list;
        }

        public void WriteByteList(List<byte> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteByte(list[index]);
                }
            }
        }

        public List<byte> ReadByteList()
        {
            int size = ReadInt();
            List<byte> list = new List<byte>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadByte());
                }
            }

            return list;
        }

        public void WriteShortList(List<short> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteShort(list[index]);
                }
            }
        }

        public List<short> ReadShortList()
        {
            int size = ReadInt();
            List<short> list = new List<short>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadShort());
                }
            }

            return list;
        }

        public void WriteIntList(List<int> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteInt(list[index]);
                }
            }
        }

        public List<int> ReadIntList()
        {
            int size = ReadInt();
            List<int> list = new List<int>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadInt());
                }
            }

            return list;
        }

        public void WriteLongList(List<long> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteLong(list[index]);
                }
            }
        }

        public List<long> ReadLongList()
        {
            int size = ReadInt();
            List<long> list = new List<long>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadLong());
                }
            }

            return list;
        }

        public void WriteFloatList(List<float> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteFloat(list[index]);
                }
            }
        }

        public List<float> ReadFloatList()
        {
            int size = ReadInt();
            List<float> list = new List<float>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadFloat());
                }
            }

            return list;
        }

        public void WriteDoubleList(List<double> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteDouble(list[index]);
                }
            }
        }

        public List<double> ReadDoubleList()
        {
            int size = ReadInt();
            List<double> list = new List<double>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadDouble());
                }
            }

            return list;
        }

        public void WriteCharList(List<char> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteDouble(list[index]);
                }
            }
        }

        public List<char> ReadCharList()
        {
            int size = ReadInt();
            List<char> list = new List<char>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadChar());
                }
            }

            return list;
        }

        public void WriteStringList(List<string> list)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    WriteString(list[index]);
                }
            }
        }

        public List<string> ReadStringList()
        {
            int size = ReadInt();
            List<string> list = new List<string>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    list.Add(ReadString());
                }
            }

            return list;
        }

        public void WritePacketList<T>(List<T> list, short protocolId)
        {
            if ((list == null) || (list.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                WriteInt(list.Count);
                int length = list.Count;
                for (int index = 0; index < length; index++)
                {
                    protocolRegistration.Write(this, (IProtocol) list[index]);
                }
            }
        }

        public List<T> ReadPacketList<T>(short protocolId)
        {
            int size = ReadInt();
            List<T> list = new List<T>(size);
            if (size > 0)
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                for (int index = 0; index < size; index++)
                {
                    list.Add((T) protocolRegistration.Read(this));
                }
            }

            return list;
        }

        public void WriteBooleanSet(HashSet<bool> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteBool(element);
                }
            }
        }

        public HashSet<bool> ReadBooleanSet()
        {
            int size = ReadInt();
            HashSet<bool> set = new HashSet<bool>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadBool());
                }
            }

            return set;
        }

        public void WriteShortSet(HashSet<short> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteShort(element);
                }
            }
        }

        public HashSet<short> ReadShortSet()
        {
            int size = ReadInt();
            HashSet<short> set = new HashSet<short>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadShort());
                }
            }

            return set;
        }

        public void WriteIntSet(HashSet<int> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteInt(element);
                }
            }
        }

        public HashSet<int> ReadIntSet()
        {
            int size = ReadInt();
            HashSet<int> set = new HashSet<int>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadInt());
                }
            }

            return set;
        }

        public void WriteLongSet(HashSet<long> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteLong(element);
                }
            }
        }

        public HashSet<long> ReadLongSet()
        {
            int size = ReadInt();
            HashSet<long> set = new HashSet<long>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadLong());
                }
            }

            return set;
        }

        public void WriteFloatSet(HashSet<float> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteFloat(element);
                }
            }
        }

        public HashSet<float> ReadFloatSet()
        {
            int size = ReadInt();
            HashSet<float> set = new HashSet<float>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadFloat());
                }
            }

            return set;
        }

        public void WriteDoubleSet(HashSet<double> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteDouble(element);
                }
            }
        }

        public HashSet<double> ReadDoubleSet()
        {
            int size = ReadInt();
            HashSet<double> set = new HashSet<double>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadDouble());
                }
            }

            return set;
        }

        public void WriteCharSet(HashSet<char> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteChar(element);
                }
            }
        }

        public HashSet<char> ReadCharSet()
        {
            int size = ReadInt();
            HashSet<char> set = new HashSet<char>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadChar());
                }
            }

            return set;
        }

        public void WriteStringSet(HashSet<string> set)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    WriteString(element);
                }
            }
        }

        public HashSet<string> ReadStringSet()
        {
            int size = ReadInt();
            HashSet<string> set = new HashSet<string>();
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    set.Add(ReadString());
                }
            }

            return set;
        }

        public void WritePacketSet<T>(HashSet<T> set, short protocolId)
        {
            if ((set == null) || (set.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                WriteInt(set.Count);
                foreach (var element in set)
                {
                    protocolRegistration.Write(this, (IProtocol) element);
                }
            }
        }

        public HashSet<T> ReadPacketSet<T>(short protocolId)
        {
            int size = ReadInt();
            HashSet<T> set = new HashSet<T>();
            if (size > 0)
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                for (int index = 0; index < size; index++)
                {
                    set.Add((T) protocolRegistration.Read(this));
                }
            }

            return set;
        }

        public void WriteIntIntMap(Dictionary<int, int> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteInt(element.Key);
                    WriteInt(element.Value);
                }
            }
        }

        public Dictionary<int, int> ReadIntIntMap()
        {
            int size = ReadInt();
            Dictionary<int, int> map = new Dictionary<int, int>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadInt();
                    var value = ReadInt();
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteIntLongMap(Dictionary<int, long> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteInt(element.Key);
                    WriteLong(element.Value);
                }
            }
        }

        public Dictionary<int, long> ReadIntLongMap()
        {
            int size = ReadInt();
            Dictionary<int, long> map = new Dictionary<int, long>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadInt();
                    var value = ReadLong();
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteIntStringMap(Dictionary<int, string> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteInt(element.Key);
                    WriteString(element.Value);
                }
            }
        }

        public Dictionary<int, string> ReadIntStringMap()
        {
            int size = ReadInt();
            Dictionary<int, string> map = new Dictionary<int, string>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadInt();
                    var value = ReadString();
                    map[key] = value;
                }
            }

            return map;
        }


        public void WriteIntPacketMap<T>(Dictionary<int, T> map, short protocolId)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteInt(element.Key);
                    protocolRegistration.Write(this, (IProtocol) element.Value);
                }
            }
        }

        public Dictionary<int, T> ReadIntPacketMap<T>(short protocolId)
        {
            int size = ReadInt();
            Dictionary<int, T> map = new Dictionary<int, T>(size);
            if (size > 0)
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                for (int index = 0; index < size; index++)
                {
                    var key = ReadInt();
                    var value = (T) protocolRegistration.Read(this);
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteLongIntMap(Dictionary<long, int> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteLong(element.Key);
                    WriteInt(element.Value);
                }
            }
        }

        public Dictionary<long, int> ReadLongIntMap()
        {
            int size = ReadInt();
            Dictionary<long, int> map = new Dictionary<long, int>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadLong();
                    var value = ReadInt();
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteLongLongMap(Dictionary<long, long> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteLong(element.Key);
                    WriteLong(element.Value);
                }
            }
        }

        public Dictionary<long, long> ReadLongLongMap()
        {
            int size = ReadInt();
            Dictionary<long, long> map = new Dictionary<long, long>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadLong();
                    var value = ReadLong();
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteLongStringMap(Dictionary<long, string> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteLong(element.Key);
                    WriteString(element.Value);
                }
            }
        }

        public Dictionary<long, string> ReadLongStringMap()
        {
            int size = ReadInt();
            Dictionary<long, string> map = new Dictionary<long, string>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadLong();
                    var value = ReadString();
                    map[key] = value;
                }
            }

            return map;
        }


        public void WriteLongPacketMap<T>(Dictionary<long, T> map, short protocolId)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteLong(element.Key);
                    protocolRegistration.Write(this, (IProtocol) element.Value);
                }
            }
        }

        public Dictionary<long, T> ReadLongPacketMap<T>(short protocolId)
        {
            int size = ReadInt();
            Dictionary<long, T> map = new Dictionary<long, T>(size);
            if (size > 0)
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                for (int index = 0; index < size; index++)
                {
                    var key = ReadLong();
                    var value = (T) protocolRegistration.Read(this);
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteStringIntMap(Dictionary<string, int> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteString(element.Key);
                    WriteInt(element.Value);
                }
            }
        }

        public Dictionary<string, int> ReadStringIntMap()
        {
            int size = ReadInt();
            Dictionary<string, int> map = new Dictionary<string, int>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadString();
                    var value = ReadInt();
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteStringLongMap(Dictionary<string, long> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteString(element.Key);
                    WriteLong(element.Value);
                }
            }
        }

        public Dictionary<string, long> ReadStringLongMap()
        {
            int size = ReadInt();
            Dictionary<string, long> map = new Dictionary<string, long>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadString();
                    var value = ReadLong();
                    map[key] = value;
                }
            }

            return map;
        }

        public void WriteStringStringMap(Dictionary<string, string> map)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteString(element.Key);
                    WriteString(element.Value);
                }
            }
        }

        public Dictionary<string, string> ReadStringStringMap()
        {
            int size = ReadInt();
            Dictionary<string, string> map = new Dictionary<string, string>(size);
            if (size > 0)
            {
                for (int index = 0; index < size; index++)
                {
                    var key = ReadString();
                    var value = ReadString();
                    map[key] = value;
                }
            }

            return map;
        }


        public void WriteStringPacketMap<T>(Dictionary<string, T> map, short protocolId)
        {
            if ((map == null) || (map.Count == 0))
            {
                WriteInt(0);
            }
            else
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                WriteInt(map.Count);
                foreach (var element in map)
                {
                    WriteString(element.Key);
                    protocolRegistration.Write(this, (IProtocol) element.Value);
                }
            }
        }

        public Dictionary<string, T> ReadStringPacketMap<T>(short protocolId)
        {
            int size = ReadInt();
            Dictionary<string, T> map = new Dictionary<string, T>(size);
            if (size > 0)
            {
                IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
                for (int index = 0; index < size; index++)
                {
                    var key = ReadString();
                    var value = (T) protocolRegistration.Read(this);
                    map[key] = value;
                }
            }

            return map;
        }

        public void WritePacket<T>(T packet, short protocolId)
        {
            IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
            protocolRegistration.Write(this, (IProtocol) packet);
        }

        public T ReadPacket<T>(short protocolId)
        {
            IProtocolRegistration protocolRegistration = ProtocolManager.GetProtocol(protocolId);
            return (T) protocolRegistration.Read(this);
        }
    }
}
