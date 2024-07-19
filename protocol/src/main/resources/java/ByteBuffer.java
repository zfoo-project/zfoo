${protocol_root_path}

import java.nio.charset.Charset;
import java.util.*;

public class ByteBuffer {
    private static final int INIT_SIZE = 128;
    private static final int MAX_SIZE = 655537;

    private byte[] buffer = new byte[INIT_SIZE];
    private int writeOffset = 0;
    private int readOffset = 0;


    public void adjustPadding(int predictionLength, int beforewriteIndex) {
        // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
        var currentwriteIndex = writeOffset;
        var predictionCount = writeIntCount(predictionLength);
        var length = currentwriteIndex - beforewriteIndex - predictionCount;
        var lengthCount = writeIntCount(length);
        var padding = lengthCount - predictionCount;
        if (padding == 0) {
            writeOffset = beforewriteIndex;
            writeInt(length);
            writeOffset = currentwriteIndex;
        } else {
            var bytes = new byte[length];
            System.arraycopy(buffer, currentwriteIndex - length, bytes, 0, length);
            writeOffset = beforewriteIndex;
            writeInt(length);
            writeBytes(bytes);
        }
    }

    public boolean compatibleRead(int beforeReadIndex, int length) {
        return length != -1 && readOffset < length + beforeReadIndex;
    }

    // -------------------------------------------------get/set-------------------------------------------------

    public byte[] getBuffer() {
        return buffer;
    }

    public int getWriteOffset() {
        return writeOffset;
    }

    public void setWriteOffset(int writeIndex) {
        if (writeIndex > buffer.length) {
            throw new RuntimeException("writeIndex[" + writeIndex + "] out of bounds exception: readOffset: " + readOffset +
                    ", writeOffset: " + writeOffset + "(expected: 0 <= readOffset <= writeOffset <= capacity:" + buffer.length + ")");
        }
        writeOffset = writeIndex;
    }

    public int getReadOffset() {
        return readOffset;
    }

    public void setReadOffset(int readIndex) {
        if (readIndex > writeOffset) {
            throw new RuntimeException("readIndex[" + readIndex + "] out of bounds exception: readOffset: " + readOffset +
                    ", writeOffset: " + writeOffset + "(expected: 0 <= readOffset <= writeOffset <= capacity:" + buffer.length + ")");
        }
        readOffset = readIndex;
    }

    public byte[] toBytes() {
        var bytes = new byte[writeOffset];
        System.arraycopy(buffer, 0, bytes, 0, writeOffset);
        return bytes;
    }

    public boolean isReadable() {
        return writeOffset > readOffset;
    }

    // -------------------------------------------------write/read-------------------------------------------------
    public void writeBool(boolean value) {
        ensureCapacity(1);
        buffer[writeOffset] = value ? (byte) 1 : (byte) 0;
        writeOffset++;
    }

    public boolean readBool() {
        var byteValue = buffer[readOffset];
        readOffset++;
        return byteValue == 1;
    }

    public void writeByte(byte value) {
        ensureCapacity(1);
        buffer[writeOffset++] = value;
    }

    public byte readByte() {
        return buffer[readOffset++];
    }

    public int getCapacity() {
        return buffer.length - writeOffset;
    }

    public void ensureCapacity(int capacity) {
        while (capacity - getCapacity() > 0) {
            var newSize = buffer.length * 2;
            if (newSize > MAX_SIZE) {
                throw new RuntimeException("Bytebuf max size is [655537], out of memory error");
            }

            var newBytes = new byte[newSize];
            System.arraycopy(buffer, 0, newBytes, 0, buffer.length);
            this.buffer = newBytes;
        }
    }

    public void writeBytes(byte[] bytes) {
        writeBytes(bytes, bytes.length);
    }

    public void writeBytes(byte[] bytes, int length) {
        ensureCapacity(length);
        System.arraycopy(bytes, 0, buffer, writeOffset, length);
        writeOffset += length;
    }

    public byte[] readBytes(int count) {
        var bytes = new byte[count];
        System.arraycopy(buffer, readOffset, bytes, 0, count);
        readOffset += count;
        return bytes;
    }

    public void writeShort(short value) {
        ensureCapacity(2);
        buffer[writeOffset++] = (byte) (value >>> 8);
        buffer[writeOffset++] = (byte) value;
    }

    public short readShort() {
        return (short) (buffer[readOffset++] << 8 | buffer[readOffset++] & 255);
    }

    // *******************************************int***************************************************
    public int writeInt(int value) {
        return writeVarInt((value << 1) ^ (value >> 31));
    }

