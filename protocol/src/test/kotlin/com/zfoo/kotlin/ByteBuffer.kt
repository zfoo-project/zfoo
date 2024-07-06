package com.zfoo.kotlin

class ByteBuffer {
    private var buffer = ByteArray(INIT_SIZE)
    private var writeOffset = 0
    private var readOffset = 0
    fun adjustPadding(predictionLength: Int, beforewriteIndex: Int) {
        // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
        val currentwriteIndex = writeOffset
        val predictionCount = writeIntCount(predictionLength)
        val length = currentwriteIndex - beforewriteIndex - predictionCount
        val lengthCount = writeIntCount(length)
        val padding = lengthCount - predictionCount
        if (padding == 0) {
            writeOffset = beforewriteIndex
            writeInt(length)
            writeOffset = currentwriteIndex
        } else {
            val bytes = ByteArray(length)
            System.arraycopy(buffer, currentwriteIndex - length, bytes, 0, length)
            writeOffset = beforewriteIndex
            writeInt(length)
            writeBytes(bytes)
        }
    }

    fun compatibleRead(beforeReadIndex: Int, length: Int): Boolean {
        return length != -1 && readOffset < length + beforeReadIndex
    }

    // -------------------------------------------------get/set-------------------------------------------------
    fun writeOffset(): Int {
        return writeOffset
    }

    fun setWriteOffset(writeIndex: Int) {
        if (writeIndex > buffer.size) {
            throw RuntimeException(
                "writeIndex[" + writeIndex + "] out of bounds exception: readerIndex: " + readOffset +
                        ", writerIndex: " + writeOffset + "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + buffer.size
            )
        }
        writeOffset = writeIndex
    }

    fun readOffset(): Int {
        return readOffset
    }

    fun setReadOffset(readIndex: Int) {
        if (readIndex > writeOffset) {
            throw RuntimeException(
                "readIndex[" + readIndex + "] out of bounds exception: readerIndex: " + readOffset +
                        ", writerIndex: " + writeOffset + "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + buffer.size
            )
        }
        readOffset = readIndex
    }

    fun toBytes(): ByteArray {
        val bytes = ByteArray(writeOffset)
        System.arraycopy(buffer, 0, bytes, 0, writeOffset)
        return bytes
    }

    val isReadable: Boolean
        get() = writeOffset > readOffset

    // -------------------------------------------------write/read-------------------------------------------------
    fun writeBool(value: Boolean) {
        ensureCapacity(1)
        buffer[writeOffset] = if (value) 1.toByte() else 0.toByte()
        writeOffset++
    }

    fun readBool(): Boolean {
        val byteValue = buffer[readOffset]
        readOffset++
        return byteValue.toInt() == 1
    }

    fun writeByte(value: Byte) {
        ensureCapacity(1)
        buffer[writeOffset++] = value
    }

    fun readByte(): Byte {
        return buffer[readOffset++]
    }

    val capacity: Int
        get() = buffer.size - writeOffset

    fun ensureCapacity(capacity: Int) {
        while (capacity - this.capacity > 0) {
            val newSize = buffer.size * 2
            if (newSize > MAX_SIZE) {
                throw RuntimeException("Bytebuf max size is [655537], out of memory error")
            }
            val newBytes = ByteArray(newSize)
            System.arraycopy(buffer, 0, newBytes, 0, buffer.size)
            buffer = newBytes
        }
    }

    @JvmOverloads
    fun writeBytes(bytes: ByteArray, length: Int = bytes.size) {
        ensureCapacity(length)
        System.arraycopy(bytes, 0, buffer, writeOffset, length)
        writeOffset += length
    }

    fun readBytes(count: Int): ByteArray {
        val bytes = ByteArray(count)
        System.arraycopy(buffer, readOffset, bytes, 0, count)
        readOffset += count
        return bytes
    }

    fun writeShort(value: Short) {
        ensureCapacity(2)
        buffer[writeOffset++] = (value.toInt() ushr 8).toByte()
        buffer[writeOffset++] = value.toByte()
    }

