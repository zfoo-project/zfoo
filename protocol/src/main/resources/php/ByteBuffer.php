<?php

namespace zfoophp;

use Exception;


class ByteBuffer
{
    private string $buffer;
    private int $writeOffset = 0;
    private int $readOffset = 0;
    private bool $bigEndian = true;

    public function __construct()
    {
        $this->buffer = str_pad("", 128);
        // 大端序还是小端序
        $this->bigEndian = pack("L", 1) === "\x00\x00\x00\x01";
    }

    public function adjustPadding(int $predictionLength, int $beforewriteIndex): void
    {
        // 因为写入的是可变长的int，如果预留的位置过多，则清除多余的位置
        $currentwriteIndex = $this->writeOffset;
        $predictionCount = $this->writeIntCount($predictionLength);
        $length = $currentwriteIndex - $beforewriteIndex - $predictionCount;
        $lengthCount = $this->writeIntCount($length);
        $padding = $lengthCount - $predictionCount;
        if ($padding == 0) {
            $this->writeOffset = $beforewriteIndex;
            $this->writeInt($length);
            $this->writeOffset = $currentwriteIndex;
        } else {
            $bytes = substr($this->buffer, $currentwriteIndex - $length, $length);
            $this->writeOffset = $beforewriteIndex;
            $this->writeInt($length);
            $this->writeBytes($bytes);
        }
    }

    public function compatibleRead(int $beforereadIndex, int $length): bool
    {
        return $length != -1 && $this->readOffset < $length + $beforereadIndex;
    }

    // -------------------------------------------------get/set-------------------------------------------------
    public function getBuffer(): string
    {
        return $this->buffer;
    }

    public function getWriteOffset(): int
    {
        return $this->writeOffset;
    }

    /**
     * @throws Exception
     */
    public function setWriteOffset(int $writeIndex): void
    {
        if ($writeIndex > strlen($this->buffer)) {
            throw new Exception("writeIndex[" . $writeIndex . "] out of bounds exception: readerIndex: " . $this->readOffset .
                ", writerIndex: " . $this->writeOffset . "(expected: 0 <= readerIndex <= writerIndex <= capacity:" . strlen($this->buffer));
        }
        $this->writeOffset = $writeIndex;
    }

    public function getReadOffset(): int
    {
        return $this->readOffset;
    }

    /**
     * @throws Exception
     */
    public function setReadOffset(int $readIndex): void
    {
        if ($readIndex > $this->writeOffset) {
            throw new Exception("readIndex[" . $readIndex . "] out of bounds exception: readerIndex: " . $this->readOffset .
                ", writerIndex: " . $this->writeOffset . "(expected: 0 <= readerIndex <= writerIndex <= capacity:" . strlen($this->buffer));
        }
        $this->readOffset = $readIndex;
    }

    public function toBytes(): string
    {
        return substr($this->buffer, 0, $this->writeOffset);
    }

    public function isReadable(): bool
    {
        return $this->writeOffset > $this->readOffset;
    }

    public function getCapacity(): int
    {
        return strlen($this->buffer) - $this->writeOffset;
    }

    // -------------------------------------------------write/read-------------------------------------------------
    public function writeBytes(string $bytes): void
    {
        // 如果容量不够则扩容一倍
        $length = strlen($bytes);
        while ($length > $this->getCapacity()) {
            $this->buffer = str_pad($this->buffer, strlen($this->buffer) * 2);
        }

        for ($i = 0; $i < $length; $i++) {
            $this->buffer[$this->writeOffset] = $bytes[$i];
            $this->writeOffset++;
        }
    }

    public function readBytes(int $count): string
    {
        $bytes = substr($this->buffer, $this->readOffset, $count);
        $this->readOffset += $count;
        return $bytes;
    }

    public function writeBytesBigEndian(string $bytes): void
    {
        if ($this->bigEndian) {
            $this->writeBytes($bytes);
        } else {
            $this->writeBytes(strrev($bytes));
        }
    }

    public function readBytesBigEndian(int $count): string
    {
        if ($this->bigEndian) {
            return $this->readBytes($count);
        } else {
            return strrev($this->readBytes($count));
        }
    }

    public function writeByte(int $value): void
    {
        $this->writeBytes(pack('c', $value));
    }

    public function readByte(): int
    {
        return unpack('c', $this->readBytes(1))[1];
    }

    public function writeUByte(int $value): void
    {
        $this->writeBytes(pack('C', $value));
    }

    public function readUByte(): int
    {
        return unpack('c', $this->readBytes(1))[1];
    }

    public function writeBool(bool $value): void
    {
        $this->writeBytes(pack('C', $value ? 1 : 0));
    }

    public function readBool(): bool
    {
        return unpack('C', $this->readBytes(1))[1] == 1;
    }

    public function writeShort(int $value): void
    {
        $this->writeBytesBigEndian(pack('s', $value));
    }

    public function readShort(): int
    {
        return unpack('s', $this->readBytesBigEndian(2))[1];
    }

    public function writeRawInt(int $value): void
    {
        $this->writeBytes(pack('i', $value));
    }