    public int writeVarInt(int value) {
        int a = value >>> 7;
        if (a == 0) {
            writeByte((byte) value);
            return 1;
        }

        ensureCapacity(5);

        buffer[writeOffset++] = (byte) (value | 0x80);
        int b = value >>> 14;
        if (b == 0) {
            buffer[writeOffset++] = (byte) a;
            return 2;
        }

        buffer[writeOffset++] = (byte) (a | 0x80);
        a = value >>> 21;
        if (a == 0) {
            buffer[writeOffset++] = (byte) (b);
            return 3;
        }

        buffer[writeOffset++] = (byte) (b | 0x80);
        b = value >>> 28;
        if (b == 0) {
            buffer[writeOffset++] = (byte) (a);
            return 4;
        }

        buffer[writeOffset++] = (byte) (a | 0x80);
        buffer[writeOffset++] = (byte) b;
        return 5;
    }

    public int readInt() {
        int b = readByte();
        int value = b;
        if (b < 0) {
            b = readByte();
            value = value & 0x0000007F | b << 7;
            if (b < 0) {
                b = readByte();
                value = value & 0x00003FFF | b << 14;
                if (b < 0) {
                    b = readByte();
                    value = value & 0x001FFFFF | b << 21;
                    if (b < 0) {
                        value = value & 0x0FFFFFFF | readByte() << 28;
                    }
                }
            }
        }
        return ((value >>> 1) ^ -(value & 1));
    }

