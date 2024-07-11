<?php

namespace zfoophp;

include_once 'IProtocolRegistration.php';
include_once 'packet/EmptyObject.php';
include_once 'packet/NormalObject.php';
include_once 'packet/ObjectA.php';
include_once 'packet/ObjectB.php';
include_once 'packet/SimpleObject.php';

class ProtocolManager
{
    private static array $protocols = array();
    private static array $protocolIdMap = array();

    public static function initProtocol(): void
    {
        self::$protocols[0] = new EmptyObjectRegistration();
        self::$protocolIdMap[EmptyObject::class] = 0;
        self::$protocols[101] = new NormalObjectRegistration();
        self::$protocolIdMap[NormalObject::class] = 101;
        self::$protocols[102] = new ObjectARegistration();
        self::$protocolIdMap[ObjectA::class] = 102;
        self::$protocols[103] = new ObjectBRegistration();
        self::$protocolIdMap[ObjectB::class] = 103;
        self::$protocols[104] = new SimpleObjectRegistration();
        self::$protocolIdMap[SimpleObject::class] = 104;;
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