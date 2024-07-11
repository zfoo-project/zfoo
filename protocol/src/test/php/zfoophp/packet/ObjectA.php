<?php

namespace zfoophp;


class ObjectA {
    var int $a = 0;
    var array $m = array();
    var mixed $objectB = null;
    var int $innerCompatibleValue = 0;
}

class ObjectARegistration implements IProtocolRegistration {
    public function protocolId(): int
    {
        return 102;
    }

    public function write(ByteBuffer $buffer, mixed $packet): void
    {
        if ($packet == null)
        {
            $buffer->writeInt(0);
            return;
        }
        $beforeWriteIndex = $buffer->getWriteOffset();
        $buffer->writeInt(201);
        $buffer->writeInt($packet->a);
        $buffer->writeIntStringMap($packet->m);
        $buffer->writePacket($packet->objectB, 103);
        $buffer->writeInt($packet->innerCompatibleValue);
        $buffer->adjustPadding(201, $beforeWriteIndex);
    }

    public function read(ByteBuffer $buffer): mixed
    {
        $length = $buffer->readInt();
        $packet = new ObjectA();
        if ($length == 0) {
            return $packet;
        }
        $beforeReadIndex = $buffer->getReadOffset();
        $result0 = $buffer->readInt();
        $packet->a = $result0;
        $map1 = $buffer->readIntStringMap();
        $packet->m = $map1;
        $result2 = $buffer->readPacket(103);
        $packet->objectB = $result2;
        if ($buffer->compatibleRead($beforeReadIndex, $length)) {
            $result3 = $buffer->readInt();
            $packet->innerCompatibleValue = $result3;
        }
        if ($length > 0) {
            $buffer->setReadOffset($beforeReadIndex + $length);
        }
        return $packet;
    }
}