/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.buffer;

import com.zfoo.protocol.collection.*;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.LongObjectHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * zfoo的自定义私有协议格式，可以非常方便的对大部分数据定制序列化和反序列化实现，可以对大部分数据结构定制高性能实现方式
 * <p>
 * “可变长字节码算法”的压缩数据的算法，压缩数据和减少磁盘IO。google的ProtocolBuf和Facebook的thrift底层的通信协议都是由这个算法实现
 *
 * @author godotg
 */
public abstract class ByteBufUtils {

    //---------------------------------compatible--------------------------------------
    public static final Double ZERO_DOUBLE = Double.valueOf(0D);
    public static final Float ZERO_FLOAT = Float.valueOf(0F);

    public static void adjustPadding(ByteBuf byteBuf, int predictionLength, int beforeWriteIndex) {
        // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
        var currentWriteIndex = byteBuf.writerIndex();
        var predictionCount = writeIntCount(predictionLength);
        var length = currentWriteIndex - beforeWriteIndex - predictionCount;
        var lengthCount = writeIntCount(length);
        var padding = lengthCount - predictionCount;
        if (padding == 0) {
            byteBuf.writerIndex(beforeWriteIndex);
            writeInt(byteBuf, length);
            byteBuf.writerIndex(currentWriteIndex);
        } else if (padding < 0) {
            var retainedByteBuf = byteBuf.retainedSlice(currentWriteIndex - length, length);
            byteBuf.writerIndex(beforeWriteIndex);
            writeInt(byteBuf, length);
            byteBuf.writeBytes(retainedByteBuf);
            ReferenceCountUtil.release(retainedByteBuf);
        } else {
            var retainedByteBuf = byteBuf.retainedSlice(currentWriteIndex - length, length);
            var bytes = readAllBytes(retainedByteBuf);
            byteBuf.writerIndex(beforeWriteIndex);
            writeInt(byteBuf, length);
            byteBuf.writeBytes(bytes);
            ReferenceCountUtil.release(retainedByteBuf);
        }
    }

    public static boolean compatibleRead(ByteBuf byteBuf, int beforeReadIndex, int length) {
        return length != -1 && byteBuf.readerIndex() < length + beforeReadIndex;
    }

    //---------------------------------boolean--------------------------------------
    public static void writeBoolean(ByteBuf byteBuf, boolean value) {
        byteBuf.writeBoolean(value);
    }

    public static boolean readBoolean(ByteBuf byteBuf) {
        return byteBuf.readBoolean();
    }

    public static boolean tryReadBoolean(ByteBuf byteBuf) {
        return byteBuf.isReadable() && readBoolean(byteBuf);
    }

    public static void writeBooleanBox(ByteBuf byteBuf, Boolean value) {
        byteBuf.writeBoolean(value != null && value);
    }

    public static Boolean readBooleanBox(ByteBuf byteBuf) {
        return byteBuf.readBoolean();
    }

    //---------------------------------byte--------------------------------------
    public static void writeByte(ByteBuf byteBuf, byte value) {
        byteBuf.writeByte(value);
    }

