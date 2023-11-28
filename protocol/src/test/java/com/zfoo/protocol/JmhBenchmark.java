package com.zfoo.protocol;

import com.alibaba.fastjson2.JSONB;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.packet.ComplexObject;
import com.zfoo.protocol.packet.NormalObject;
import com.zfoo.protocol.packet.ProtobufObject;
import com.zfoo.protocol.packet.SimpleObject;
import io.fury.ThreadSafeFury;
import io.fury.memory.MemoryBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;


/**
 * @author godotg
 */
@Ignore
public class JmhBenchmark {

    public static final BenchmarkTesting benchmark = new BenchmarkTesting();
    public static final SimpleObject simpleObject = BenchmarkTesting.simpleObject;
    public static final NormalObject normalObject = BenchmarkTesting.normalObject;
    public static final ComplexObject complexObject = BenchmarkTesting.complexObject;
    // zfoo
    public static final ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);
    // fury
    public static final ThreadSafeFury fury = BenchmarkTesting.fury;
    public static final MemoryBuffer bufferFury = MemoryBuffer.newHeapBuffer(1_0000);
    // kryo
    public static final Output output = new Output(1024 * 8);
    public static final Input input = new Input(output.getBuffer());
    public static final ThreadLocal<Kryo> kryos = BenchmarkTesting.kryos;
    // protobuf
    public static final byte[] bufferProtobuf = new byte[1024 * 8];
    public static ProtobufObject.ProtobufComplexObject protobufComplexObject = BenchmarkTesting.protobufComplexObject;
    public static ProtobufObject.ProtobufNormalObject protobufNormalObject = BenchmarkTesting.protobufNormalObject;
    public static ProtobufObject.ProtobufSimpleObject protobufSimpleObject = BenchmarkTesting.protobufSimpleObject;

    @Benchmark
    public void zfooTest() {
        buffer.clear();
        ProtocolManager.write(buffer, simpleObject);
        var simple = ProtocolManager.read(buffer);
        buffer.clear();
        ProtocolManager.write(buffer, normalObject);
        var normal = ProtocolManager.read(buffer);
        buffer.clear();
        ProtocolManager.write(buffer, complexObject);
        var complex = ProtocolManager.read(buffer);
    }

    @Benchmark
    public void furyTest() {
        bufferFury.writerIndex(0);
        bufferFury.readerIndex(0);
        fury.serialize(bufferFury, simpleObject);
        var simple = fury.deserialize(bufferFury);
        bufferFury.writerIndex(0);
        bufferFury.readerIndex(0);
        fury.serialize(bufferFury, normalObject);
        var normal = fury.deserialize(bufferFury);
        bufferFury.writerIndex(0);
        bufferFury.readerIndex(0);
        fury.serialize(bufferFury, complexObject);
        var complex = fury.deserialize(bufferFury);
    }

    @Benchmark
    public void jsonbTest() {
        var bytes = JSONB.toBytes(simpleObject);
        var simple = JSONB.parseObject(bytes, SimpleObject.class);
        bytes = JSONB.toBytes(normalObject);
        var normal = JSONB.parseObject(bytes, NormalObject.class);
        bytes = JSONB.toBytes(complexObject);
        var complex = JSONB.parseObject(bytes, ComplexObject.class);
    }

    @Benchmark
    public void kryoTest() {
        var kryo = kryos.get();
        input.reset();
        output.reset();
        kryo.writeObject(output, simpleObject);
        var simple = kryo.readObject(input, SimpleObject.class);
        input.reset();
        output.reset();
        kryo.writeObject(output, normalObject);
        var normal = kryo.readObject(input, NormalObject.class);
        input.reset();
        output.reset();
        kryo.writeObject(output, complexObject);
        var complex = kryo.readObject(input, ComplexObject.class);
    }

    @Benchmark
    public void protobufTest() throws IOException {
        var codedOutputStream = CodedOutputStream.newInstance(bufferProtobuf);
        protobufSimpleObject.writeTo(codedOutputStream);
        var length = codedOutputStream.getTotalBytesWritten();
        var codeInput = CodedInputStream.newInstance(bufferProtobuf, 0, length);
        var simple = ProtobufObject.ProtobufSimpleObject.parseFrom(codeInput);

        codedOutputStream = CodedOutputStream.newInstance(bufferProtobuf);
        protobufNormalObject.writeTo(codedOutputStream);
        length = codedOutputStream.getTotalBytesWritten();
        codeInput = CodedInputStream.newInstance(bufferProtobuf, 0, length);
        var normal = ProtobufObject.ProtobufNormalObject.parseFrom(codeInput);

        codedOutputStream = CodedOutputStream.newInstance(bufferProtobuf);
        protobufComplexObject.writeTo(codedOutputStream);
        length = codedOutputStream.getTotalBytesWritten();
        codeInput = CodedInputStream.newInstance(bufferProtobuf, 0, length);
        var complex = ProtobufObject.ProtobufComplexObject.parseFrom(codeInput);
    }

    @Test
    public void test() throws RunnerException {
        // upload jmh-result.json to the following website in order to generate chart
        // https://deepoove.com/jmh-visual-chart/
        Options opt = new OptionsBuilder()
                .include(JmhBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}