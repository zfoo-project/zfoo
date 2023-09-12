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

package com.zfoo.net.packet.csharp;

import com.zfoo.protocol.anno.Protocol;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 */
@Protocol(id = 1165)
public class CM_CSharpRequest {

    // 注释1
    public byte a;
    /**
     * 注释2
     * 附加注释
     */
    public Byte aa;
    public byte[] aaa;
    public Byte[] aaaa;

    public short b;
    public Short bb;
    public short[] bbb;
    public Short[] bbbb;

    public int c;
    public Integer cc;
    public int[] ccc;
    public Integer[] cccc;

    public long d;
    public Long dd;
    public long[] ddd;
    public Long[] dddd;

    public float e;
    public Float ee;
    public float[] eee;
    public Float[] eeee;

    public double f;
    public Double ff;
    public double[] fff;
    public Double[] ffff;

    public boolean g;
    public Boolean gg;
    public boolean[] ggg;
    public Boolean[] gggg;

    // 注释行
    public char h;
    public Character hh;
    public char[] hhh;
    public Character[] hhhh;

    public String jj;
    public String[] jjj;

    public CSharpObjectA objectA;
    public CSharpObjectA[] objectArray;

    public List<Integer> l;
    public List<List<List<Integer>>> ll;
    public List<List<CSharpObjectA>> lll;
    public List<String> llll;
    public List<Map<Integer, String>> lllll;

    public Map<Integer, String> m;
    public Map<Integer, CSharpObjectA> mm;
    public Map<CSharpObjectA, List<Integer>> mmm;
    public Map<List<List<CSharpObjectA>>, List<List<List<Integer>>>> mmmm;

    public Set<Integer> s;
    public Set<Set<List<Integer>>> ss;
    public Set<Set<CSharpObjectA>> sss;
    public Set<String> ssss;
    /**
     * 注释z
     * 附加注释
     */
    public Set<Map<Integer, String>> sssss;

}
