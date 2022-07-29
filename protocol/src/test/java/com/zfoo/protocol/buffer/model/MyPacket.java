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

package com.zfoo.protocol.buffer.model;

import com.zfoo.protocol.IPacket;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 * @version 3.0
 */
public class MyPacket implements IPacket {

    public static final transient short PROTOCOL_ID = 1;

    // 基础类型
    public byte base1;
    public short base2;
    public int base3;
    public long base4;
    public float base5;
    public double base6;
    public boolean base7;
    public char base8;

    // 包装类型
    public Byte box1;
    public Short box2;
    public Integer box3;
    public Long box4;
    public Float box5;
    public Double box6;
    public Boolean box7;
    public Character box8;

    // string
    public String str;
    public MyObjectA objectA;

    // 数组
    public byte[] array1;
    public Byte[] array2;
    public short[] array3;
    public Short[] array4;
    public int[] array5;
    public Integer[] array6;
    public long[] array7;
    public Long[] array8;
    public float[] array9;
    public Float[] array10;
    public double[] array11;
    public Double[] array12;
    public boolean[] array13;
    public Boolean[] array14;
    public char[] array15;
    public Character[] array16;
    public String[] array17;
    public MyObjectA[] array18;

    // list
    public List<Byte> list1;
    public List<Short> list2;
    public List<Integer> list3;
    public List<Long> list4;
    public List<Float> list5;
    public List<Double> list6;
    public List<Boolean> list7;
    public List<Character> list8;
    public List<String> list9;
    public List<MyObjectA> list10;


    // set
    public Set<Byte> set1;
    public Set<Short> set2;
    public Set<Integer> set3;
    public Set<Long> set4;
    public Set<Float> set5;
    public Set<Double> set6;
    public Set<Boolean> set7;
    public Set<Character> set8;
    public Set<String> set9;
    public Set<MyObjectA> set10;


    // map
    public Map<Integer, Integer> map1;
    public Map<Integer, Long> map2;
    public Map<Integer, String> map3;
    public Map<Integer, MyObjectA> map4;
    public Map<Long, Integer> map5;
    public Map<Long, Long> map6;
    public Map<Long, String> map7;
    public Map<Long, MyObjectA> map8;
    public Map<String, Integer> map9;
    public Map<String, Long> map10;
    public Map<String, String> map11;
    public Map<String, MyObjectA> map12;
    public Map<Byte, Byte> map15;
    public Map<Short, Short> map16;
    public Map<Float, Float> map17;
    public Map<Double, Double> map18;
    public Map<Boolean, Boolean> map19;
    public Map<Character, Character> map20;
    public Map<MyObjectA, MyObjectA> map21;
    public Map<MyObjectA, List<Integer>> map22;

    // mix
    public List<List<Integer>> listList1;
    public List<List<MyObjectA>> listList2;
    public List<Set<Integer>> listSet1;
    public List<Set<MyObjectA>> listSet2;
    public List<Map<Integer, Integer>> listMap1;
    public List<Map<MyObjectA, Integer>> listMap2;
    public List<List<Set<Integer>>> listListSet;
    public List<List<Map<Integer, Integer>>> listListMap;

    public Set<List<Integer>> setList1;
    public Set<List<MyObjectA>> setList2;
    public Set<Set<Integer>> setSet1;
    public Set<Set<MyObjectA>> setSet2;
    public Set<Map<Integer, Integer>> setMap1;
    public Set<Map<MyObjectA, Integer>> setMap2;
    public Set<List<Set<Integer>>> setListSet;
    public Set<List<Map<Integer, Integer>>> setListMap;

    public Map<Integer, List<Integer>> mapList1;
    public Map<MyObjectA, List<MyObjectA>> mapList2;
    public Map<Set<Integer>, Integer> mapSet1;
    public Map<Set<MyObjectA>, MyObjectA> mapSet2;
    public Map<Integer, Map<Integer, Integer>> mapMap1;
    public Map<MyObjectA, Map<MyObjectA, Integer>> mapMap2;
    public Map<Integer, List<Set<Integer>>> mapListSet;
    public Map<MyObjectA, List<Map<Integer, Integer>>> mapListMap;

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }


}
