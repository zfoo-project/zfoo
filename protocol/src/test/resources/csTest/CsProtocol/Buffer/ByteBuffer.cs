using System;
using System.Collections.Generic;
using System.Text;

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
            if (writeOffset > buffer.Length)
            {
                throw new Exception("writeIndex[" + writeIndex + "] out of bounds exception: readerIndex: " + readOffset +
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
    }
}