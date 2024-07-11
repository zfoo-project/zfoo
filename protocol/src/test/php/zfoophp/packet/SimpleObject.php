<?php

namespace zfoophp;


class SimpleObject {
    var int $c = 0;
    var bool $g = false;
}

class SimpleObjectRegistration implements IProtocolRegistration {
    public function protocolId(): int
    {
        return 104;
    }

    public function write(ByteBuffer $buffer, mixed $packet): void
    {
        if ($packet == null)
        {
            $buffer->writeInt(0);
            return;
        }
        $buffer->writeInt(-1);
        $buffer->writeInt($packet->c);
        $buffer->writeBool($packet->g);
    }

    public function read(ByteBuffer $buffer): mixed
    {
        $length = $buffer->readInt();
        $packet = new SimpleObject();
        if ($length == 0) {
            return $packet;
        }
        $beforeReadIndex = $buffer->getReadOffset();
        $result0 = $buffer->readInt();
        $packet->c = $result0;
        $result1 = $buffer->readBool(); 
        $packet->g = $result1;
        if ($length > 0) {
            $buffer->setReadOffset($beforeReadIndex + $length);
        }
        return $packet;
    }
}