    public function readRawInt(): int
    {
        return unpack('i', $this->readBytes(4))[1];
    }

    public function writeInt(int $value): void
    {
        $this->writeLong($value);
    }

    public function readInt(): int
    {
        return $this->readLong();
    }

    public function writeIntCount(int $value): int
    {
        $value = ($value << 1) ^ ($value >> 31);
        if ($value >> 7 == 0) {
            return 1;
        }
        if ($value >> 14 == 0) {
            return 2;
        }
        if ($value >> 21 == 0) {
            return 3;
        }
        if ($value >> 28 == 0) {
            return 4;
        }
        return 5;
    }

    public function writeLong(int $longValue): void
    {
        $value = ($longValue << 1) ^ ($longValue >> 63);
        if ($value < 0) {
            $this->writeByte($value & 0xFF | 0x80);
            $this->writeByte($value >> 7 & 0xFF | 0x80);
            $this->writeByte($value >> 14 & 0xFF | 0x80);
            $this->writeByte($value >> 21 & 0xFF | 0x80);
            $this->writeByte($value >> 28 & 0xFF | 0x80);
            $this->writeByte($value >> 35 & 0xFF | 0x80);
            $this->writeByte($value >> 42 & 0xFF | 0x80);
            $this->writeByte($value >> 49 & 0xFF | 0x80);
            $this->writeByte($value >> 56 & 0xFF);
            return;
        }
        if ($value >> 7 == 0) {
            $this->writeByte($value);
            return;
        }

        if ($value >> 14 == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7);
            return;
        }

