<?php

namespace zfoophp;


class ObjectB {
    var bool $flag = false;
    var int $innerCompatibleValue = 0;
}

class ObjectBRegistration implements IProtocolRegistration {
    public function protocolId(): int
    {
        return 103;
    }

    public function write(ByteBuffer $buffer, mixed $packet): void
    {
        if ($packet == null)
        {
            $buffer->writeInt(0);
            return;
        }
        $beforeWriteIndex = $buffer->getWriteOffset();
        $buffer->writeInt(4);
        $buffer->writeBool($packet->flag);
        $buffer->writeInt($packet->innerCompatibleValue);
        $buffer->adjustPadding(4, $beforeWriteIndex);
    }

    public function read(ByteBuffer $buffer): mixed
    {
        $length = $buffer->readInt();
        $packet = new ObjectB();
        if ($length == 0) {
            return $packet;
        }
        $beforeReadIndex = $buffer->getReadOffset();
        $result0 = $buffer->readBool(); 
        $packet->flag = $result0;
        if ($buffer->compatibleRead($beforeReadIndex, $length)) {
            $result1 = $buffer->readInt();
            $packet->innerCompatibleValue = $result1;
        }
        if ($length > 0) {
            $buffer->setReadOffset($beforeReadIndex + $length);
        }
        return $packet;
    }
}