    public static byte readByte(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    public static void writeByteBox(ByteBuf byteBuf, Byte value) {
        byteBuf.writeByte(value == null ? 0 : value);
    }

    public static Byte readByteBox(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    public static byte[] readAllBytes(ByteBuf byteBuf) {
        var readableBytes = byteBuf.readableBytes();
        var bytes = new byte[readableBytes];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    //---------------------------------short--------------------------------------
    public static void writeShort(ByteBuf byteBuf, short value) {
        byteBuf.writeShort(value);
    }

    public static short readShort(ByteBuf byteBuf) {
        return byteBuf.readShort();
    }

    public static void writeShortBox(ByteBuf byteBuf, Short value) {
        byteBuf.writeShort(value == null ? 0 : value);
    }

    public static Short readShortBox(ByteBuf byteBuf) {
        return byteBuf.readShort();
    }

    //---------------------------------int--------------------------------------
    // 用Zigzag算法压缩int和long的值，再用Varint紧凑算法表示数字的有效位
    public static int writeInt(ByteBuf byteBuf, int value) {
        return writeVarInt(byteBuf, (value << 1) ^ (value >> 31));
    }

    private static int writeVarInt(ByteBuf byteBuf, int value) {
        int a = value >>> 7;
        if (a == 0) {
            byteBuf.writeByte(value);
            return 1;
        }

        int writeIndex = byteBuf.writerIndex();
        byteBuf.ensureWritable(5);

        byteBuf.setByte(writeIndex++, value | 0x80);
        int b = value >>> 14;
        if (b == 0) {
            byteBuf.setByte(writeIndex++, a);
            byteBuf.writerIndex(writeIndex);
            return 2;
        }

        byteBuf.setByte(writeIndex++, a | 0x80);
        a = value >>> 21;
        if (a == 0) {
            byteBuf.setByte(writeIndex++, b);
            byteBuf.writerIndex(writeIndex);
            return 3;
        }

        byteBuf.setByte(writeIndex++, b | 0x80);
        b = value >>> 28;
        if (b == 0) {
            byteBuf.setByte(writeIndex++, a);
            byteBuf.writerIndex(writeIndex);
            return 4;
        }

        byteBuf.setByte(writeIndex++, a | 0x80);
        byteBuf.setByte(writeIndex++, b);
        byteBuf.writerIndex(writeIndex);
        return 5;
    }

    public static int readInt(ByteBuf byteBuf) {
        int readIndex = byteBuf.readerIndex();
        int b = byteBuf.getByte(readIndex++);
        int value = b;
        if (b < 0) {
            b = byteBuf.getByte(readIndex++);
            value = value & 0x0000007F | b << 7;
            if (b < 0) {
                b = byteBuf.getByte(readIndex++);
                value = value & 0x00003FFF | b << 14;
                if (b < 0) {
                    b = byteBuf.getByte(readIndex++);
                    value = value & 0x001FFFFF | b << 21;
                    if (b < 0) {
                        value = value & 0x0FFFFFFF | byteBuf.getByte(readIndex++) << 28;
                    }
                }
            }
        }
        byteBuf.readerIndex(readIndex);
        return ((value >>> 1) ^ -(value & 1));
    }


    public static int writeIntCount(int value) {
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


    public static void writeIntBox(ByteBuf byteBuf, Integer value) {
        writeInt(byteBuf, value == null ? 0 : value);
    }

    public static Integer readIntBox(ByteBuf byteBuf) {
        return readInt(byteBuf);
    }

    //---------------------------------long--------------------------------------
    public static void writeLong(ByteBuf byteBuf, long value) {
        long mask = (value << 1) ^ (value >> 63);

        if (mask >>> 32 == 0) {
            writeVarInt(byteBuf, (int) mask);
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
            byteBuf.writeBytes(bytes, 0, 5);
            return;
        }

        bytes[4] = (byte) (a | 0x80);
        a = (int) (mask >>> 42);
        if (a == 0) {
            bytes[5] = (byte) b;
            byteBuf.writeBytes(bytes, 0, 6);
            return;
        }

        bytes[5] = (byte) (b | 0x80);
        b = (int) (mask >>> 49);
        if (b == 0) {
            bytes[6] = (byte) a;
            byteBuf.writeBytes(bytes, 0, 7);
            return;
        }

        bytes[6] = (byte) (a | 0x80);
        a = (int) (mask >>> 56);
        if (a == 0) {
            bytes[7] = (byte) b;
            byteBuf.writeBytes(bytes, 0, 8);
            return;
        }

        bytes[7] = (byte) (b | 0x80);
        bytes[8] = (byte) a;
        byteBuf.writeBytes(bytes, 0, 9);
    }


    public static long readLong(ByteBuf byteBuf) {
        int readIndex = byteBuf.readerIndex();
        long b = byteBuf.getByte(readIndex++);
        long value = b;
        if (b < 0) {
            b = byteBuf.getByte(readIndex++);
            value = value & 0x00000000_0000007FL | b << 7;
            if (b < 0) {
                b = byteBuf.getByte(readIndex++);
                value = value & 0x00000000_00003FFFL | b << 14;
                if (b < 0) {
                    b = byteBuf.getByte(readIndex++);
                    value = value & 0x00000000_001FFFFFL | b << 21;
                    if (b < 0) {
                        b = byteBuf.getByte(readIndex++);
                        value = value & 0x00000000_0FFFFFFFL | b << 28;
                        if (b < 0) {
                            b = byteBuf.getByte(readIndex++);
                            value = value & 0x00000007_FFFFFFFFL | b << 35;
                            if (b < 0) {
                                b = byteBuf.getByte(readIndex++);
                                value = value & 0x000003FF_FFFFFFFFL | b << 42;
                                if (b < 0) {
                                    b = byteBuf.getByte(readIndex++);
                                    value = value & 0x0001FFFF_FFFFFFFFL | b << 49;
                                    if (b < 0) {
                                        b = byteBuf.getByte(readIndex++);
                                        value = value & 0x00FFFFFF_FFFFFFFFL | b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        byteBuf.readerIndex(readIndex);
        return ((value >>> 1) ^ -(value & 1));
    }

    public static void writeLongBox(ByteBuf byteBuf, Long value) {
        writeLong(byteBuf, value == null ? 0L : value);
    }

    public static Long readLongBox(ByteBuf byteBuf) {
        return readLong(byteBuf);
    }

    //---------------------------------float--------------------------------------
    public static void writeFloat(ByteBuf byteBuf, float value) {
        byteBuf.writeFloat(value);
    }

    public static float readFloat(ByteBuf byteBuf) {
        return byteBuf.readFloat();
    }

    public static void writeFloatBox(ByteBuf byteBuf, Float value) {
        byteBuf.writeFloat(value == null ? 0F : value);
    }

    public static Float readFloatBox(ByteBuf byteBuf) {
        return byteBuf.readFloat();
    }

    //---------------------------------double--------------------------------------
    public static void writeDouble(ByteBuf byteBuf, double value) {
        byteBuf.writeDouble(value);
    }

    public static double readDouble(ByteBuf byteBuf) {
        return byteBuf.readDouble();
    }

    public static void writeDoubleBox(ByteBuf byteBuf, Double value) {
        byteBuf.writeDouble(value == null ? 0D : value);
    }

    public static Double readDoubleBox(ByteBuf byteBuf) {
        return byteBuf.readDouble();
    }

    //---------------------------------String--------------------------------------
    public static void writeString(ByteBuf byteBuf, String value) {
        if (StringUtils.isEmpty(value)) {
            writeInt(byteBuf, 0);
            return;
        }

        // 预估需要写入的字节数，并预留位置
        var beforeWriteIndex = byteBuf.writerIndex();
        var maxLength = ByteBufUtil.utf8MaxBytes(value);
        var writeIntCountByte = writeInt(byteBuf, maxLength);

        var length = byteBuf.writeCharSequence(value, StringUtils.DEFAULT_CHARSET);

        var currentWriteIndex = byteBuf.writerIndex();

        // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
        var padding = writeIntCountByte - writeIntCount(length);
        if (padding == 0) {
            byteBuf.writerIndex(beforeWriteIndex);
            writeInt(byteBuf, length);
            byteBuf.writerIndex(currentWriteIndex);
        } else {
            var retainedByteBuf = byteBuf.retainedSlice(currentWriteIndex - length, length);
            byteBuf.writerIndex(beforeWriteIndex);
            writeInt(byteBuf, length);
            byteBuf.writeBytes(retainedByteBuf);
            ReferenceCountUtil.release(retainedByteBuf);
        }
    }

    public static String readString(ByteBuf byteBuf) {
        int length = readInt(byteBuf);
        return length <= 0 ? StringUtils.EMPTY : (String) byteBuf.readCharSequence(length, StringUtils.DEFAULT_CHARSET);
    }


    //---------------------------------char--------------------------------------
    // 很多脚本语言没有char，所以这里使用string代替
    public static void writeChar(ByteBuf byteBuf, char value) {
        writeString(byteBuf, String.valueOf(value));
    }

    public static char readChar(ByteBuf byteBuf) {
        var value = readString(byteBuf);
        if (StringUtils.isEmpty(value)) {
            return Character.MIN_VALUE;
        }
        return value.charAt(0);
    }

    public static void writeCharBox(ByteBuf byteBuf, Character value) {
        writeChar(byteBuf, value == null ? Character.MIN_VALUE : value);
    }

    public static Character readCharBox(ByteBuf byteBuf) {
        return readChar(byteBuf);
    }

    //-----------------------------------------------------------------------
    //---------------------------------以下方法会被字节码生成的代码调用--------------------------------------
    public static void writePacketCollection(ByteBuf byteBuf, Collection<?> collection, IProtocolRegistration protocolRegistration) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var packet : collection) {
            protocolRegistration.write(byteBuf, packet);
        }
    }

    public static void writePacketList(ByteBuf byteBuf, List<?> list, IProtocolRegistration protocolRegistration) {
        writePacketCollection(byteBuf, list, protocolRegistration);
    }

    public static List<?> readPacketList(ByteBuf byteBuf, IProtocolRegistration protocolRegistration) {
        var length = readInt(byteBuf);
        List<Object> list = CollectionUtils.newList(length);
        for (var i = 0; i < length; i++) {
            list.add(protocolRegistration.read(byteBuf));
        }
        return list;
    }

    public static void writePacketSet(ByteBuf byteBuf, Set<?> set, IProtocolRegistration protocolRegistration) {
        writePacketCollection(byteBuf, set, protocolRegistration);
    }

    public static Set<?> readPacketSet(ByteBuf byteBuf, IProtocolRegistration protocolRegistration) {
        var length = readInt(byteBuf);
        Set<Object> set = CollectionUtils.newSet(length);
        for (var i = 0; i < length; i++) {
            set.add(protocolRegistration.read(byteBuf));
        }
        return set;
    }

    public static void writeIntIntMap(ByteBuf byteBuf, Map<Integer, Integer> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeIntBox(byteBuf, entry.getKey());
            writeIntBox(byteBuf, entry.getValue());
        }
    }

    public static Map<Integer, Integer> readIntIntMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var map = new HashMapIntInt(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.putPrimitive(readInt(byteBuf), readInt(byteBuf));
        }
        return map;
    }

    public static void writeIntLongMap(ByteBuf byteBuf, Map<Integer, Long> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeIntBox(byteBuf, entry.getKey());
            writeLongBox(byteBuf, entry.getValue());
        }
    }

    public static Map<Integer, Long> readIntLongMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var map = new HashMapIntLong(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.putPrimitive(readInt(byteBuf), readLong(byteBuf));
        }
        return map;
    }

    public static void writeIntStringMap(ByteBuf byteBuf, Map<Integer, String> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeIntBox(byteBuf, entry.getKey());
            writeString(byteBuf, entry.getValue());
        }
    }

    public static Map<Integer, String> readIntStringMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var map = new IntObjectHashMap<String>(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.put(readInt(byteBuf), readString(byteBuf));
        }
        return map;
    }

    public static void writeIntPacketMap(ByteBuf byteBuf, Map<Integer, ?> map, IProtocolRegistration protocolRegistration) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeIntBox(byteBuf, entry.getKey());
            protocolRegistration.write(byteBuf, entry.getValue());
        }
    }

    public static Map<Integer, ?> readIntPacketMap(ByteBuf byteBuf, IProtocolRegistration protocolRegistration) {
        var length = readInt(byteBuf);
        var map = new IntObjectHashMap<>(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.put(readInt(byteBuf), protocolRegistration.read(byteBuf));
        }
        return map;
    }

    public static void writeLongIntMap(ByteBuf byteBuf, Map<Long, Integer> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeLongBox(byteBuf, entry.getKey());
            writeIntBox(byteBuf, entry.getValue());
        }
    }

    public static Map<Long, Integer> readLongIntMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var map = new HashMapLongInt(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.putPrimitive(readLong(byteBuf), readInt(byteBuf));
        }
        return map;
    }