    public int writeIntCount(int value) {
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

    // 写入没有压缩的int
    public void writeRawInt(int value) {
        ensureCapacity(4);
        buffer[writeOffset++] = (byte) (value >>> 24);
        buffer[writeOffset++] = (byte) (value >>> 16);
        buffer[writeOffset++] = (byte) (value >>> 8);
        buffer[writeOffset++] = (byte) value;
    }

    // 读取没有压缩的int
    public int readRawInt() {
        return (buffer[readOffset++] & 255) << 24 | (buffer[readOffset++] & 255) << 16 | (buffer[readOffset++] & 255) << 8 | buffer[readOffset++] & 255;
    }

    // *******************************************long**************************************************
    public void writeLong(long value) {
        long mask = (value << 1) ^ (value >> 63);

        if (mask >>> 32 == 0) {
            writeVarInt((int) mask);
            return;
        }

        byte[] bytes = new byte[9];
        bytes[0] = (byte) (mask | 0x80);
        bytes[1] = (byte) (mask >>> 7 | 0x80);
        bytes[2] = (byte) (mask >>> 14 | 0x80);
        bytes[3] = (byte) (mask >>> 21 | 0x80);


        int a = (int) (mask >>> 28);
        int b = (int) (mask >>> 35);
        if (b == 0) {
            bytes[4] = (byte) a;
            writeBytes(bytes, 5);
            return;
        }

        bytes[4] = (byte) (a | 0x80);
        a = (int) (mask >>> 42);
        if (a == 0) {
            bytes[5] = (byte) b;
            writeBytes(bytes, 6);
            return;
        }

        bytes[5] = (byte) (b | 0x80);
        b = (int) (mask >>> 49);
        if (b == 0) {
            bytes[6] = (byte) a;
            writeBytes(bytes, 7);
            return;
        }

        bytes[6] = (byte) (a | 0x80);
        a = (int) (mask >>> 56);
        if (a == 0) {
            bytes[7] = (byte) b;
            writeBytes(bytes, 8);
            return;
        }

        bytes[7] = (byte) (b | 0x80);
        bytes[8] = (byte) a;
        writeBytes(bytes, 9);
    }

    public long readLong() {
        long b = readByte();
        long value = b;
        if (b < 0) {
            b = readByte();
            value = value & 0x00000000_0000007FL | b << 7;
            if (b < 0) {
                b = readByte();
                value = value & 0x00000000_00003FFFL | b << 14;
                if (b < 0) {
                    b = readByte();
                    value = value & 0x00000000_001FFFFFL | b << 21;
                    if (b < 0) {
                        b = readByte();
                        value = value & 0x00000000_0FFFFFFFL | b << 28;
                        if (b < 0) {
                            b = readByte();
                            value = value & 0x00000007_FFFFFFFFL | b << 35;
                            if (b < 0) {
                                b = readByte();
                                value = value & 0x000003FF_FFFFFFFFL | b << 42;
                                if (b < 0) {
                                    b = readByte();
                                    value = value & 0x0001FFFF_FFFFFFFFL | b << 49;
                                    if (b < 0) {
                                        b = readByte();
                                        value = value & 0x00FFFFFF_FFFFFFFFL | b << 56;
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

    public void writeRawLong(long value) {
        ensureCapacity(8);
        buffer[writeOffset++] = (byte) ((int) (value >>> 56));
        buffer[writeOffset++] = (byte) ((int) (value >>> 48));
        buffer[writeOffset++] = (byte) ((int) (value >>> 40));
        buffer[writeOffset++] = (byte) ((int) (value >>> 32));
        buffer[writeOffset++] = (byte) ((int) (value >>> 24));
        buffer[writeOffset++] = (byte) ((int) (value >>> 16));
        buffer[writeOffset++] = (byte) ((int) (value >>> 8));
        buffer[writeOffset++] = (byte) ((int) value);
    }

    public long readRawLong() {
        return ((long) buffer[readOffset++] & 255L) << 56 | ((long) buffer[readOffset++] & 255L) << 48 | ((long) buffer[readOffset++] & 255L) << 40 | ((long) buffer[readOffset++] & 255L) << 32 | ((long) buffer[readOffset++] & 255L) << 24 | ((long) buffer[readOffset++] & 255L) << 16 | ((long) buffer[readOffset++] & 255L) << 8 | (long) buffer[readOffset++] & 255L;
    }

    // *******************************************float***************************************************
    public void writeFloat(float value) {
        writeRawInt(Float.floatToRawIntBits(value));
    }

    public float readFloat() {
        return Float.intBitsToFloat(readRawInt());
    }

    // *******************************************double***************************************************
    public void writeDouble(double value) {
        writeRawLong(Double.doubleToRawLongBits(value));
    }

    public double readDouble() {
        return Double.longBitsToDouble(readRawLong());
    }

    // *******************************************String***************************************************
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);

    public void writeString(String value) {
        if (value == null || value.isEmpty()) {
            writeInt(0);
            return;
        }
        var bytes = value.getBytes(DEFAULT_CHARSET);
        writeInt(bytes.length);
        writeBytes(bytes);
    }

    public String readString() {
        int length = readInt();
        if (length <= 0) {
            return "";
        }
        byte[] bytes = readBytes(length);
        return new String(bytes, DEFAULT_CHARSET);
    }

    public void writeBoolArray(boolean[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeBool(array[index]);
            }
        }
    }

    public boolean[] readBoolArray() {
        int size = readInt();
        boolean[] array = new boolean[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readBool();
            }
        }
        return array;
    }

    public void writeByteArray(byte[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeByte(array[index]);
            }
        }
    }

    public byte[] readByteArray() {
        int size = readInt();
        byte[] array = new byte[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readByte();
            }
        }
        return array;
    }

    public void writeShortArray(short[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeShort(array[index]);
            }
        }
    }

    public short[] readShortArray() {
        int size = readInt();
        short[] array = new short[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readShort();
            }
        }
        return array;
    }

    public void writeIntArray(int[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeInt(array[index]);
            }
        }
    }

    public int[] readIntArray() {
        int size = readInt();
        int[] array = new int[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readInt();
            }
        }
        return array;
    }

    public void writeLongArray(long[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeLong(array[index]);
            }
        }
    }

