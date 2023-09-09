package com.zfoo.protocol.field;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.field.packet.*;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * 测试各种不同特征的数据类型的序列化和反序列化时间
 * 细致比较性能
 */
@Ignore
public class FieldSpeedTesting {
    public static int benchmark = 100000;
    public static IntObject intObject = new IntObject();
    public static IntegerObject integerObject = new IntegerObject();
    public static FloatObject floatObject = new FloatObject();
    public static BytesObject bytesObject = new BytesObject();
    public static StringObject stringObject = new StringObject();
    public static ListIntegerObject listIntegerObject = new ListIntegerObject();
    public static SetObject setObject = new SetObject();
    public static MapObject mapObject = new MapObject();
    public static InnerObjectObject innerObjectObject = new InnerObjectObject();
    public static FieldProtobufObject.IntObject protobufIntObject = null;
    public static FieldProtobufObject.FloatObject protobufFloatObject = null;
    public static FieldProtobufObject.BytesObject protobufBytesObject = null;
    public static FieldProtobufObject.StringObject protobufStringObject = null;
    public static FieldProtobufObject.ListIntegerObject protobufListIntegerObject = null;
    public static FieldProtobufObject.MapObject protobufMapObject = null;
    public static FieldProtobufObject.InnerObjectObject protobufInnerObjectObject = null;
    public static long zfooSerializationTime;
    public static long zfooDeserializationTime;
    public static long kryoSerializationTime;
    public static long kryoDeserializationTime;
    public static long protobufSerializationTime;
    public static long protobufDeserializationTime;
    public static long zfooSerializationSize;
    public static long kryoSerializationSize;
    public static long protobufSerializationSize;