    fun readShort(): Short {
        return (buffer[readOffset++].toInt() shl 8 or (buffer[readOffset++].toInt() and 255)).toShort()
    }

    // *******************************************int***************************************************
    fun writeInt(value: Int): Int {
        return writeVarInt(value shl 1 xor (value shr 31))
    }

    fun writeVarInt(value: Int): Int {
        var a = value ushr 7
        if (a == 0) {
            writeByte(value.toByte())
            return 1
        }
        ensureCapacity(5)
        buffer[writeOffset++] = (value or 0x80).toByte()
        var b = value ushr 14
        if (b == 0) {
            buffer[writeOffset++] = a.toByte()
            return 2
        }
        buffer[writeOffset++] = (a or 0x80).toByte()
        a = value ushr 21
        if (a == 0) {
            buffer[writeOffset++] = b.toByte()
            return 3
        }
        buffer[writeOffset++] = (b or 0x80).toByte()
        b = value ushr 28
        if (b == 0) {
            buffer[writeOffset++] = a.toByte()
            return 4
        }
        buffer[writeOffset++] = (a or 0x80).toByte()
        buffer[writeOffset++] = b.toByte()
        return 5
    }

    fun readInt(): Int {
        var b = readByte().toInt()
        var value = b
        if (b < 0) {
            b = readByte().toInt()
            value = value and 0x0000007F or (b shl 7)
            if (b < 0) {
                b = readByte().toInt()
                value = value and 0x00003FFF or (b shl 14)
                if (b < 0) {
                    b = readByte().toInt()
                    value = value and 0x001FFFFF or (b shl 21)
                    if (b < 0) {
                        value = value and 0x0FFFFFFF or (readByte().toInt() shl 28)
                    }
                }
            }
        }
        return value ushr 1 xor -(value and 1)
    }

    fun writeIntCount(value: Int): Int {
        var value = value
        value = value shl 1 xor (value shr 31)
        if (value ushr 7 == 0) {
            return 1
        }
        if (value ushr 14 == 0) {
            return 2
        }
        if (value ushr 21 == 0) {
            return 3
        }
        return if (value ushr 28 == 0) {
            4
        } else 5
    }

    // 写入没有压缩的int
    fun writeRawInt(value: Int) {
        ensureCapacity(4)
        buffer[writeOffset++] = (value ushr 24).toByte()
        buffer[writeOffset++] = (value ushr 16).toByte()
        buffer[writeOffset++] = (value ushr 8).toByte()
        buffer[writeOffset++] = value.toByte()
    }

    // 读取没有压缩的int
    fun readRawInt(): Int {
        return buffer[readOffset++].toInt() and 255 shl 24 or (buffer[readOffset++].toInt() and 255 shl 16) or (buffer[readOffset++].toInt() and 255 shl 8) or (buffer[readOffset++].toInt() and 255)
    }

    // *******************************************long**************************************************
    fun writeLong(value: Long) {
        val mask = value shl 1 xor (value shr 63)
        if (mask ushr 32 == 0L) {
            writeVarInt(mask.toInt())
            return
        }
        val bytes = ByteArray(9)
        bytes[0] = (mask or 0x80L).toByte()
        bytes[1] = (mask ushr 7 or 0x80L).toByte()
        bytes[2] = (mask ushr 14 or 0x80L).toByte()
        bytes[3] = (mask ushr 21 or 0x80L).toByte()
        var a = (mask ushr 28).toInt()
        var b = (mask ushr 35).toInt()
        if (b == 0) {
            bytes[4] = a.toByte()
            writeBytes(bytes, 5)
            return
        }
        bytes[4] = (a or 0x80).toByte()
        a = (mask ushr 42).toInt()
        if (a == 0) {
            bytes[5] = b.toByte()
            writeBytes(bytes, 6)
            return
        }
        bytes[5] = (b or 0x80).toByte()
        b = (mask ushr 49).toInt()
        if (b == 0) {
            bytes[6] = a.toByte()
            writeBytes(bytes, 7)
            return
        }
        bytes[6] = (a or 0x80).toByte()
        a = (mask ushr 56).toInt()
        if (a == 0) {
            bytes[7] = b.toByte()
            writeBytes(bytes, 8)
            return
        }
        bytes[7] = (b or 0x80).toByte()
        bytes[8] = a.toByte()
        writeBytes(bytes, 9)
    }

