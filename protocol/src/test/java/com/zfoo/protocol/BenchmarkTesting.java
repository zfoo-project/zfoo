/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.packet.*;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author godotg
 */
@Ignore
public class BenchmarkTesting {

    public static int benchmark = 10_0000;

    /**
     * 单线程性能测试
     * <p>
     * 不使用任何JVM参数：zfoo比protobuf快20%，zfoo比kryo快40%
     * <p>
     * 包体大小：
     * 简单对象，zfoo包体大小8，kryo包体大小5，protobuf包体大小8
     * 常规对象，zfoo包体大小430，kryo包体大小483，protobuf包体大小793
     * 复杂对象，zfoo包体大小2216，kryo包体大小2528，protobuf包体大小5091
     */
    @Test
    public void singleThreadBenchmarks() {
        while (true) {
            if (benchmark > 160_0000) {
                return;
            }
            System.out.println(StringUtils.MULTIPLE_HYPHENS);
            System.out.println(StringUtils.format("[单线程性能测试-->[benchmark:{}]]", benchmark));

            zfooTest();
            protobufTest();
            kryoTest();

            // 递归执行，多跑几遍
            benchmark = benchmark * 2;
        }
    }

    /**
     * 多线程性能测试
     */
    @Test
    public void multipleThreadBenchmarks() throws InterruptedException {
        while (true) {
            if (benchmark > 160_0000) {
                return;
            }
            System.out.println(StringUtils.MULTIPLE_HYPHENS);
            System.out.println(StringUtils.format("[多线程性能测试-->[benchmark:{}]]", benchmark));

            zfooMultipleThreadTest();
            protobufMultipleThreadTest();
            kryoMultipleThreadTest();

            benchmark = benchmark * 2;
            multipleThreadBenchmarks();
        }
    }

    @Test
    public void zfooTest() {
        // netty的ByteBuf做了更多的安全检测，java自带的ByteBuffer并没有做安全检测，为了公平，把不需要的检测去掉
        // java通过ByteBuffer.allocate(1024 * 8)构造出来的是使用了unsafe的HeapByteBuffer，为了公平，使用netty中带有unsafe操作的UnpooledUnsafeHeapByteBuf
        System.setProperty("io.netty.buffer.checkAccessible", "false");
        System.setProperty("io.netty.buffer.checkBounds", "false");

        ByteBuf buffer = new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 100, 1_0000);

        // 序列化和反序列化简单对象
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            // 把对象序列化到buffer中
            ProtocolManager.write(buffer, simpleObject);

