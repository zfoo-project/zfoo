package com.zfoo.protocol.field;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.field.packet.FieldProtobufObject;
import com.zfoo.protocol.field.packet.FloatObject;
import com.zfoo.protocol.field.packet.IntObject;
import com.zfoo.protocol.field.packet.IntegerObject;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试各种不同特征的数据类型的序列化和反序列化时间
 * 细致比较性能
 */
@Ignore
public class FieldSpeedTest {
    public static int benchmark = 100000;
    public static IntObject intObject=new IntObject();
    public static IntegerObject integerObject=new IntegerObject();
    public static FloatObject floatObject=new FloatObject();
    public static FieldProtobufObject.IntObject protobufIntObject = null;
    public static FieldProtobufObject.FloatObject protobufFloatObject = null;
    public static long zfooSerializationTime;
    public static long zfooDeserializationTime;
    public static long kryoSerializationTime;
    public static long kryoDeserializationTime;
    public static long protobufSerializationTime;
    public static long protobufDeserializationTime;
    @Test
    public void testIntObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark=benchmark*10;
        long startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            ProtocolManager.write(buffer,intObject);
        }
        zfooSerializationTime=System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime=System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            kryo.writeObject(output, intObject);
        }
        kryoSerializationTime=System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var mess = kryo.readObject(input, IntObject.class);
        }
        kryoDeserializationTime=System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff=new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            protobufIntObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime=System.currentTimeMillis() - startTime;
        var length=codedOutputStream.getTotalBytesWritten();
        var singleLength = length/benchmark;
        var l=0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs=new CodedInputStream[benchmark];
        for(int i=0;i<benchmark;i++)
        {
            codeInputs[i]=CodedInputStream.newInstance(protobufBuff, l+singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var mess = FieldProtobufObject.IntObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime=System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooSerializationTime));
        System.out.println(StringUtils.format("序列化   [kryo]     [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoSerializationTime));
        System.out.println(StringUtils.format("序列化   [protobuf] [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(),protobufSerializationTime));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(),kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [int类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testIntegerObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark=benchmark*10;
        long startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            ProtocolManager.write(buffer,integerObject);
        }
        zfooSerializationTime=System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime=System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            kryo.writeObject(output, integerObject);
        }
        kryoSerializationTime=System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var mess = kryo.readObject(input, IntegerObject.class);
        }
        kryoDeserializationTime=System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff=new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            protobufIntObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime=System.currentTimeMillis() - startTime;
        var length=codedOutputStream.getTotalBytesWritten();
        var singleLength = length/benchmark;
        var l=0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs=new CodedInputStream[benchmark];
        for(int i=0;i<benchmark;i++)
        {
            codeInputs[i]=CodedInputStream.newInstance(protobufBuff, l+singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var mess = FieldProtobufObject.IntObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime=System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [Integer类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooSerializationTime));
        System.out.println(StringUtils.format("序列化   [kryo]     [Integer类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoSerializationTime));
        System.out.println(StringUtils.format("序列化   [protobuf] [int类型]     [thread:{}] [time:{}]", Thread.currentThread().getName(),protobufSerializationTime));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [Integer类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [Integer类型] [thread:{}] [time:{}]", Thread.currentThread().getName(),kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [int类型]     [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }

    @Test
    public void testFloatObject() throws IOException {
        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 10000_0000, 100000000);
        benchmark=benchmark*10;
        long startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            ProtocolManager.write(buffer,floatObject);
        }
        zfooSerializationTime=System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var packet = ProtocolManager.read(buffer);
        }
        zfooDeserializationTime=System.currentTimeMillis() - startTime;
        buffer.release();
        var kryo = kryos.get();
        var output = new Output(10000_0000);
        var input = new Input(output.getBuffer());
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            kryo.writeObject(output, floatObject);
        }
        kryoSerializationTime=System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var mess = kryo.readObject(input, FloatObject.class);
        }
        kryoDeserializationTime=System.currentTimeMillis() - startTime;
        output.close();
        var protobufBuff=new byte[10000_0000];
        var codedOutputStream = CodedOutputStream.newInstance(protobufBuff);
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            protobufFloatObject.writeTo(codedOutputStream);
        }
        protobufSerializationTime=System.currentTimeMillis() - startTime;
        var length=codedOutputStream.getTotalBytesWritten();
        var singleLength = length/benchmark;
        var l=0;
        //在循环中去掉创建CodedInputStream的代码，保证比较耗时公平性
        var codeInputs=new CodedInputStream[benchmark];
        for(int i=0;i<benchmark;i++)
        {
            codeInputs[i]=CodedInputStream.newInstance(protobufBuff, l+singleLength, singleLength);
        }
        startTime = System.currentTimeMillis();
        for(int i=0;i<benchmark;i++)
        {
            var mess = FieldProtobufObject.FloatObject.parseFrom(codeInputs[i]);
        }
        protobufDeserializationTime=System.currentTimeMillis() - startTime;
        System.out.println(StringUtils.format("序列化   [zfoo]     [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooSerializationTime));
        System.out.println(StringUtils.format("序列化   [kryo]     [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), kryoSerializationTime));
        System.out.println(StringUtils.format("序列化   [protobuf] [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(),protobufSerializationTime));
        System.out.println(StringUtils.format("反序列化  [zfoo]     [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), zfooDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [kryo]     [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(),kryoDeserializationTime));
        System.out.println(StringUtils.format("反序列化  [protobuf] [float类型] [thread:{}] [time:{}]", Thread.currentThread().getName(), protobufDeserializationTime));
    }
    public static final int intAValue=-100000000;
    public static final int intBValue=-1000000;
    public static final int intCValue=-10000;
    public static final int intDValue=-100;
    public static final int intEValue=0;
    public static final int intFValue=100;
    public static final int intGValue=10000;
    public static final int intHValue=1000000;
    public static final int intIValue=100000000;
    public static final int intJValue=2147483647;
    public static final float floatAValue=(float)-10000000.123;
    public static final float floatBValue=(float)-100000.123;
    public static final float floatCValue=(float)-1000.123;
    public static final float floatDValue=(float)-10.123;
    public static final float floatEValue=(float)-0.12345678;
    public static final float floatFValue=(float)0.12345678;
    public static final float floatGValue=(float)10.123;
    public static final float floatHValue=(float)1000.123;
    public static final float floatIValue=(float)100000.123;
    public static final float floatJValue=(float)10000000.123;

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
        protobufIntObject= FieldProtobufObject.IntObject.newBuilder().setA(intAValue).setB(intBValue).setC(intCValue).setD(intDValue)
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
        protobufFloatObject=FieldProtobufObject.FloatObject.newBuilder().setA(floatAValue).setB(floatBValue).setC(floatCValue)
                .setD(floatDValue).setE(floatEValue).setF(floatFValue).setG(floatGValue).setH(floatHValue).setI(floatIValue).setJ(floatJValue).build();

        // zfoo -----------------------------------------------------------------------
        System.setProperty("io.netty.buffer.checkAccessible", "false");
        System.setProperty("io.netty.buffer.checkBounds", "false");
        var op = GenerateOperation.NO_OPERATION;
        ProtocolManager.initProtocolAuto(Set.of(IntObject.class,IntegerObject.class,FloatObject.class), op);
    }

    public static final ThreadLocal<Kryo> kryos = new ThreadLocal<>() {
        @Override
        protected Kryo initialValue() {
            var kryo = new Kryo();
            kryo.register(IntObject.class);
            kryo.register(IntegerObject.class);
            kryo.register(FloatObject.class);
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
