<?php


use zfoophp\ByteBuffer;
use zfoophp\ProtocolManager;

include './zfoophp/ByteBuffer.php';
include './zfoophp/ProtocolManager.php';


function assertEqual($a, $b)
{
    if ($a === $b) {
        return;
    }
    throw new Exception("a is not equals b");
}

function byteTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeByte($value);
    $readValue = $byteBuffer->readByte();
    assertEqual($value, $readValue);
}

function boolTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeBool($value);
    $readValue = $byteBuffer->readBool();
    assertEqual($value, $readValue);
}

function shortTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeShort($value);
    $readValue = $byteBuffer->readShort();
    assertEqual($value, $readValue);
}

function rawIntTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeRawInt($value);
    $readValue = $byteBuffer->readRawInt();
    assertEqual($value, $readValue);
}

function intTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeInt($value);
    $readValue = $byteBuffer->readInt();
    assertEqual($value, $readValue);
}

function floatTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeFloat($value);
    $readValue = $byteBuffer->readFloat();
    assertEqual($value, $readValue);
}

function doubleTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeDouble($value);
    $readValue = $byteBuffer->readDouble();
    assertEqual($value, $readValue);
}

function stringTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeString($value);
    $readValue = $byteBuffer->readString();
    assertEqual($value, $readValue);
}

function boolArrayTest($value)
{
    $byteBuffer = new ByteBuffer();
    $byteBuffer->writeBoolArray($value);
    $readValue = $byteBuffer->readBoolArray();
    assertEqual($value, $readValue);
}


byteTest(22);
byteTest(-22);
boolTest(true);
shortTest(32767);
shortTest(-32768);
rawIntTest(2147483647);
rawIntTest(-2147483648);
intTest(2147483647);
intTest(-2147483648);
intTest(9223372036854775807);
intTest(PHP_INT_MIN);
floatTest(1.0);
floatTest(99.0);
floatTest(-99.0);
doubleTest(PHP_FLOAT_MAX);
doubleTest(PHP_FLOAT_MIN);
stringTest("hello world!");
stringTest("你好啊");
boolArrayTest([true, false, true]);


ProtocolManager::initProtocol();

//$file = 'D:\Project\zfoo\protocol\src\test\resources\compatible\normal-inner-compatible.bytes';
//$file = 'D:\Project\zfoo\protocol\src\test\resources\compatible\normal-no-compatible.bytes';
//$file = 'D:\Project\zfoo\protocol\src\test\resources\compatible\normal-out-compatible.bytes';
//$file = 'D:\Project\zfoo\protocol\src\test\resources\compatible\normal-out-inner-compatible.bytes';
$file = 'D:\Project\zfoo\protocol\src\test\resources\compatible\normal-out-inner-inner-compatible.bytes';
$data = file_get_contents($file);
//echo "file length: " . strlen($data) . "\n";
$buffer = new ByteBuffer();
$buffer->writeBytes($data);
$packet = ProtocolManager::read($buffer);
echo json_encode($packet);
