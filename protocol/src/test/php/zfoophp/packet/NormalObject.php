<?php

namespace zfoophp;

// 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
class NormalObject {
    var int $a = 0;
    var array $aaa = array();
    var int $b = 0;
    // 整数类型
    var int $c = 0;
    var int $d = 0;
    var float $e = 0;
    var float $f = 0;
    var bool $g = false;
    var string $jj = "";
    var mixed $kk = null;
    var array $l = array();
    var array $ll = array();
    var array $lll = array();
    var array $llll = array();
    var array $m = array();
    var array $mm = array();
    var array $s = array();
    var array $ssss = array();
    var int $outCompatibleValue = 0;
    var int $outCompatibleValue2 = 0;
}

class NormalObjectRegistration implements IProtocolRegistration {
    public function protocolId(): int
    {
        return 101;
    }

    public function write(ByteBuffer $buffer, mixed $packet): void
    {
        if ($packet == null)
        {
            $buffer->writeInt(0);
            return;
        }
        $beforeWriteIndex = $buffer->getWriteOffset();
        $buffer->writeInt(857);
        $buffer->writeByte($packet->a);
        $buffer->writeByteArray($packet->aaa);
        $buffer->writeShort($packet->b);
        $buffer->writeInt($packet->c);
        $buffer->writeLong($packet->d);
        $buffer->writeFloat($packet->e);
        $buffer->writeDouble($packet->f);
        $buffer->writeBool($packet->g);
        $buffer->writeString($packet->jj);
        $buffer->writePacket($packet->kk, 102);
        $buffer->writeIntArray($packet->l);
        $buffer->writeLongArray($packet->ll);
        $buffer->writePacketArray($packet->lll, 102);
        $buffer->writeStringArray($packet->llll);
        $buffer->writeIntStringMap($packet->m);
        $buffer->writeIntPacketMap($packet->mm, 102);
        $buffer->writeIntArray($packet->s);
        $buffer->writeStringArray($packet->ssss);
        $buffer->writeInt($packet->outCompatibleValue);
        $buffer->writeInt($packet->outCompatibleValue2);
        $buffer->adjustPadding(857, $beforeWriteIndex);
    }

    public function read(ByteBuffer $buffer): mixed
    {
        $length = $buffer->readInt();
        $packet = new NormalObject();
        if ($length == 0) {
            return $packet;
        }
        $beforeReadIndex = $buffer->getReadOffset();
        $result0 = $buffer->readByte();
        $packet->a = $result0;
        $array1 = $buffer->readByteArray();
        $packet->aaa = $array1;
        $result2 = $buffer->readShort();
        $packet->b = $result2;
        $result3 = $buffer->readInt();
        $packet->c = $result3;
        $result4 = $buffer->readLong();
        $packet->d = $result4;
        $result5 = $buffer->readFloat();
        $packet->e = $result5;
        $result6 = $buffer->readDouble();
        $packet->f = $result6;
        $result7 = $buffer->readBool(); 
        $packet->g = $result7;
        $result8 = $buffer->readString();
        $packet->jj = $result8;
        $result9 = $buffer->readPacket(102);
        $packet->kk = $result9;
        $list10 = $buffer->readIntArray();
        $packet->l = $list10;
        $list11 = $buffer->readLongArray();
        $packet->ll = $list11;
        $list12 = $buffer->readPacketArray(102);
        $packet->lll = $list12;
        $list13 = $buffer->readStringArray();
        $packet->llll = $list13;
        $map14 = $buffer->readIntStringMap();
        $packet->m = $map14;
        $map15 = $buffer->readIntPacketMap(102);
        $packet->mm = $map15;
        $set16 = $buffer->readIntArray();
        $packet->s = $set16;
        $set17 = $buffer->readStringArray();
        $packet->ssss = $set17;
        if ($buffer->compatibleRead($beforeReadIndex, $length)) {
            $result18 = $buffer->readInt();
            $packet->outCompatibleValue = $result18;
        }
        if ($buffer->compatibleRead($beforeReadIndex, $length)) {
            $result19 = $buffer->readInt();
            $packet->outCompatibleValue2 = $result19;
        }
        if ($length > 0) {
            $buffer->setReadOffset($beforeReadIndex + $length);
        }
        return $packet;
    }
}