    fun readLong(): Long {
        var b = readByte().toLong()
        var value = b
        if (b < 0) {
            b = readByte().toLong()
            value = value and 0x000000000000007FL or (b shl 7)
            if (b < 0) {
                b = readByte().toLong()
                value = value and 0x0000000000003FFFL or (b shl 14)
                if (b < 0) {
                    b = readByte().toLong()
                    value = value and 0x00000000001FFFFFL or (b shl 21)
                    if (b < 0) {
                        b = readByte().toLong()
                        value = value and 0x000000000FFFFFFFL or (b shl 28)
                        if (b < 0) {
                            b = readByte().toLong()
                            value = value and 0x00000007FFFFFFFFL or (b shl 35)
                            if (b < 0) {
                                b = readByte().toLong()
                                value = value and 0x000003FFFFFFFFFFL or (b shl 42)
                                if (b < 0) {
                                    b = readByte().toLong()
                                    value = value and 0x0001FFFFFFFFFFFFL or (b shl 49)
                                    if (b < 0) {
                                        b = readByte().toLong()
                                        value = value and 0x00FFFFFFFFFFFFFFL or (b shl 56)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return value ushr 1 xor -(value and 1L)
    }

    fun writeRawLong(value: Long) {
        ensureCapacity(8)
        buffer[writeOffset++] = (value ushr 56).toInt().toByte()
        buffer[writeOffset++] = (value ushr 48).toInt().toByte()
        buffer[writeOffset++] = (value ushr 40).toInt().toByte()
        buffer[writeOffset++] = (value ushr 32).toInt().toByte()
        buffer[writeOffset++] = (value ushr 24).toInt().toByte()
        buffer[writeOffset++] = (value ushr 16).toInt().toByte()
        buffer[writeOffset++] = (value ushr 8).toInt().toByte()
        buffer[writeOffset++] = value.toInt().toByte()
    }

    fun readRawLong(): Long {
        return buffer[readOffset++].toLong() and 255L shl 56 or (buffer[readOffset++].toLong() and 255L shl 48) or (buffer[readOffset++].toLong() and 255L shl 40) or (buffer[readOffset++].toLong() and 255L shl 32) or (buffer[readOffset++].toLong() and 255L shl 24) or (buffer[readOffset++].toLong() and 255L shl 16) or (buffer[readOffset++].toLong() and 255L shl 8) or (buffer[readOffset++].toLong() and 255L)
    }

    // *******************************************float***************************************************
    fun writeFloat(value: Float) {
        writeRawInt(value.toBits())
    }

    fun readFloat(): Float {
        return Float.fromBits(readRawInt())
    }

    // *******************************************double***************************************************
    fun writeDouble(value: Double) {
        writeRawLong(value.toBits())
    }

    fun readDouble(): Double {
        return Double.fromBits(readRawLong())
    }

    fun writeString(value: String?) {
        if (value == null || value.isEmpty()) {
            writeInt(0)
            return
        }
        val bytes = value.toByteArray()
        writeInt(bytes.size)
        writeBytes(bytes)
    }

    fun readString(): String {
        val length = readInt()
        if (length <= 0) {
            return ""
        }
        val bytes = readBytes(length)
        return String(bytes)
    }

    fun writeBooleanArray(array: Array<Boolean>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeBool(array[index])
            }
        }
    }

    fun readBooleanArray(): Array<Boolean> {
        val size = readInt()
        val array = Array<Boolean>(size) { init -> false }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readBool()
            }
        }
        return array
    }

    fun writeByteArray(array: Array<Byte>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeByte(array[index])
            }
        }
    }

    fun readByteArray(): Array<Byte> {
        val size = readInt()
        val array = Array<Byte>(size) { init -> 0 }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readByte()
            }
        }
        return array
    }

