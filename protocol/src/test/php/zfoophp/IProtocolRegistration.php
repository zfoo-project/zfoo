<?php

namespace zfoophp;

interface IProtocolRegistration
{
    public function protocolId(): int;

    public function write(ByteBuffer $buffer, mixed $packet): void;

    public function read(ByteBuffer $buffer): mixed;

}