    public static void writeLongLongMap(ByteBuf byteBuf, Map<Long, Long> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeLongBox(byteBuf, entry.getKey());
            writeLongBox(byteBuf, entry.getValue());
        }
    }

    public static Map<Long, Long> readLongLongMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var map = new HashMapLongLong(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.putPrimitive(readLong(byteBuf), readLong(byteBuf));
        }
        return map;
    }

    public static void writeLongStringMap(ByteBuf byteBuf, Map<Long, String> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeLongBox(byteBuf, entry.getKey());
            writeString(byteBuf, entry.getValue());
        }
    }

    public static Map<Long, String> readLongStringMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var map = new LongObjectHashMap<String>(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.put(readLong(byteBuf), readString(byteBuf));
        }
        return map;
    }

    public static void writeLongPacketMap(ByteBuf byteBuf, Map<Long, ?> map, IProtocolRegistration protocolRegistration) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeLongBox(byteBuf, entry.getKey());
            protocolRegistration.write(byteBuf, entry.getValue());
        }
    }

    public static Map<Long, ?> readLongPacketMap(ByteBuf byteBuf, IProtocolRegistration protocolRegistration) {
        var length = readInt(byteBuf);
        var map = new LongObjectHashMap<>(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            map.put(readLong(byteBuf), protocolRegistration.read(byteBuf));
        }
        return map;
    }

    public static void writeStringIntMap(ByteBuf byteBuf, Map<String, Integer> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeString(byteBuf, entry.getKey());
            writeIntBox(byteBuf, entry.getValue());
        }
    }

    public static Map<String, Integer> readStringIntMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Map<String, Integer> map = CollectionUtils.newMap(length);
        for (var i = 0; i < length; i++) {
            map.put(readString(byteBuf), readIntBox(byteBuf));
        }
        return map;
    }

    public static void writeStringLongMap(ByteBuf byteBuf, Map<String, Long> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeString(byteBuf, entry.getKey());
            writeLongBox(byteBuf, entry.getValue());
        }
    }

    public static Map<String, Long> readStringLongMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Map<String, Long> map = CollectionUtils.newMap(length);
        for (var i = 0; i < length; i++) {
            map.put(readString(byteBuf), readLongBox(byteBuf));
        }
        return map;
    }

    public static void writeStringStringMap(ByteBuf byteBuf, Map<String, String> map) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeString(byteBuf, entry.getKey());
            writeString(byteBuf, entry.getValue());
        }
    }

    public static Map<String, String> readStringStringMap(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Map<String, String> map = CollectionUtils.newMap(length);
        for (var i = 0; i < length; i++) {
            map.put(readString(byteBuf), readString(byteBuf));
        }
        return map;
    }

    public static void writeStringPacketMap(ByteBuf byteBuf, Map<String, ?> map, IProtocolRegistration protocolRegistration) {
        if (map == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, map.size());
        for (var entry : map.entrySet()) {
            writeString(byteBuf, entry.getKey());
            protocolRegistration.write(byteBuf, entry.getValue());
        }
    }

    public static Map<String, ?> readStringPacketMap(ByteBuf byteBuf, IProtocolRegistration protocolRegistration) {
        var length = readInt(byteBuf);
        Map<String, Object> map = CollectionUtils.newMap(length);
        for (var i = 0; i < length; i++) {
            map.put(readString(byteBuf), protocolRegistration.read(byteBuf));
        }
        return map;
    }

    //---------------------------------boolean--------------------------------------
    public static void writeBooleanArray(ByteBuf byteBuf, boolean[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        var length = array.length;
        writeInt(byteBuf, length);
        var bytes = new byte[length];
        for (var i = 0; i < length; i++) {
            bytes[i] = (byte) (array[i] ? 1 : 0);
        }
        byteBuf.writeBytes(bytes);
    }

    public static boolean[] readBooleanArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var bytes = new byte[CollectionUtils.comfortableLength(length)];
        var array = new boolean[length];
        byteBuf.readBytes(bytes);
        for (var i = 0; i < length; i++) {
            array[i] = bytes[i] != 0;
        }
        return array;
    }

    public static void writeBooleanBoxArray(ByteBuf byteBuf, Boolean[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeBooleanBox(byteBuf, value);
        }
    }

    public static Boolean[] readBooleanBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var array = new Boolean[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            array[i] = readBooleanBox(byteBuf);
        }
        return array;
    }

    public static void writeBooleanCollection(ByteBuf byteBuf, Collection<Boolean> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeBooleanBox(byteBuf, value);
        }
    }

    public static void writeBooleanList(ByteBuf byteBuf, List<Boolean> list) {
        writeBooleanCollection(byteBuf, list);
    }

    public static List<Boolean> readBooleanList(ByteBuf byteBuf) {
        return new ArrayListBoolean(readBooleanArray(byteBuf));
    }

    public static void writeBooleanSet(ByteBuf byteBuf, Set<Boolean> set) {
        writeBooleanCollection(byteBuf, set);
    }

    public static Set<Boolean> readBooleanSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Set<Boolean> set = CollectionUtils.newSet(length);
        for (var i = 0; i < length; i++) {
            set.add(readBooleanBox(byteBuf));
        }
        return set;
    }

    //---------------------------------byte--------------------------------------
    public static void writeByteArray(ByteBuf byteBuf, byte[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        byteBuf.writeBytes(array);
    }

    public static byte[] readByteArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        if (length == 0) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        var bytes = new byte[CollectionUtils.comfortableLength(length)];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public static void writeByteBoxArray(ByteBuf byteBuf, Byte[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeByteBox(byteBuf, value);
        }
    }

    public static Byte[] readByteBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var bytesBox = new Byte[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            bytesBox[i] = readByteBox(byteBuf);
        }
        return bytesBox;
    }

    public static void writeByteCollection(ByteBuf byteBuf, Collection<Byte> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeByteBox(byteBuf, value);
        }
    }

    public static void writeByteList(ByteBuf byteBuf, List<Byte> list) {
        writeByteCollection(byteBuf, list);
    }

    public static List<Byte> readByteList(ByteBuf byteBuf) {
        return new ArrayListByte(readByteArray(byteBuf));
    }

    public static void writeByteSet(ByteBuf byteBuf, Set<Byte> set) {
        writeByteCollection(byteBuf, set);
    }

    public static Set<Byte> readByteSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var set = new HashSetByte(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            set.add(readByte(byteBuf));
        }
        return set;
    }

    //---------------------------------short--------------------------------------
    public static void writeShortArray(ByteBuf byteBuf, short[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        var length = array.length;
        writeInt(byteBuf, length);
        var writeIndex = byteBuf.writerIndex();
        byteBuf.ensureWritable(length * 2);
        for (var value : array) {
            byteBuf.setShort(writeIndex, value);
            writeIndex += 2;
        }
        byteBuf.writerIndex(writeIndex);
    }

    public static short[] readShortArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var shorts = new short[CollectionUtils.comfortableLength(length)];
        var readIndex = byteBuf.readerIndex();
        for (var i = 0; i < length; i++) {
            shorts[i] = byteBuf.getShort(readIndex);
            readIndex += 2;
        }
        byteBuf.readerIndex(readIndex);
        return shorts;
    }

    public static void writeShortBoxArray(ByteBuf byteBuf, Short[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeShortBox(byteBuf, value);
        }
    }

    public static Short[] readShortBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var shorts = new Short[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            shorts[i] = readShortBox(byteBuf);
        }
        return shorts;
    }

    public static void writeShortCollection(ByteBuf byteBuf, Collection<Short> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeShortBox(byteBuf, value);
        }
    }

    public static void writeShortList(ByteBuf byteBuf, List<Short> list) {
        writeShortCollection(byteBuf, list);
    }

    public static List<Short> readShortList(ByteBuf byteBuf) {
        return new ArrayListShort(readShortArray(byteBuf));
    }

    public static void writeShortSet(ByteBuf byteBuf, Set<Short> set) {
        writeShortCollection(byteBuf, set);
    }

    public static Set<Short> readShortSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var set = new HashSetShort(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            set.add(readShort(byteBuf));
        }
        return set;
    }


    //---------------------------------int--------------------------------------
    public static void writeIntArray(ByteBuf byteBuf, int[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeInt(byteBuf, value);
        }
    }

    public static int[] readIntArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var ints = new int[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            ints[i] = readInt(byteBuf);
        }
        return ints;
    }

    public static void writeIntBoxArray(ByteBuf byteBuf, Integer[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeIntBox(byteBuf, value);
        }
    }

    public static Integer[] readIntBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var ints = new Integer[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            ints[i] = readIntBox(byteBuf);
        }
        return ints;
    }

    public static void writeIntCollection(ByteBuf byteBuf, Collection<Integer> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeIntBox(byteBuf, value);
        }
    }

    public static void writeIntList(ByteBuf byteBuf, List<Integer> list) {
        writeIntCollection(byteBuf, list);
    }

    public static List<Integer> readIntList(ByteBuf byteBuf) {
        return new ArrayListInt(readIntArray(byteBuf));
    }

    public static void writeIntSet(ByteBuf byteBuf, Set<Integer> set) {
        writeIntCollection(byteBuf, set);
    }

    public static Set<Integer> readIntSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var set = new HashSetInt(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            set.add(readInt(byteBuf));
        }
        return set;
    }


    //---------------------------------long--------------------------------------
    public static void writeLongArray(ByteBuf byteBuf, long[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeLong(byteBuf, value);
        }
    }

    public static long[] readLongArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var longs = new long[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            longs[i] = readLong(byteBuf);
        }
        return longs;
    }

    public static void writeLongBoxArray(ByteBuf byteBuf, Long[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeLongBox(byteBuf, value);
        }
    }

    public static Long[] readLongBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var longs = new Long[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            longs[i] = readLongBox(byteBuf);
        }
        return longs;
    }

    public static void writeLongCollection(ByteBuf byteBuf, Collection<Long> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeLongBox(byteBuf, value);
        }
    }

    public static void writeLongList(ByteBuf byteBuf, List<Long> list) {
        writeLongCollection(byteBuf, list);
    }

    public static List<Long> readLongList(ByteBuf byteBuf) {
        return new ArrayListLong(readLongArray(byteBuf));
    }

    public static void writeLongSet(ByteBuf byteBuf, Set<Long> set) {
        writeLongCollection(byteBuf, set);
    }

    public static Set<Long> readLongSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var set = new HashSetLong(CollectionUtils.comfortableCapacity(length));
        for (var i = 0; i < length; i++) {
            set.add(readLong(byteBuf));
        }
        return set;
    }

    //---------------------------------float--------------------------------------
    public static void writeFloatArray(ByteBuf byteBuf, float[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        var length = array.length;
        writeInt(byteBuf, length);
        var writeIndex = byteBuf.writerIndex();
        byteBuf.ensureWritable(length * 4);
        for (var value : array) {
            byteBuf.setFloat(writeIndex, value);
            writeIndex += 4;
        }
        byteBuf.writerIndex(writeIndex);
    }

    public static float[] readFloatArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var floats = new float[CollectionUtils.comfortableLength(length)];
        var readIndex = byteBuf.readerIndex();
        for (var i = 0; i < length; i++) {
            floats[i] = byteBuf.getFloat(readIndex);
            readIndex += 4;
        }
        byteBuf.readerIndex(readIndex);
        return floats;
    }

    public static void writeFloatBoxArray(ByteBuf byteBuf, Float[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeFloatBox(byteBuf, value);
        }
    }

    public static Float[] readFloatBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var floats = new Float[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            floats[i] = readFloatBox(byteBuf);
        }
        return floats;
    }

    public static void writeFloatCollection(ByteBuf byteBuf, Collection<Float> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeFloatBox(byteBuf, value);
        }
    }

    public static void writeFloatList(ByteBuf byteBuf, List<Float> list) {
        writeFloatCollection(byteBuf, list);
    }

    public static List<Float> readFloatList(ByteBuf byteBuf) {
        return new ArrayListFloat(readFloatArray(byteBuf));
    }

    public static void writeFloatSet(ByteBuf byteBuf, Set<Float> set) {
        writeFloatCollection(byteBuf, set);
    }

    public static Set<Float> readFloatSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Set<Float> set = CollectionUtils.newSet(length);
        for (var i = 0; i < length; i++) {
            set.add(readFloatBox(byteBuf));
        }
        return set;
    }

    //---------------------------------double--------------------------------------
    public static void writeDoubleArray(ByteBuf byteBuf, double[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        var length = array.length;
        writeInt(byteBuf, length);
        var writeIndex = byteBuf.writerIndex();
        byteBuf.ensureWritable(length * 8);
        for (var value : array) {
            byteBuf.setDouble(writeIndex, value);
            writeIndex += 8;
        }
        byteBuf.writerIndex(writeIndex);
    }

    public static double[] readDoubleArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var doubles = new double[CollectionUtils.comfortableLength(length)];
        var readIndex = byteBuf.readerIndex();
        for (var i = 0; i < length; i++) {
            doubles[i] = byteBuf.getDouble(readIndex);
            readIndex += 8;
        }
        byteBuf.readerIndex(readIndex);
        return doubles;
    }

    public static void writeDoubleBoxArray(ByteBuf byteBuf, Double[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeDoubleBox(byteBuf, value);
        }
    }

    public static Double[] readDoubleBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var doubles = new Double[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            doubles[i] = readDoubleBox(byteBuf);
        }
        return doubles;
    }

    public static void writeDoubleCollection(ByteBuf byteBuf, Collection<Double> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeDoubleBox(byteBuf, value);
        }
    }

    public static void writeDoubleList(ByteBuf byteBuf, List<Double> list) {
        writeDoubleCollection(byteBuf, list);
    }

    public static List<Double> readDoubleList(ByteBuf byteBuf) {
        return new ArrayListDouble(readDoubleArray(byteBuf));
    }

    public static void writeDoubleSet(ByteBuf byteBuf, Set<Double> set) {
        writeDoubleCollection(byteBuf, set);
    }

    public static Set<Double> readDoubleSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Set<Double> set = CollectionUtils.newSet(length);
        for (var i = 0; i < length; i++) {
            set.add(readDoubleBox(byteBuf));
        }
        return set;
    }

    //---------------------------------string--------------------------------------
    public static void writeStringArray(ByteBuf byteBuf, String[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeString(byteBuf, value);
        }
    }

    public static String[] readStringArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var strings = new String[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            strings[i] = readString(byteBuf);
        }
        return strings;
    }

    public static void writeStringCollection(ByteBuf byteBuf, Collection<String> collection) {
        if (collection == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, collection.size());
        for (var value : collection) {
            writeString(byteBuf, value);
        }
    }

    public static void writeStringList(ByteBuf byteBuf, List<String> list) {
        writeStringCollection(byteBuf, list);
    }

    public static List<String> readStringList(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        List<String> list = CollectionUtils.newList(length);
        for (var i = 0; i < length; i++) {
            list.add(readString(byteBuf));
        }
        return list;
    }

    public static void writeStringSet(ByteBuf byteBuf, Set<String> set) {
        writeStringCollection(byteBuf, set);
    }

    public static Set<String> readStringSet(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        Set<String> set = CollectionUtils.newSet(length);
        for (var i = 0; i < length; i++) {
            set.add(readString(byteBuf));
        }
        return set;
    }


    //---------------------------------char--------------------------------------
    public static void writeCharArray(ByteBuf byteBuf, char[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeChar(byteBuf, value);
        }
    }

    public static char[] readCharArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var chars = new char[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            chars[i] = readChar(byteBuf);
        }
        return chars;
    }

    public static void writeCharBoxArray(ByteBuf byteBuf, Character[] array) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            writeCharBox(byteBuf, value);
        }
    }

    public static Character[] readCharBoxArray(ByteBuf byteBuf) {
        var length = readInt(byteBuf);
        var chars = new Character[CollectionUtils.comfortableLength(length)];
        for (var i = 0; i < length; i++) {
            chars[i] = readCharBox(byteBuf);
        }
        return chars;
    }

    public static <T> void writePacketArray(ByteBuf byteBuf, T[] array, IProtocolRegistration protocolRegistration) {
        if (array == null) {
            byteBuf.writeByte(0);
            return;
        }
        writeInt(byteBuf, array.length);
        for (var value : array) {
            protocolRegistration.write(byteBuf, value);
        }
    }

}
