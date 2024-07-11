<?php

namespace zfoophp;

include_once 'IProtocolRegistration.php';
${protocol_imports}

class ProtocolManager
{
    private static array $protocols = array();
    private static array $protocolIdMap = array();

    public static function initProtocol(): void
    {
        ${protocol_manager_registrations};
    }

    public static function getProtocolId(mixed $clazz): int
    {
        return self::$protocolIdMap[$clazz];
    }

    public static function getProtocol(int $protocolId): IProtocolRegistration
    {
        return self::$protocols[$protocolId];
    }

    public static function write(ByteBuffer $buffer, mixed $packet): void
    {
        $protocolId = self::getProtocolId($packet::class);
        // write protocol id to buffer
        $buffer->writeShort($protocolId);
        // write packet
        self::getProtocol($protocolId)->write($buffer, $packet);

    }

    public static function read(ByteBuffer $buffer): mixed
    {
        $protocolId = $buffer->readShort();
        return self::getProtocol($protocolId)->read($buffer);
    }
}