            // 从buffer中反序列化出对象
            var packet = ProtocolManager.read(buffer);
        }

        System.out.println(StringUtils.format("[zfoo]     [简单对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), buffer.writerIndex(), System.currentTimeMillis() - startTime));

        // 序列化和反序列化常规对象
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            ProtocolManager.write(buffer, normalObject);
            var packet = ProtocolManager.read(buffer);
        }

        System.out.println(StringUtils.format("[zfoo]     [常规对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), buffer.writerIndex(), System.currentTimeMillis() - startTime));

        // 序列化和反序列化复杂对象
        startTime = System.currentTimeMillis();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            ProtocolManager.write(buffer, complexObject);
            var packet = ProtocolManager.read(buffer);
        }

        System.out.println(StringUtils.format("[zfoo]     [复杂对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), buffer.writerIndex(), System.currentTimeMillis() - startTime));
    }

    @Test
    public void kryoTest() {
        try {
            var kryo = kryos.get();

            var output = new Output(1024 * 8);
            var input = new Input(output.getBuffer());

            // 序列化和反序列化简单对象
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                input.reset();
                output.reset();
                kryo.writeObject(output, simpleObject);
                var mess = kryo.readObject(input, SimpleObject.class);
            }

            System.out.println(StringUtils.format("[kryo]     [简单对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), output.position(), System.currentTimeMillis() - startTime));

            // 序列化和反序列化常规对象
            startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                input.reset();
                output.reset();
                kryo.writeObject(output, normalObject);
                var mess = kryo.readObject(input, NormalObject.class);
            }

            System.out.println(StringUtils.format("[kryo]     [常规对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), output.position(), System.currentTimeMillis() - startTime));

            // 序列化和反序列化复杂对象
            startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                input.reset();
                output.reset();
                kryo.writeObject(output, complexObject);
                var mess = kryo.readObject(input, ComplexObject.class);
            }
            System.out.println(StringUtils.format("[kryo]     [复杂对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), output.position(), System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("JDK17 运行kryo会报错，等kryo修复bug");
        }
    }

    @Test
    public void protobufTest() {
        try {
            var buffer = new byte[1024 * 8];
            var length = 0;

            // 序列化和反序列化简单对象
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                protobufSimpleObject.writeTo(codedOutputStream);
                length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = ProtobufObject.ProtobufSimpleObject.parseFrom(codeInput);
            }
            System.out.println(StringUtils.format("[protobuf] [简单对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), length, System.currentTimeMillis() - startTime));

            // 序列化和反序列化常规对象
            startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                protobufNormalObject.writeTo(codedOutputStream);
                length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = ProtobufObject.ProtobufNormalObject.parseFrom(codeInput);
            }
            System.out.println(StringUtils.format("[protobuf] [常规对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), length, System.currentTimeMillis() - startTime));

            // 序列化和反序列化复杂对象
            startTime = System.currentTimeMillis();
            for (int i = 0; i < benchmark; i++) {
                var codedOutputStream = CodedOutputStream.newInstance(buffer);
                protobufComplexObject.writeTo(codedOutputStream);
                length = codedOutputStream.getTotalBytesWritten();
                var codeInput = CodedInputStream.newInstance(buffer, 0, length);
                var mess = ProtobufObject.ProtobufComplexObject.parseFrom(codeInput);
            }
            System.out.println(StringUtils.format("[protobuf] [复杂对象] [thread:{}] [size:{}] [time:{}]", Thread.currentThread().getName(), length, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void zfooMultipleThreadTest() throws InterruptedException {
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            executors[i].execute(() -> {
                zfooTest();
                countdown.countDown();
            });
        }
        countdown.await();
    }


    @Test
    public void kryoMultipleThreadTest() throws InterruptedException {
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            executors[i].execute(() -> {
                kryoTest();
                countdown.countDown();
            });
        }
        countdown.await();
    }

    @Test
    public void protobufMultipleThreadTest() throws InterruptedException {
        var countdown = new CountDownLatch(threadNum);
        for (var i = 0; i < threadNum; i++) {
            executors[i].execute(() -> {
                protobufTest();
                countdown.countDown();
            });
        }
        countdown.await();
    }

    public static final int threadNum = Runtime.getRuntime().availableProcessors() / 2;
    public static final ExecutorService[] executors = new ExecutorService[threadNum];

    // kryo协议注册
    public static final ThreadLocal<Kryo> kryos = new ThreadLocal<>() {
        @Override
        protected Kryo initialValue() {
            var kryo = new Kryo();
            kryo.register(VeryBigObject.class);
            kryo.register(ComplexObject.class);
            kryo.register(NormalObject.class);
            kryo.register(SimpleObject.class);
            kryo.register(ObjectA.class);
            kryo.register(ObjectB.class);
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
            kryo.register(ObjectA[].class);
            kryo.register(ArrayList.class);
            kryo.register(HashSet.class);
            kryo.register(HashMap.class);
            // 关闭循环引用，提高性能
            kryo.setReferences(false);
            return kryo;
        }
    };

    static {
        // zfoo协议注册(其实就是：将Set里面的协议号和对应的类注册好，这样子就可以根据协议号知道是反序列化为哪个类)
        ProtocolManager.initProtocolAuto(Set.of(ComplexObject.class, NormalObject.class, SimpleObject.class, EmptyObject.class, VeryBigObject.class),  GenerateOperation.NO_OPERATION);

        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
    }


    // -------------------------------------------以下为测试用例---------------------------------------------------------------
    // 简单类型
    public static final byte byteValue = 99;
    public static final short shortValue = 9999;
    public static final int intValue = 99999999;
    public static final long longValue = 9999999999999999L;
    public static final float floatValue = 99999999.9F;
    public static final double doubleValue = 99999999.9D;
    public static final String stringValue = "hello";

    // 数组类型
    public static final boolean[] booleanArray = new boolean[]{true, false, true, false, true};
    public static final byte[] byteArray = new byte[]{Byte.MIN_VALUE, -99, 0, 99, Byte.MAX_VALUE};
    public static final short[] shortArray = new short[]{Short.MIN_VALUE, -99, 0, 99, Short.MAX_VALUE};
    public static final int[] intArray = new int[]{Integer.MIN_VALUE, -99999999, -99, 0, 99, 99999999, Integer.MAX_VALUE};
    public static final int[] intArray1 = new int[]{Integer.MIN_VALUE, -99999999, -99, 0, 99, 99999999, Integer.MAX_VALUE - 1};
    public static final int[] intArray2 = new int[]{Integer.MIN_VALUE, -99999999, -99, 0, 99, 99999999, Integer.MAX_VALUE - 2};
    public static final long[] longArray = new long[]{Long.MIN_VALUE, -9999999999999999L, -99999999L, -99L, 0L, 99L, 99999999L, 9999999999999999L, Long.MAX_VALUE};
    public static final float[] floatArray = new float[]{Float.MIN_VALUE, -99999999.9F, -99.9F, 0F, 99.9F, 99999999.9F, Float.MAX_VALUE};
    public static final double[] doubleArray = new double[]{Double.MIN_VALUE, -99999999.9F, -99.9D, 0D, 99.9D, 99999999.9F, Double.MAX_VALUE};
    public static final char[] charArray = new char[]{'a', 'b', 'c', 'd', 'e'};
    public static final String[] stringArray = new String[]{"a", "b", "c", "d", "e"};
    public static final Map<Integer, String> mapWithInteger = new HashMap<>(Map.of(Integer.MIN_VALUE, "a", -99, "b", 0, "c", 99, "d", Integer.MAX_VALUE, "e"));

    public static final ObjectB objectB = new ObjectB(true);
//    public static final ObjectB objectB = new ObjectB(true, 44);
    public static final ObjectA objectA = new ObjectA(Integer.MAX_VALUE, mapWithInteger, objectB);
//    public static final ObjectA objectA = new ObjectA(Integer.MAX_VALUE, mapWithInteger, objectB, 66);
    public static final List<Integer> listWithInteger = new ArrayList<>(ArrayUtils.toList(intArray));
    public static final List<Integer> listWithInteger1 = new ArrayList<>(ArrayUtils.toList(intArray1));
    public static final List<Integer> listWithInteger2 = new ArrayList<>(ArrayUtils.toList(intArray2));
    public static final List<ObjectA> listWithObject = new ArrayList<>(List.of(objectA, objectA, objectA));
    public static final List<List<ObjectA>> listListWithObject = new ArrayList<>(List.of(listWithObject, listWithObject, listWithObject));
    public static final List<List<Integer>> listListWithInteger = new ArrayList<>(List.of(listWithInteger, listWithInteger, listWithInteger));
    public static final List<List<List<Integer>>> listListListWithInteger = new ArrayList<>(List.of(listListWithInteger, listListWithInteger, listListWithInteger));
    public static final List<String> listWithString = new ArrayList<>(ArrayUtils.toList(stringArray));
    public static final Set<Integer> setWithInteger = new HashSet<>(ArrayUtils.toList(intArray));
    public static final Set<Set<List<Integer>>> setSetListWithInteger = new HashSet<>(Set.of(new HashSet<>(Set.of(listWithInteger)), new HashSet<>(Set.of(listWithInteger1)), new HashSet<>(Set.of(listWithInteger2))));
    public static final Set<Set<ObjectA>> setSetWithObject = new HashSet<>(Set.of(new HashSet<>(Set.of(objectA))));
    public static final Set<String> setWithString = new HashSet<>(ArrayUtils.toList(stringArray));
    public static final Map<Integer, ObjectA> mapWithObject = new HashMap<>(Map.of(1, objectA, 2, objectA, 3, objectA));
    public static final Map<ObjectA, List<Integer>> mapWithList = new HashMap<>(Map.of(objectA, listWithInteger));
    public static final Map<List<List<ObjectA>>, List<List<List<Integer>>>> mapWithListList = new HashMap<>(Map.of(new ArrayList<>(List.of(listWithObject, listWithObject, listWithObject)), listListListWithInteger));
    public static final List<Map<Integer, String>> listMap = new ArrayList<>(List.of(mapWithInteger, mapWithInteger, mapWithInteger));
    public static final Set<Map<Integer, String>> setMapWithInteger = new HashSet<>(Set.of(mapWithInteger));
    public static final Map<List<Map<Integer, String>>, Set<Map<Integer, String>>> mapListSet = new HashMap<>(Map.of(listMap, setMapWithInteger));
    public static final Byte[] byteBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(byteArray), Byte.class);
    public static final Short[] shortBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(shortArray), Short.class);
    public static final Integer[] integerArray = ArrayUtils.listToArray(ArrayUtils.toList(intArray), Integer.class);
    public static final Long[] longBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(longArray), Long.class);
    public static final List<Long> listWithLong = ArrayUtils.toList(longArray);
    public static final Float[] floatBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(floatArray), Float.class);
    public static final List<Float> listWithFloat = ArrayUtils.toList(floatArray);
    public static final Double[] doubleBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(doubleArray), Double.class);
    public static final List<Double> listWithDouble = ArrayUtils.toList(doubleArray);
    public static final Boolean[] booleanBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(booleanArray), Boolean.class);
    public static final List<Boolean> listWithBoolean = ArrayUtils.toList(booleanArray);
    public static final Character[] charBoxArray = ArrayUtils.listToArray(ArrayUtils.toList(charArray), Character.class);
    public static final ComplexObject complexObject = new ComplexObject();
    public static final NormalObject normalObject = new NormalObject();
    public static final SimpleObject simpleObject = new SimpleObject();
    public static ProtobufObject.ProtobufComplexObject protobufComplexObject = null;
    public static ProtobufObject.ProtobufNormalObject protobufNormalObject = null;
    public static ProtobufObject.ProtobufSimpleObject protobufSimpleObject = null;


    static {
        complexObject.setA(byteValue);
        complexObject.setAa(byteValue);
        complexObject.setAaa(byteArray);
        complexObject.setAaaa(byteBoxArray);
        complexObject.setB(shortValue);
        complexObject.setBb(shortValue);
        complexObject.setBbb(shortArray);
        complexObject.setBbbb(shortBoxArray);
        complexObject.setC(intValue);
        complexObject.setCc(intValue);
        complexObject.setCcc(intArray);
        complexObject.setCccc(integerArray);
        complexObject.setD(longValue);
        complexObject.setDd(longValue);
        complexObject.setDdd(longArray);
        complexObject.setDddd(longBoxArray);
        complexObject.setE(floatValue);
        complexObject.setEe(floatValue);
        complexObject.setEee(floatArray);
        complexObject.setEeee(floatBoxArray);
        complexObject.setF(doubleValue);
        complexObject.setFf(doubleValue);
        complexObject.setFff(doubleArray);
        complexObject.setFfff(doubleBoxArray);
        complexObject.setG(true);
        complexObject.setGg(true);
        complexObject.setGgg(booleanArray);
        complexObject.setGggg(booleanBoxArray);
        complexObject.setJj(stringValue);
        complexObject.setJjj(stringArray);
        complexObject.setKk(objectA);
        complexObject.setKkk(new ObjectA[]{objectA, objectA});

        complexObject.setL(listWithInteger);
        complexObject.setLl(listListListWithInteger);
        complexObject.setLll(listListWithObject);
        complexObject.setLlll(listWithString);
        complexObject.setLllll(listMap);

        complexObject.setM(mapWithInteger);
        complexObject.setMm(mapWithObject);
        complexObject.setMmm(mapWithList);
        complexObject.setMmmm(mapWithListList);
        complexObject.setMmmmm(mapListSet);

        complexObject.setS(setWithInteger);
        complexObject.setSs(setSetListWithInteger);
        complexObject.setSss(setSetWithObject);
        complexObject.setSsss(setWithString);
        complexObject.setSssss(setMapWithInteger);

        normalObject.setA(byteValue);
        normalObject.setAaa(byteArray);
        normalObject.setB(shortValue);
        normalObject.setC(intValue);
        normalObject.setD(longValue);
        normalObject.setE(floatValue);
        normalObject.setF(doubleValue);
        normalObject.setG(true);
        normalObject.setJj(stringValue);
        normalObject.setKk(objectA);

        normalObject.setL(listWithInteger);
        normalObject.setLl(listWithLong);
        normalObject.setLll(listWithObject);
        normalObject.setLlll(listWithString);

        normalObject.setM(mapWithInteger);
        normalObject.setMm(mapWithObject);

        normalObject.setS(setWithInteger);
        normalObject.setSsss(setWithString);

        simpleObject.setC(intValue);
        simpleObject.setG(true);

        // protobuf相关
        var protobufObjectB = ProtobufObject.ObjectB.newBuilder().setFlag(false).build();
        var protobufObjectA = ProtobufObject.ObjectA.newBuilder()
                .setA(Integer.MAX_VALUE)
                .putAllM(mapWithInteger)
                .setObjectB(protobufObjectB)
                .build();
        var protobufListInteger = ProtobufObject.ListInteger.newBuilder().addAllA(listWithInteger).build();
        var protobufListListInteger = ProtobufObject.ListListInteger.newBuilder()
                .addAllA(List.of(protobufListInteger, protobufListInteger, protobufListInteger))
                .build();
        var protobufListListListInteger = ProtobufObject.ListListListInteger.newBuilder()
                .addAllA(List.of(protobufListListInteger, protobufListListInteger, protobufListListInteger))
                .build();
        var protobufListObjectA = ProtobufObject.ListObjectA.newBuilder()
                .addAllA(List.of(protobufObjectA, protobufObjectA, protobufObjectA))
                .build();
        var protobufListListObjectA = ProtobufObject.ListListObjectA.newBuilder()
                .addAllA(List.of(protobufListObjectA, protobufListObjectA, protobufListObjectA))
                .build();
        var rawProtobufListListObjectA = List.of(protobufListObjectA, protobufListObjectA, protobufListObjectA);
        var rawProtobufListListListInteger = List.of(protobufListListInteger, protobufListListInteger, protobufListListInteger);
        var rawProtobufListWithObject = new ArrayList<>(List.of(protobufObjectA, protobufObjectA, protobufObjectA));
        var protobufMapIntegerString = ProtobufObject.MapIntegerString.newBuilder().putAllA(mapWithInteger).build();
        var rawProtobufListMapIntegerString = List.of(protobufMapIntegerString, protobufMapIntegerString, protobufMapIntegerString);
        var protobufMapObjectA = ProtobufObject.MapObjectA.newBuilder()
                .setKey(protobufObjectA)
                .setValue(protobufListInteger)
                .build();
        var protobufMapListListObjectA = ProtobufObject.MapListListObjectA.newBuilder()
                .setKey(protobufListListObjectA)
                .setValue(protobufListListListInteger)
                .build();
        var protobufListMapIntegerStringKey = ProtobufObject.ListMapIntegerString.newBuilder()
                .addAllA(rawProtobufListMapIntegerString)
                .build();
        var protobufListMapIntegerStringValue = ProtobufObject.ListMapIntegerString.newBuilder()
                .addAllA(List.of(protobufMapIntegerString))
                .build();
        var protobufMapListMapInteger = ProtobufObject.MapListMapInteger.newBuilder()
                .setKey(protobufListMapIntegerStringKey)
                .setValue(protobufListMapIntegerStringValue)
                .build();


        var protobufComplexBuilder = ProtobufObject.ProtobufComplexObject.newBuilder();
        var protobufNormalBuilder = ProtobufObject.ProtobufNormalObject.newBuilder();
        var protobufSimpleBuilder = ProtobufObject.ProtobufSimpleObject.newBuilder();
        protobufComplexBuilder.setA(byteValue);
        protobufComplexBuilder.setAa(byteValue);
        protobufComplexBuilder.setAaa(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setAaaa(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setB(shortValue);
        protobufComplexBuilder.setBb(shortValue);
        protobufComplexBuilder.setBbb(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setBbbb(ByteString.copyFrom(byteArray));
        protobufComplexBuilder.setC(intValue);
        protobufComplexBuilder.setCc(intValue);
        protobufComplexBuilder.addAllCcc(listWithInteger);
        protobufComplexBuilder.addAllCccc(listWithInteger);
        protobufComplexBuilder.setD(longValue);
        protobufComplexBuilder.setDd(longValue);
        protobufComplexBuilder.addAllDdd(listWithLong);
        protobufComplexBuilder.addAllDddd(listWithLong);
        protobufComplexBuilder.setE(floatValue);
        protobufComplexBuilder.setEe(floatValue);
        protobufComplexBuilder.addAllEee(listWithFloat);
        protobufComplexBuilder.addAllEeee(listWithFloat);
        protobufComplexBuilder.setF(doubleValue);
        protobufComplexBuilder.setFf(doubleValue);
        protobufComplexBuilder.addAllFff(listWithDouble);
        protobufComplexBuilder.addAllFfff(listWithDouble);
        protobufComplexBuilder.setG(true);
        protobufComplexBuilder.setGg(true);
        protobufComplexBuilder.addAllGgg(listWithBoolean);
        protobufComplexBuilder.addAllGggg(listWithBoolean);
        protobufComplexBuilder.addAllHhh(listWithString);
        protobufComplexBuilder.addAllHhhh(listWithString);
        protobufComplexBuilder.setJj(stringValue);
        protobufComplexBuilder.addAllJjj(listWithString);
        protobufComplexBuilder.setKk(protobufObjectA);
        protobufComplexBuilder.addAllKkk(rawProtobufListWithObject);
        protobufComplexBuilder.addAllL(listWithInteger);
        protobufComplexBuilder.addAllLl(rawProtobufListListListInteger);
        protobufComplexBuilder.addAllLll(rawProtobufListListObjectA);
        protobufComplexBuilder.addAllLlll(listWithString);
        protobufComplexBuilder.addAllLllll(rawProtobufListMapIntegerString);
        protobufComplexBuilder.putAllM(mapWithInteger);
        protobufComplexBuilder.putAllMm(Map.of(1, protobufObjectA, 2, protobufObjectA, 3, protobufObjectA));
        protobufComplexBuilder.addMmm(protobufMapObjectA);
        protobufComplexBuilder.addMmmm(protobufMapListListObjectA);
        protobufComplexBuilder.addMmmmm(protobufMapListMapInteger);
        protobufComplexBuilder.addAllS(listWithInteger);
        protobufComplexBuilder.addAllSs(rawProtobufListListListInteger);
        protobufComplexBuilder.addAllSss(rawProtobufListListObjectA);
        protobufComplexBuilder.addAllSsss(listWithString);
        protobufComplexBuilder.addAllSssss(rawProtobufListMapIntegerString);
        protobufComplexObject = protobufComplexBuilder.build();

        protobufNormalBuilder.setA(byteValue);
        protobufNormalBuilder.setAaa(ByteString.copyFrom(byteArray));
        protobufNormalBuilder.setB(shortValue);
        protobufNormalBuilder.setC(intValue);
        protobufNormalBuilder.setD(intValue);
        protobufNormalBuilder.setE(longValue);
        protobufNormalBuilder.setF(doubleValue);
        protobufNormalBuilder.setG(true);
        protobufNormalBuilder.setJj(stringValue);
        protobufNormalBuilder.setKk(protobufObjectA);
        protobufNormalBuilder.addAllL(listWithInteger);
        protobufNormalBuilder.addAllLl(listWithLong);
        protobufNormalBuilder.addAllLll(rawProtobufListWithObject);
        protobufNormalBuilder.addAllLlll(listWithString);
        protobufNormalBuilder.putAllM(mapWithInteger);
        protobufNormalBuilder.putAllMm(Map.of(1, protobufObjectA, 2, protobufObjectA, 3, protobufObjectA));
        protobufNormalBuilder.addAllS(listWithInteger);
        protobufNormalBuilder.addAllSsss(listWithString);
        protobufNormalObject = protobufNormalBuilder.build();

        protobufSimpleBuilder.setC(intValue);
        protobufSimpleBuilder.setG(true);
        protobufSimpleObject = protobufSimpleBuilder.build();
    }


}
