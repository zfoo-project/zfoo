<?php

namespace zfoophp;


class EmptyObject {
    
}

class EmptyObjectRegistration implements IProtocolRegistration {
    public function protocolId(): int
    {
        return 0;
    }

    public function write(ByteBuffer $buffer, mixed $packet): void
    {
        if ($packet == null)
        {
            $buffer->writeInt(0);
            return;
        }
        $buffer->writeInt(-1);
    }

    public function read(ByteBuffer $buffer): mixed
    {
        $length = $buffer->readInt();
        $packet = new EmptyObject();
        if ($length == 0) {
            return $packet;
        }
        $beforeReadIndex = $buffer->getReadOffset();
        
        if ($length > 0) {
            $buffer->setReadOffset($beforeReadIndex + $length);
        }
        return $packet;
    }
}