    public long[] readLongArray() {
        int size = readInt();
        long[] array = new long[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readLong();
            }
        }
        return array;
    }

    public void writeFloatArray(float[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeFloat(array[index]);
            }
        }
    }

    public float[] readFloatArray() {
        int size = readInt();
        float[] array = new float[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readFloat();
            }
        }
        return array;
    }

    public void writeDoubleArray(double[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeDouble(array[index]);
            }
        }
    }

    public double[] readDoubleArray() {
        int size = readInt();
        double[] array = new double[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readDouble();
            }
        }
        return array;
    }

    public void writeStringArray(String[] array) {
        if ((array == null) || (array.length == 0)) {
            writeInt(0);
        } else {
            writeInt(array.length);
            int length = array.length;
            for (int index = 0; index < length; index++) {
                writeString(array[index]);
            }
        }
    }

    public String[] readStringArray() {
        int size = readInt();
        String[] array = new String[size];
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                array[index] = readString();
            }
        }
        return array;
    }

    public void writeBoolList(List<Boolean> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeBool(ele);
            }
        }
    }

    public List<Boolean> readBoolList() {
        int size = readInt();
        List<Boolean> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readBool());
            }
        }
        return list;
    }

    public void writeByteList(List<Byte> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeByte(ele);
            }
        }
    }

    public List<Byte> readByteList() {
        int size = readInt();
        List<Byte> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readByte());
            }
        }
        return list;
    }

    public void writeShortList(List<Short> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeShort(ele);
            }
        }
    }

    public List<Short> readShortList() {
        int size = readInt();
        List<Short> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readShort());
            }
        }
        return list;
    }

    public void writeIntList(List<Integer> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeInt(ele);
            }
        }
    }

    public List<Integer> readIntList() {
        int size = readInt();
        List<Integer> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readInt());
            }
        }
        return list;
    }

    public void writeLongList(List<Long> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeLong(ele);
            }
        }
    }

    public List<Long> readLongList() {
        int size = readInt();
        List<Long> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readLong());
            }
        }
        return list;
    }

    public void writeFloatList(List<Float> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeFloat(ele);
            }
        }
    }

    public List<Float> readFloatList() {
        int size = readInt();
        List<Float> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readFloat());
            }
        }
        return list;
    }

    public void writeDoubleList(List<Double> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeDouble(ele);
            }
        }
    }

    public List<Double> readDoubleList() {
        int size = readInt();
        List<Double> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readDouble());
            }
        }
        return list;
    }

    public void writeStringList(List<String> list) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(list.size());
            for (var ele : list) {
                writeString(ele);
            }
        }
    }

    public List<String> readStringList() {
        int size = readInt();
        List<String> list = new ArrayList<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                list.add(readString());
            }
        }
        return list;
    }

    public void writePacketList(List<?> list, short protocolId) {
        if ((list == null) || list.isEmpty()) {
            writeInt(0);
        } else {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            writeInt(list.size());
            for(var ele : list) {
                protocolRegistration.write(this, ele);
            }
        }
    }

    public <T> List<T> readPacketList(Class<T> clazz, short protocolId) {
        int size = readInt();
        List<T> list = new ArrayList<>();
        if (size > 0) {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (int index = 0; index < size; index++) {
                list.add((T) protocolRegistration.read(this));
            }
        }
        return list;
    }

    public void writeBoolSet(Set<Boolean> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeBool(ele);
            }
        }
    }

    public Set<Boolean> readBoolSet() {
        int size = readInt();
        Set<Boolean> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readBool());
            }
        }
        return set;
    }

    public void writeShortSet(Set<Short> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeShort(ele);
            }
        }
    }

    public Set<Short> readShortSet() {
        int size = readInt();
        Set<Short> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readShort());
            }
        }
        return set;
    }

    public void writeIntSet(Set<Integer> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeInt(ele);
            }
        }
    }

    public Set<Integer> readIntSet() {
        int size = readInt();
        Set<Integer> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readInt());
            }
        }
        return set;
    }

    public void writeLongSet(Set<Long> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeLong(ele);
            }
        }
    }

    public Set<Long> readLongSet() {
        int size = readInt();
        Set<Long> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readLong());
            }
        }
        return set;
    }

    public void writeFloatSet(Set<Float> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeFloat(ele);
            }
        }
    }

    public Set<Float> readFloatSet() {
        int size = readInt();
        Set<Float> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readFloat());
            }
        }
        return set;
    }

    public void writeDoubleSet(Set<Double> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeDouble(ele);
            }
        }
    }

    public Set<Double> readDoubleSet() {
        int size = readInt();
        Set<Double> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readDouble());
            }
        }
        return set;
    }

    public void writeStringSet(Set<String> set) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(set.size());
            for (var ele : set) {
                writeString(ele);
            }
        }
    }

    public Set<String> readStringSet() {
        int size = readInt();
        Set<String> set = new HashSet<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                set.add(readString());
            }
        }
        return set;
    }

    public void writePacketSet(Set<?> set, short protocolId) {
        if ((set == null) || set.isEmpty()) {
            writeInt(0);
        } else {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            writeInt(set.size());
            for(var element : set) {
                protocolRegistration.write(this, element);
            }
        }
    }

    public <T> Set<T> readPacketSet(Class<T> clazz, short protocolId) {
        int size = readInt();
        Set<T> set = new HashSet<>();
        if (size > 0) {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (int index = 0; index < size; index++) {
                set.add((T) protocolRegistration.read(this));
            }
        }
        return set;
    }

    public void writeIntIntMap(Map<Integer, Integer> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeInt(entry.getKey());
                writeInt(entry.getValue());
            }
        }
    }

    public Map<Integer, Integer> readIntIntMap() {
        int size = readInt();
        Map<Integer, Integer> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readInt();
                var value = readInt();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeIntLongMap(Map<Integer, Long> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeInt(entry.getKey());
                writeLong(entry.getValue());
            }
        }
    }

    public Map<Integer, Long> readIntLongMap() {
        int size = readInt();
        Map<Integer, Long> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readInt();
                var value = readLong();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeIntStringMap(Map<Integer, String> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeInt(entry.getKey());
                writeString(entry.getValue());
            }
        }
    }

    public Map<Integer, String> readIntStringMap() {
        int size = readInt();
        Map<Integer, String> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readInt();
                var value = readString();
                map.put(key, value);
            }
        }
        return map;
    }


    public void writeIntPacketMap(Map<Integer, ?> map, short protocolId) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            writeInt(map.size());
            for(var element : map.entrySet()) {
                writeInt(element.getKey());
                protocolRegistration.write(this, element.getValue());
            }
        }
    }

    public <T> Map<Integer, T> readIntPacketMap(Class<T> clazz, short protocolId) {
        int size = readInt();
        Map<Integer, T> map = new HashMap<>();
        if (size > 0) {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (int index = 0; index < size; index++) {
                var key = readInt();
                var value = (T) protocolRegistration.read(this);
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeLongIntMap(Map<Long, Integer> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeLong(entry.getKey());
                writeInt(entry.getValue());
            }
        }
    }

    public Map<Long, Integer> readLongIntMap() {
        int size = readInt();
        Map<Long, Integer> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readLong();
                var value = readInt();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeLongLongMap(Map<Long, Long> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeLong(entry.getKey());
                writeLong(entry.getValue());
            }
        }
    }

    public Map<Long, Long> readLongLongMap() {
        int size = readInt();
        Map<Long, Long> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readLong();
                var value = readLong();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeLongStringMap(Map<Long, String> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeLong(entry.getKey());
                writeString(entry.getValue());
            }
        }
    }

    public Map<Long, String> readLongStringMap() {
        int size = readInt();
        Map<Long, String> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readLong();
                var value = readString();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeLongPacketMap(Map<Long, ?> map, short protocolId) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            writeInt(map.size());
            for(var element : map.entrySet()) {
                writeLong(element.getKey());
                protocolRegistration.write(this, element.getValue());
            }
        }
    }

    public <T> Map<Long, T> readLongPacketMap(Class<T> clazz, short protocolId) {
        int size = readInt();
        Map<Long, T> map = new HashMap<>();
        if (size > 0) {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (int index = 0; index < size; index++) {
                var key = readLong();
                var value = (T) protocolRegistration.read(this);
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeStringIntMap(Map<String, Integer> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeString(entry.getKey());
                writeInt(entry.getValue());
            }
        }
    }

    public Map<String, Integer> readStringIntMap() {
        int size = readInt();
        Map<String, Integer> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readString();
                var value = readInt();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeStringLongMap(Map<String, Long> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeString(entry.getKey());
                writeLong(entry.getValue());
            }
        }
    }

    public Map<String, Long> readStringLongMap() {
        int size = readInt();
        Map<String, Long> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readString();
                var value = readLong();
                map.put(key, value);
            }
        }
        return map;
    }

    public void writeStringStringMap(Map<String, String> map) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            writeInt(map.size());
            for (var entry : map.entrySet()) {
                writeString(entry.getKey());
                writeString(entry.getValue());
            }
        }
    }

    public Map<String, String> readStringStringMap() {
        int size = readInt();
        Map<String, String> map = new HashMap<>();
        if (size > 0) {
            for (int index = 0; index < size; index++) {
                var key = readString();
                var value = readString();
                map.put(key, value);
            }
        }
        return map;
    }


    public void writeStringPacketMap(Map<String, ?> map, short protocolId) {
        if ((map == null) || map.isEmpty()) {
            writeInt(0);
        } else {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            writeInt(map.size());
            for (var element : map.entrySet()) {
                writeString(element.getKey());
                protocolRegistration.write(this, element.getValue());
            }
        }
    }

    public <T> Map<String, T> readStringPacketMap(Class<T> clazz, short protocolId) {
        int size = readInt();
        Map<String, T> map = new HashMap<>();
        if (size > 0) {
            IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (int index = 0; index < size; index++) {
                var key = readString();
                var value = (T) protocolRegistration.read(this);
                map.put(key, value);
            }
        }
        return map;
    }

    public void writePacket(Object packet, short protocolId) {
        IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
        protocolRegistration.write(this, packet);
    }

    public Object readPacket(short protocolId) {
        IProtocolRegistration protocolRegistration = ProtocolManager.getProtocol(protocolId);
        return protocolRegistration.read(this);
    }

}