    fun writeShortArray(array: Array<Short>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeShort(array[index])
            }
        }
    }

    fun readShortArray(): Array<Short> {
        val size = readInt()
        val array = Array<Short>(size) { init -> 0 }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readShort()
            }
        }
        return array
    }

    fun writeIntArray(array: Array<Int>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeInt(array[index])
            }
        }
    }

    fun readIntArray(): Array<Int> {
        val size = readInt()
        val array = Array<Int>(size) { init -> 0 }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readInt()
            }
        }
        return array
    }

    fun writeLongArray(array: Array<Long>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeLong(array[index])
            }
        }
    }

    fun readLongArray(): Array<Long> {
        val size = readInt()
        val array = Array<Long>(size) { init -> 0 }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readLong()
            }
        }
        return array
    }

    fun writeFloatArray(array: Array<Float>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeFloat(array[index])
            }
        }
    }

    fun readFloatArray(): Array<Float> {
        val size = readInt()
        val array = Array<Float>(size) { init -> 0F }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readFloat()
            }
        }
        return array
    }

    fun writeDoubleArray(array: Array<Double>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeDouble(array[index])
            }
        }
    }

    fun readDoubleArray(): Array<Double> {
        val size = readInt()
        val array = Array<Double>(size) { init -> 0.0 }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readDouble()
            }
        }
        return array
    }

    fun writeStringArray(array: Array<String>) {
        if (array.size == 0) {
            writeInt(0)
        } else {
            writeInt(array.size)
            val length = array.size
            for (index in 0 until length) {
                writeString(array[index])
            }
        }
    }

    fun readStringArray(): Array<String> {
        val size = readInt()
        val array = Array<String>(size) { init -> "" }
        if (size > 0) {
            for (index in 0 until size) {
                array[index] = readString()
            }
        }
        return array
    }

    fun writeBooleanList(list: List<Boolean>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeBool(ele)
            }
        }
    }

    fun readBooleanList(): List<Boolean> {
        val size = readInt()
        val list: MutableList<Boolean> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readBool())
            }
        }
        return list
    }

    fun writeByteList(list: List<Byte>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeByte(ele)
            }
        }
    }

    fun readByteList(): List<Byte> {
        val size = readInt()
        val list: MutableList<Byte> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readByte())
            }
        }
        return list
    }

    fun writeShortList(list: List<Short>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeShort(ele)
            }
        }
    }

    fun readShortList(): List<Short> {
        val size = readInt()
        val list: MutableList<Short> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readShort())
            }
        }
        return list
    }

    fun writeIntList(list: List<Int>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeInt(ele)
            }
        }
    }

    fun readIntList(): List<Int> {
        val size = readInt()
        val list: MutableList<Int> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readInt())
            }
        }
        return list
    }

    fun writeLongList(list: List<Long>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeLong(ele)
            }
        }
    }

    fun readLongList(): List<Long> {
        val size = readInt()
        val list: MutableList<Long> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readLong())
            }
        }
        return list
    }

    fun writeFloatList(list: List<Float>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeFloat(ele)
            }
        }
    }

    fun readFloatList(): List<Float> {
        val size = readInt()
        val list: MutableList<Float> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readFloat())
            }
        }
        return list
    }

    fun writeDoubleList(list: List<Double>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeDouble(ele)
            }
        }
    }

    fun readDoubleList(): List<Double> {
        val size = readInt()
        val list: MutableList<Double> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readDouble())
            }
        }
        return list
    }

    fun writeStringList(list: List<String>) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(list.size)
            for (ele in list) {
                writeString(ele)
            }
        }
    }

    fun readStringList(): List<String> {
        val size = readInt()
        val list: MutableList<String> = ArrayList()
        if (size > 0) {
            for (index in 0 until size) {
                list.add(readString())
            }
        }
        return list
    }

    fun writePacketList(list: List<*>, protocolId: Short) {
        if (list.isEmpty()) {
            writeInt(0)
        } else {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            writeInt(list.size)
            for (ele in list) {
                protocolRegistration.write(this, ele)
            }
        }
    }

    fun <T> readPacketList(clazz: Class<T>, protocolId: Short): List<T> {
        val size = readInt()
        val list: MutableList<T> = ArrayList()
        if (size > 0) {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            for (index in 0 until size) {
                list.add(protocolRegistration.read(this) as T)
            }
        }
        return list
    }

    fun writeBooleanSet(set: Set<Boolean>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeBool(ele)
            }
        }
    }

    fun readBooleanSet(): Set<Boolean> {
        val size = readInt()
        val set: MutableSet<Boolean> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readBool())
            }
        }
        return set
    }

    fun writeShortSet(set: Set<Short>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeShort(ele)
            }
        }
    }

    fun readShortSet(): Set<Short> {
        val size = readInt()
        val set: MutableSet<Short> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readShort())
            }
        }
        return set
    }

    fun writeIntSet(set: Set<Int>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeInt(ele)
            }
        }
    }

    fun readIntSet(): Set<Int> {
        val size = readInt()
        val set: MutableSet<Int> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readInt())
            }
        }
        return set
    }

    fun writeLongSet(set: Set<Long>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeLong(ele)
            }
        }
    }

    fun readLongSet(): Set<Long> {
        val size = readInt()
        val set: MutableSet<Long> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readLong())
            }
        }
        return set
    }

    fun writeFloatSet(set: Set<Float>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeFloat(ele)
            }
        }
    }

    fun readFloatSet(): Set<Float> {
        val size = readInt()
        val set: MutableSet<Float> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readFloat())
            }
        }
        return set
    }

    fun writeDoubleSet(set: Set<Double>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeDouble(ele)
            }
        }
    }

    fun readDoubleSet(): Set<Double> {
        val size = readInt()
        val set: MutableSet<Double> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readDouble())
            }
        }
        return set
    }

    fun writeStringSet(set: Set<String>) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(set.size)
            for (ele in set) {
                writeString(ele)
            }
        }
    }

    fun readStringSet(): Set<String> {
        val size = readInt()
        val set: MutableSet<String> = HashSet()
        if (size > 0) {
            for (index in 0 until size) {
                set.add(readString())
            }
        }
        return set
    }

    fun writePacketSet(set: Set<*>, protocolId: Short) {
        if (set.isEmpty()) {
            writeInt(0)
        } else {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            writeInt(set.size)
            for (element in set) {
                protocolRegistration.write(this, element)
            }
        }
    }

    fun <T> readPacketSet(clazz: Class<T>, protocolId: Short): Set<T> {
        val size = readInt()
        val set: MutableSet<T> = HashSet()
        if (size > 0) {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            for (index in 0 until size) {
                set.add(protocolRegistration.read(this) as T)
            }
        }
        return set
    }

    fun writeIntIntMap(map: Map<Int, Int>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeInt(key)
                writeInt(value)
            }
        }
    }

    fun readIntIntMap(): Map<Int, Int> {
        val size = readInt()
        val map: MutableMap<Int, Int> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readInt()
                val value = readInt()
                map[key] = value
            }
        }
        return map
    }

    fun writeIntLongMap(map: Map<Int, Long>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeInt(key)
                writeLong(value)
            }
        }
    }

    fun readIntLongMap(): Map<Int, Long> {
        val size = readInt()
        val map: MutableMap<Int, Long> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readInt()
                val value = readLong()
                map[key] = value
            }
        }
        return map
    }

    fun writeIntStringMap(map: Map<Int, String>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeInt(key)
                writeString(value)
            }
        }
    }

    fun readIntStringMap(): Map<Int, String> {
        val size = readInt()
        val map: MutableMap<Int, String> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readInt()
                val value = readString()
                map[key] = value
            }
        }
        return map
    }

    fun writeIntPacketMap(map: Map<Int, *>, protocolId: Short) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            writeInt(map.size)
            for ((key, value) in map) {
                writeInt(key)
                protocolRegistration.write(this, value)
            }
        }
    }

    fun <T> readIntPacketMap(clazz: Class<T>, protocolId: Short): Map<Int, T> {
        val size = readInt()
        val map: MutableMap<Int, T> = HashMap()
        if (size > 0) {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            for (index in 0 until size) {
                val key = readInt()
                val value = protocolRegistration.read(this) as T
                map[key] = value
            }
        }
        return map
    }

    fun writeLongIntMap(map: Map<Long, Int>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeLong(key)
                writeInt(value)
            }
        }
    }

    fun readLongIntMap(): Map<Long, Int> {
        val size = readInt()
        val map: MutableMap<Long, Int> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readLong()
                val value = readInt()
                map[key] = value
            }
        }
        return map
    }

    fun writeLongLongMap(map: Map<Long, Long>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeLong(key)
                writeLong(value)
            }
        }
    }

    fun readLongLongMap(): Map<Long, Long> {
        val size = readInt()
        val map: MutableMap<Long, Long> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readLong()
                val value = readLong()
                map[key] = value
            }
        }
        return map
    }

    fun writeLongStringMap(map: Map<Long, String>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeLong(key)
                writeString(value)
            }
        }
    }

    fun readLongStringMap(): Map<Long, String> {
        val size = readInt()
        val map: MutableMap<Long, String> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readLong()
                val value = readString()
                map[key] = value
            }
        }
        return map
    }

    fun writeLongPacketMap(map: Map<Long, *>, protocolId: Short) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            writeInt(map.size)
            for ((key, value) in map) {
                writeLong(key)
                protocolRegistration.write(this, value)
            }
        }
    }

    fun <T> readLongPacketMap(clazz: Class<T>, protocolId: Short): Map<Long, T> {
        val size = readInt()
        val map: MutableMap<Long, T> = HashMap()
        if (size > 0) {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            for (index in 0 until size) {
                val key = readLong()
                val value = protocolRegistration.read(this) as T
                map[key] = value
            }
        }
        return map
    }

    fun writeStringIntMap(map: Map<String, Int>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeString(key)
                writeInt(value)
            }
        }
    }

    fun readStringIntMap(): Map<String, Int> {
        val size = readInt()
        val map: MutableMap<String, Int> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readString()
                val value = readInt()
                map[key] = value
            }
        }
        return map
    }

    fun writeStringLongMap(map: Map<String, Long>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeString(key)
                writeLong(value)
            }
        }
    }

    fun readStringLongMap(): Map<String, Long> {
        val size = readInt()
        val map: MutableMap<String, Long> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readString()
                val value = readLong()
                map[key] = value
            }
        }
        return map
    }

    fun writeStringStringMap(map: Map<String, String>) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            writeInt(map.size)
            for ((key, value) in map) {
                writeString(key)
                writeString(value)
            }
        }
    }

    fun readStringStringMap(): Map<String, String> {
        val size = readInt()
        val map: MutableMap<String, String> = HashMap()
        if (size > 0) {
            for (index in 0 until size) {
                val key = readString()
                val value = readString()
                map[key] = value
            }
        }
        return map
    }

    fun writeStringPacketMap(map: Map<String, *>, protocolId: Short) {
        if (map.isEmpty()) {
            writeInt(0)
        } else {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            writeInt(map.size)
            for ((key, value) in map) {
                writeString(key)
                protocolRegistration.write(this, value)
            }
        }
    }

    fun <T> readStringPacketMap(clazz: Class<T>, protocolId: Short): Map<String, T> {
        val size = readInt()
        val map: MutableMap<String, T> = HashMap()
        if (size > 0) {
            val protocolRegistration = ProtocolManager.getProtocol(protocolId)
            for (index in 0 until size) {
                val key = readString()
                val value = protocolRegistration.read(this) as T
                map[key] = value
            }
        }
        return map
    }

    fun writePacket(packet: Any?, protocolId: Short) {
        val protocolRegistration = ProtocolManager.getProtocol(protocolId)
        protocolRegistration.write(this, packet)
    }

    fun readPacket(protocolId: Short): Any {
        val protocolRegistration = ProtocolManager.getProtocol(protocolId)
        return protocolRegistration.read(this)
    }

    companion object {
        private const val INIT_SIZE = 128
        private const val MAX_SIZE = 655537
    }
}