        if ($value >> 21 == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7 | 0x80);
            $this->writeByte($value >> 14);
            return;
        }

        if ($value >> 28 == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7 | 0x80);
            $this->writeByte($value >> 14 | 0x80);
            $this->writeByte($value >> 21);
            return;
        }

        if ($value >> 35 == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7 | 0x80);
            $this->writeByte($value >> 14 | 0x80);
            $this->writeByte($value >> 21 | 0x80);
            $this->writeByte($value >> 28);
            return;
        }

        if ($value >> 42 == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7 | 0x80);
            $this->writeByte($value >> 14 | 0x80);
            $this->writeByte($value >> 21 | 0x80);
            $this->writeByte($value >> 28 | 0x80);
            $this->writeByte($value >> 35);
            return;
        }

        if ($value >> 49 == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7 | 0x80);
            $this->writeByte($value >> 14 | 0x80);
            $this->writeByte($value >> 21 | 0x80);
            $this->writeByte($value >> 28 | 0x80);
            $this->writeByte($value >> 35 | 0x80);
            $this->writeByte($value >> 42);
            return;
        }

        if (($value >> 56) == 0) {
            $this->writeByte($value | 0x80);
            $this->writeByte($value >> 7 | 0x80);
            $this->writeByte($value >> 14 | 0x80);
            $this->writeByte($value >> 21 | 0x80);
            $this->writeByte($value >> 28 | 0x80);
            $this->writeByte($value >> 35 | 0x80);
            $this->writeByte($value >> 42 | 0x80);
            $this->writeByte($value >> 49);
            return;
        }

        $this->writeByte($value | 0x80);
        $this->writeByte($value >> 7 | 0x80);
        $this->writeByte($value >> 14 | 0x80);
        $this->writeByte($value >> 21 | 0x80);
        $this->writeByte($value >> 28 | 0x80);
        $this->writeByte($value >> 35 | 0x80);
        $this->writeByte($value >> 42 | 0x80);
        $this->writeByte($value >> 49 | 0x80);
        $this->writeByte($value >> 56);
    }

    public function readLong(): int
    {
        $b = $this->readByte();
        $value = $b;
        if ($b < 0) {
            $b = $this->readByte();
            $value = $value & 0x00000000_0000007F | $b << 7;
            if ($b < 0) {
                $b = $this->readByte();
                $value = $value & 0x00000000_00003FFF | $b << 14;
                if ($b < 0) {
                    $b = $this->readByte();
                    $value = $value & 0x00000000_001FFFFF | $b << 21;
                    if ($b < 0) {
                        $b = $this->readByte();
                        $value = $value & 0x00000000_0FFFFFFF | $b << 28;
                        if ($b < 0) {
                            $b = $this->readByte();
                            $value = $value & 0x00000007_FFFFFFFF | $b << 35;
                            if ($b < 0) {
                                $b = $this->readByte();
                                $value = $value & 0x000003FF_FFFFFFFF | $b << 42;
                                if ($b < 0) {
                                    $b = $this->readByte();
                                    $value = $value & 0x0001FFFF_FFFFFFFF | $b << 49;
                                    if ($b < 0) {
                                        $b = $this->readByte();
                                        $value = $value & 0x00FFFFFF_FFFFFFFF | $b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (($value >> 1 & 0x7FFFFFFF_FFFFFFFF) ^ -($value & 1));
    }

    public function writeFloat(float $value): void
    {
        $this->writeBytes(pack('f', $value));
    }

    public function readFloat(): float
    {
        return unpack('f', $this->readBytes(4))[1];
    }

    public function writeDouble(float $value): void
    {
        $this->writeBytes(pack('d', $value));
    }

    public function readDouble(): float
    {
        return unpack('d', $this->readBytes(8))[1];
    }

    public function writeString(string $value): void
    {
        $this->writeInt(strlen($value));
        $this->writeBytes($value);
    }

    public function readString(): string
    {
        $length = $this->readInt();
        return $this->readBytes($length);
    }

    public function writePacket(mixed $value, int $protocolId): void
    {
        $protocolRegistration = ProtocolManager::getProtocol($protocolId);
        $protocolRegistration->write($this, $value);
    }

    public function readPacket(int $protocolId): mixed
    {
        $protocolRegistration = ProtocolManager::getProtocol($protocolId);
        return $protocolRegistration->read($this);
    }

    public function writeBooleanArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeBool($array[$index]);
            }
        }
    }

    public function readBooleanArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readBool());
            }
        }
        return $array;
    }

    public function writeByteArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeByte($array[$index]);
            }
        }
    }

    public function readByteArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readByte());
            }
        }
        return $array;
    }

    public function writeShortArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeShort($array[$index]);
            }
        }
    }

    public function readShortArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readShort());
            }
        }
        return $array;
    }

    public function writeIntArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeInt($array[$index]);
            }
        }
    }

    public function readIntArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readInt());
            }
        }
        return $array;
    }


    public function writeLongArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeLong($array[$index]);
            }
        }
    }

    public function readLongArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readLong());
            }
        }
        return $array;
    }


    public function writeFloatArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeFloat($array[$index]);
            }
        }
    }

    public function readFloatArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readFloat());
            }
        }
        return $array;
    }


    public function writeDoubleArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeDouble($array[$index]);
            }
        }
    }

    public function readDoubleArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readDouble());
            }
        }
        return $array;
    }


    public function writeStringArray(array $array): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writeString($array[$index]);
            }
        }
    }

    public function readStringArray(): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readString());
            }
        }
        return $array;
    }

    public function writePacketArray(array $array, int $protocolId): void
    {
        if (empty($array)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($array));
            $length = count($array);
            for ($index = 0; $index < $length; $index++) {
                $this->writePacket($array[$index], $protocolId);
            }
        }
    }

    public function readPacketArray(int $protocolId): array
    {
        $size = $this->readInt();
        $array = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                array_push($array, $this->readPacket($protocolId));
            }
        }
        return $array;
    }

    public function writeIntIntMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeInt($key);
                $this->writeInt($value);
            }
        }
    }

    public function readIntIntMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readInt();
                $value = $this->readInt();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeIntLongMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeInt($key);
                $this->writeLong($value);
            }
        }
    }

    public function readIntLongMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readInt();
                $value = $this->readLong();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeIntStringMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeInt($key);
                $this->writeString($value);
            }
        }
    }

    public function readIntStringMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readInt();
                $value = $this->readString();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeIntPacketMap(array $map, int $protocolId): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeInt($key);
                $this->writePacket($value, $protocolId);
            }
        }
    }

    public function readIntPacketMap(int $protocolId): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readInt();
                $value = $this->readPacket($protocolId);
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeLongIntMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeLong($key);
                $this->writeInt($value);
            }
        }
    }

    public function readLongIntMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readLong();
                $value = $this->readInt();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeLongLongMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeLong($key);
                $this->writeLong($value);
            }
        }
    }

    public function readLongLongMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readLong();
                $value = $this->readLong();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeLongStringMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeLong($key);
                $this->writeString($value);
            }
        }
    }

    public function readLongStringMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readLong();
                $value = $this->readString();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeLongPacketMap(array $map, int $protocolId): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeLong($key);
                $this->writePacket($value, $protocolId);
            }
        }
    }

    public function readLongPacketMap(int $protocolId): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readLong();
                $value = $this->readPacket($protocolId);
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeStringIntMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeString($key);
                $this->writeInt($value);
            }
        }
    }

    public function readStringIntMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readString();
                $value = $this->readInt();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeStringLongMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeString($key);
                $this->writeLong($value);
            }
        }
    }

    public function readStringLongMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readString();
                $value = $this->readLong();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeStringStringMap(array $map): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeString($key);
                $this->writeString($value);
            }
        }
    }

    public function readStringStringMap(): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readString();
                $value = $this->readString();
                $map[$key] = $value;
            }
        }
        return $map;
    }

    public function writeStringPacketMap(array $map, int $protocolId): void
    {
        if (empty($map)) {
            $this->writeInt(0);
        } else {
            $this->writeInt(count($map));
            foreach ($map as $key => $value) {
                $this->writeString($key);
                $this->writePacket($value, $protocolId);
            }
        }
    }

    public function readStringPacketMap(int $protocolId): array
    {
        $size = $this->readInt();
        $map = array();
        if ($size > 0) {
            for ($index = 0; $index < $size; $index++) {
                $key = $this->readString();
                $value = $this->readPacket($protocolId);
                $map[$key] = $value;
            }
        }
        return $map;
    }
}