    @Test
    public void testIntObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark * 10;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, intObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, intObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, IntObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufIntObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.IntObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [int类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [int类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [int类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testIntegerObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark * 10;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, integerObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, integerObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, IntegerObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufIntObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.IntObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [Integer类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [Integer类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [int类型]     [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [Integer类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [Integer类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [int类型]     [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testFloatObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark * 10;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, floatObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, floatObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, FloatObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufFloatObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.FloatObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [float类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [float类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [float类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testBytesObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark * 5;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, bytesObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, bytesObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, BytesObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufBytesObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.BytesObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [byte数组类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [byte数组类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [byte数组类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [byte数组类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [byte数组类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [byte数组类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testStringObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark * 3;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, stringObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, stringObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, StringObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufStringObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.StringObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [string类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [string类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [string类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [string类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [string类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [string类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testListIntegerObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, listIntegerObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, listIntegerObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, ListIntegerObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufListIntegerObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.ListIntegerObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [list类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [list类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [list类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [list类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [list类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [list类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testSetObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, setObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, setObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, SetObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        System.out.println(StringUtils.format("序列化   [zfoo]     [set类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [set类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [set类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [set类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
    }

    @Test
    public void testMapObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark / 2;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, mapObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, mapObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, MapObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufMapObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.MapObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [map类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [map类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [map类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [map类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [map类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [map类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testInnerObjectObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark = benchmark * 2;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            ProtocolManager.write(buffer, innerObjectObject);
        }
        zfooSerializationTime = System.currentTimeMillis() - startTime;
        zfooSerializationSize = buffer.writerIndex();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime = System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            kryo.writeObject(output, innerObjectObject);
        }
        kryoSerializationTime = System.currentTimeMillis() - startTime;
        kryoSerializationSize = output.position();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = kryo.readObject(input, InnerObjectObject.class);
        }
        kryoDeserializationTime = System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff = new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            protobufInnerObjectObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime = System.currentTimeMillis() - startTime;
        var length = codedOutputStream.getTotalBytesWritten();
        protobufSerializationSize = length;
        var singleLength = length / benchmark;
        var l = 0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs = new CodedInputStream[benchmark];
        for (int i = 0; i < benchmark; i++) {
            codeInputs[i] = CodedInputStream.newInstance(protobufBuff, l + singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            var mess = FieldProtobufObject.InnerObjectObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime = System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [内嵌Object类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), zfooSerializationTime, zfooSerializationSize));
        System.out.println(StringUtils.format("序列化   [kryo]     [内嵌Object类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), kryoSerializationTime, kryoSerializationSize));
        System.out.println(StringUtils.format("序列化   [protobuf] [内嵌Object类型] [thread:{}] [time:{}] [size:{}]", Thread.currentThread().getName(), protobufSerializationTime, protobufSerializationSize));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [内嵌Object类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [内嵌Object类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [内嵌Object类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    public static final int intAValue = -100000000;
    public static final int intBValue = -1000000;
    public static final int intCValue = -10000;
    public static final int intDValue = -100;
    public static final int intEValue = 0;
    public static final int intFValue = 100;
    public static final int intGValue = 10000;
    public static final int intHValue = 1000000;
    public static final int intIValue = 100000000;
    public static final int intJValue = 2147483647;
    public static final int intXValue = 1;
    public static final float floatAValue = (float) -10000000.123;
    public static final float floatBValue = (float) -100000.123;
    public static final float floatCValue = (float) -1000.123;
    public static final float floatDValue = (float) -10.123;
    public static final float floatEValue = (float) -0.12345678;
    public static final float floatFValue = (float) 0.12345678;
    public static final float floatGValue = (float) 10.123;
    public static final float floatHValue = (float) 1000.123;
    public static final float floatIValue = (float) 100000.123;
    public static final float floatJValue = (float) 10000000.123;
    public static final byte[] bytesValue = new byte[]{-128, -90, -60, -30, 0, 30, 60, 90, 120, 127};
    public static final String stringAValue = "a";
    public static final String stringBValue = "ab";
    public static final String stringCValue = "abc";
    public static final String stringDValue = "abcd";
    public static final String stringEValue = "abcde";
    public static final String stringFValue = "abcdef";
    public static final String stringGValue = "abcdefg";
    public static final String stringHValue = "abcdefgh";
    public static final String stringIValue = "abcdefghi";
    public static final String stringJValue = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
    public static final List<Integer> listValue = new ArrayList<>(ArrayUtils.toList(new int[]{Integer.MIN_VALUE, -10000000, -100000, -1000, -10, 0, 10, 1000, 100000, 10000000, Integer.MAX_VALUE}));
    public static final Set<Integer> setValue = new HashSet<>(listValue);
    public static final Map<Integer, String> mapValue = new HashMap<>(Map.of(Integer.MIN_VALUE, "a", -10000000, "ab", -100000, "abc", -1000, "abcd", -10, "abcde",
            0, "abcdef", 10, "abcdefg", 1000, "abcdefgh", 100000, "abcdefghi", Integer.MAX_VALUE, "abcdefghij"));
    public static final InnerObject innerObjectValue = new InnerObject();

    @BeforeClass
    public static void prepare() {
        intObject.setA(intAValue);
        intObject.setB(intBValue);
        intObject.setC(intCValue);
        intObject.setD(intDValue);
        intObject.setE(intEValue);
        intObject.setF(intFValue);
        intObject.setG(intGValue);
        intObject.setH(intHValue);
        intObject.setI(intIValue);
        intObject.setJ(intJValue);
        protobufIntObject = FieldProtobufObject.IntObject.newBuilder().setA(intAValue).setB(intBValue).setC(intCValue).setD(intDValue)
                .setE(intEValue).setF(intFValue).setG(intGValue).setH(intHValue).setI(intIValue).setJ(intJValue).build();

        integerObject.setA(intAValue);
        integerObject.setB(intBValue);
        integerObject.setC(intCValue);
        integerObject.setD(intDValue);
        integerObject.setE(intEValue);
        integerObject.setF(intFValue);
        integerObject.setG(intGValue);
        integerObject.setH(intHValue);
        integerObject.setI(intIValue);
        integerObject.setJ(intJValue);

        floatObject.setA(floatAValue);
        floatObject.setB(floatBValue);
        floatObject.setC(floatCValue);
        floatObject.setD(floatDValue);
        floatObject.setE(floatEValue);
        floatObject.setF(floatFValue);
        floatObject.setG(floatGValue);
        floatObject.setH(floatHValue);
        floatObject.setI(floatIValue);
        floatObject.setJ(floatJValue);
        protobufFloatObject = FieldProtobufObject.FloatObject.newBuilder().setA(floatAValue).setB(floatBValue).setC(floatCValue)
                .setD(floatDValue).setE(floatEValue).setF(floatFValue).setG(floatGValue).setH(floatHValue).setI(floatIValue).setJ(floatJValue).build();

        bytesObject.setA(bytesValue);
        bytesObject.setB(bytesValue);
        bytesObject.setC(bytesValue);
        bytesObject.setD(bytesValue);
        bytesObject.setE(bytesValue);
        bytesObject.setF(bytesValue);
        bytesObject.setG(bytesValue);
        bytesObject.setH(bytesValue);
        bytesObject.setI(bytesValue);
        bytesObject.setJ(bytesValue);
        protobufBytesObject = FieldProtobufObject.BytesObject.newBuilder().setA(ByteString.copyFrom(bytesValue)).setB(ByteString.copyFrom(bytesValue)).setC(ByteString.copyFrom(bytesValue))
                .setD(ByteString.copyFrom(bytesValue)).setE(ByteString.copyFrom(bytesValue)).setF(ByteString.copyFrom(bytesValue)).setG(ByteString.copyFrom(bytesValue)).setH(ByteString.copyFrom(bytesValue)).setI(ByteString.copyFrom(bytesValue)).setJ(ByteString.copyFrom(bytesValue)).build();

        stringObject.setA(stringAValue);
        stringObject.setB(stringBValue);
        stringObject.setC(stringCValue);
        stringObject.setD(stringDValue);
        stringObject.setE(stringEValue);
        stringObject.setF(stringFValue);
        stringObject.setG(stringGValue);
        stringObject.setH(stringHValue);
        stringObject.setI(stringIValue);
        stringObject.setJ(stringJValue);
        protobufStringObject = FieldProtobufObject.StringObject.newBuilder().setA(stringAValue).setB(stringBValue).setC(stringCValue)
                .setD(stringDValue).setE(stringEValue).setF(stringFValue).setG(stringGValue).setH(stringHValue).setI(stringIValue).setJ(stringJValue).build();

        listIntegerObject.setA(listValue);
        listIntegerObject.setB(listValue);
        listIntegerObject.setC(listValue);
        listIntegerObject.setD(listValue);
        listIntegerObject.setE(listValue);
        listIntegerObject.setF(listValue);
        listIntegerObject.setG(listValue);
        listIntegerObject.setH(listValue);
        listIntegerObject.setI(listValue);
        listIntegerObject.setJ(listValue);
        protobufListIntegerObject = FieldProtobufObject.ListIntegerObject.newBuilder().addAllA(listValue).addAllB(listValue).addAllC(listValue)
                .addAllD(listValue).addAllE(listValue).addAllF(listValue).addAllG(listValue).addAllH(listValue).addAllI(listValue).addAllJ(listValue).build();

        setObject.setA(setValue);
        setObject.setB(setValue);
        setObject.setC(setValue);
        setObject.setD(setValue);
        setObject.setE(setValue);
        setObject.setF(setValue);
        setObject.setG(setValue);
        setObject.setH(setValue);
        setObject.setI(setValue);
        setObject.setJ(setValue);

        mapObject.setA(mapValue);
        mapObject.setB(mapValue);
        mapObject.setC(mapValue);
        mapObject.setD(mapValue);
        mapObject.setE(mapValue);
        mapObject.setF(mapValue);
        mapObject.setG(mapValue);
        mapObject.setH(mapValue);
        mapObject.setI(mapValue);
        mapObject.setJ(mapValue);
        protobufMapObject = FieldProtobufObject.MapObject.newBuilder().putAllA(mapValue).putAllB(mapValue).putAllC(mapValue)
                .putAllD(mapValue).putAllE(mapValue).putAllF(mapValue).putAllG(mapValue).putAllH(mapValue).putAllI(mapValue).putAllJ(mapValue).build();

        innerObjectValue.setX(10000);
        innerObjectObject.setA(innerObjectValue);
        innerObjectObject.setB(innerObjectValue);
        innerObjectObject.setC(innerObjectValue);
        innerObjectObject.setD(innerObjectValue);
        innerObjectObject.setE(innerObjectValue);
        innerObjectObject.setF(innerObjectValue);
        innerObjectObject.setG(innerObjectValue);
        innerObjectObject.setH(innerObjectValue);
        innerObjectObject.setI(innerObjectValue);
        innerObjectObject.setJ(innerObjectValue);

        var protobufInnerObject = FieldProtobufObject.InnerObject.newBuilder().setX(intXValue).build();
        protobufInnerObjectObject = FieldProtobufObject.InnerObjectObject.newBuilder().setA(protobufInnerObject).setB(protobufInnerObject).setC(protobufInnerObject)
                .setD(protobufInnerObject).setE(protobufInnerObject).setF(protobufInnerObject).setG(protobufInnerObject).setH(protobufInnerObject).setI(protobufInnerObject).setJ(protobufInnerObject).build();
        // zfoo -----------------------------------------------------------------------
        System.setProperty("io.netty.buffer.checkAccessible", "false");
        System.setProperty("io.netty.buffer.checkBounds", "false");
        var op = GenerateOperation.NO_OPERATION;
        ProtocolManager.initProtocolAuto(Set.of(IntObject.class, IntegerObject.class, FloatObject.class, BytesObject.class, StringObject.class, ListIntegerObject.class, SetObject.class, MapObject.class, InnerObjectObject.class), op);
    }

    public static final ThreadLocal<Kryo> kryos = new ThreadLocal<>() {
        @Override
        protected Kryo initialValue() {
            var kryo = new Kryo();
            kryo.register(IntObject.class);
            kryo.register(IntegerObject.class);
            kryo.register(FloatObject.class);
            kryo.register(BytesObject.class);
            kryo.register(StringObject.class);
            kryo.register(ListIntegerObject.class);
            kryo.register(SetObject.class);
            kryo.register(MapObject.class);
            kryo.register(InnerObject.class);
            kryo.register(InnerObjectObject.class);
            kryo.register(byte[].class);
            kryo.register(Byte[].class);
            kryo.register(short[].class);
            kryo.register(Short[].class);
            kryo.register(int[].class);
            kryo.register(Integer[].class);
            kryo.register(long[].class);
            kryo.register(Long[].class);
            kryo.register(float[].class);
            kryo.register(Float[].class);
            kryo.register(double[].class);
            kryo.register(Double[].class);
            kryo.register(boolean[].class);
            kryo.register(Boolean[].class);
            kryo.register(char[].class);
            kryo.register(Character[].class);
            kryo.register(String[].class);
            kryo.register(ArrayList.class);
            kryo.register(HashSet.class);
            kryo.register(HashMap.class);
            // 关闭循环引用，提高性能
            kryo.setReferences(false);
            return kryo;
        }
    };
}
