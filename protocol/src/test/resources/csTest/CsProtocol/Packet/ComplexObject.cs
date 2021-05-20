using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    // 复杂的对象
    // 包括了各种复杂的结构，数组，List，Set，Map
    //
    // @author jaysunxiao
    // @version 1.0
    // @since 2017 10.14 11:19
    public class ComplexObject : IPacket
    {
        // byte类型，最简单的整形
        public byte a;
        // byte的包装类型
        // 优先使用基础类型，包装类型会有装箱拆箱
        public byte aa;
        // 数组类型
        public byte[] aaa;
        public byte[] aaaa;
        public short b;
        public short bb;
        public short[] bbb;
        public short[] bbbb;
        public int c;
        public int cc;
        public int[] ccc;
        public int[] cccc;
        public long d;
        public long dd;
        public long[] ddd;
        public long[] dddd;
        public float e;
        public float ee;
        public float[] eee;
        public float[] eeee;
        public double f;
        public double ff;
        public double[] fff;
        public double[] ffff;
        public bool g;
        public bool gg;
        public bool[] ggg;
        public bool[] gggg;
        public char h;
        public char hh;
        public char[] hhh;
        public char[] hhhh;
        public string jj;
        public string[] jjj;
        public ObjectA kk;
        public ObjectA[] kkk;
        public List<int> l;
        public List<List<List<int>>> ll;
        public List<List<ObjectA>> lll;
        public List<string> llll;
        public List<Dictionary<int, string>> lllll;
        public Dictionary<int, string> m;
        public Dictionary<int, ObjectA> mm;
        public Dictionary<ObjectA, List<int>> mmm;
        public Dictionary<List<List<ObjectA>>, List<List<List<int>>>> mmmm;
        public Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>> mmmmm;
        public HashSet<int> s;
        public HashSet<HashSet<List<int>>> ss;
        public HashSet<HashSet<ObjectA>> sss;
        public HashSet<string> ssss;
        public HashSet<Dictionary<int, string>> sssss;

        public static ComplexObject ValueOf(byte a, byte aa, byte[] aaa, byte[] aaaa, short b, short bb, short[] bbb, short[] bbbb, int c, int cc, int[] ccc, int[] cccc, long d, long dd, long[] ddd, long[] dddd, float e, float ee, float[] eee, float[] eeee, double f, double ff, double[] fff, double[] ffff, bool g, bool gg, bool[] ggg, bool[] gggg, char h, char hh, char[] hhh, char[] hhhh, string jj, string[] jjj, ObjectA kk, ObjectA[] kkk, List<int> l, List<List<List<int>>> ll, List<List<ObjectA>> lll, List<string> llll, List<Dictionary<int, string>> lllll, Dictionary<int, string> m, Dictionary<int, ObjectA> mm, Dictionary<ObjectA, List<int>> mmm, Dictionary<List<List<ObjectA>>, List<List<List<int>>>> mmmm, Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>> mmmmm, HashSet<int> s, HashSet<HashSet<List<int>>> ss, HashSet<HashSet<ObjectA>> sss, HashSet<string> ssss, HashSet<Dictionary<int, string>> sssss)
        {
            var packet = new ComplexObject();
            packet.a = a;
            packet.aa = aa;
            packet.aaa = aaa;
            packet.aaaa = aaaa;
            packet.b = b;
            packet.bb = bb;
            packet.bbb = bbb;
            packet.bbbb = bbbb;
            packet.c = c;
            packet.cc = cc;
            packet.ccc = ccc;
            packet.cccc = cccc;
            packet.d = d;
            packet.dd = dd;
            packet.ddd = ddd;
            packet.dddd = dddd;
            packet.e = e;
            packet.ee = ee;
            packet.eee = eee;
            packet.eeee = eeee;
            packet.f = f;
            packet.ff = ff;
            packet.fff = fff;
            packet.ffff = ffff;
            packet.g = g;
            packet.gg = gg;
            packet.ggg = ggg;
            packet.gggg = gggg;
            packet.h = h;
            packet.hh = hh;
            packet.hhh = hhh;
            packet.hhhh = hhhh;
            packet.jj = jj;
            packet.jjj = jjj;
            packet.kk = kk;
            packet.kkk = kkk;
            packet.l = l;
            packet.ll = ll;
            packet.lll = lll;
            packet.llll = llll;
            packet.lllll = lllll;
            packet.m = m;
            packet.mm = mm;
            packet.mmm = mmm;
            packet.mmmm = mmmm;
            packet.mmmmm = mmmmm;
            packet.s = s;
            packet.ss = ss;
            packet.sss = sss;
            packet.ssss = ssss;
            packet.sssss = sssss;
            return packet;
        }


        public short ProtocolId()
        {
            return 1160;
        }
    }


    public class ComplexObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 1160;
        }

        public void Write(ByteBuffer buffer, IPacket packet)
        {
            if (packet == null)
            {
                buffer.WriteBool(false);
                return;
            }
            buffer.WriteBool(true);
            ComplexObject message = (ComplexObject) packet;
            buffer.WriteByte(message.a);
            buffer.WriteByte(message.aa);
            if ((message.aaa == null) || (message.aaa.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.aaa.Length);
                int length0 = message.aaa.Length;
                for (int i1 = 0; i1 < length0; i1++)
                {
                    byte element2 = message.aaa[i1];
                    buffer.WriteByte(element2);
                }
            }
            if ((message.aaaa == null) || (message.aaaa.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.aaaa.Length);
                int length3 = message.aaaa.Length;
                for (int i4 = 0; i4 < length3; i4++)
                {
                    byte element5 = message.aaaa[i4];
                    buffer.WriteByte(element5);
                }
            }
            buffer.WriteShort(message.b);
            buffer.WriteShort(message.bb);
            if ((message.bbb == null) || (message.bbb.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.bbb.Length);
                int length6 = message.bbb.Length;
                for (int i7 = 0; i7 < length6; i7++)
                {
                    short element8 = message.bbb[i7];
                    buffer.WriteShort(element8);
                }
            }
            if ((message.bbbb == null) || (message.bbbb.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.bbbb.Length);
                int length9 = message.bbbb.Length;
                for (int i10 = 0; i10 < length9; i10++)
                {
                    short element11 = message.bbbb[i10];
                    buffer.WriteShort(element11);
                }
            }
            buffer.WriteInt(message.c);
            buffer.WriteInt(message.cc);
            if ((message.ccc == null) || (message.ccc.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ccc.Length);
                int length12 = message.ccc.Length;
                for (int i13 = 0; i13 < length12; i13++)
                {
                    int element14 = message.ccc[i13];
                    buffer.WriteInt(element14);
                }
            }
            if ((message.cccc == null) || (message.cccc.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.cccc.Length);
                int length15 = message.cccc.Length;
                for (int i16 = 0; i16 < length15; i16++)
                {
                    int element17 = message.cccc[i16];
                    buffer.WriteInt(element17);
                }
            }
            buffer.WriteLong(message.d);
            buffer.WriteLong(message.dd);
            if ((message.ddd == null) || (message.ddd.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ddd.Length);
                int length18 = message.ddd.Length;
                for (int i19 = 0; i19 < length18; i19++)
                {
                    long element20 = message.ddd[i19];
                    buffer.WriteLong(element20);
                }
            }
            if ((message.dddd == null) || (message.dddd.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.dddd.Length);
                int length21 = message.dddd.Length;
                for (int i22 = 0; i22 < length21; i22++)
                {
                    long element23 = message.dddd[i22];
                    buffer.WriteLong(element23);
                }
            }
            buffer.WriteFloat(message.e);
            buffer.WriteFloat(message.ee);
            if ((message.eee == null) || (message.eee.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.eee.Length);
                int length24 = message.eee.Length;
                for (int i25 = 0; i25 < length24; i25++)
                {
                    float element26 = message.eee[i25];
                    buffer.WriteFloat(element26);
                }
            }
            if ((message.eeee == null) || (message.eeee.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.eeee.Length);
                int length27 = message.eeee.Length;
                for (int i28 = 0; i28 < length27; i28++)
                {
                    float element29 = message.eeee[i28];
                    buffer.WriteFloat(element29);
                }
            }
            buffer.WriteDouble(message.f);
            buffer.WriteDouble(message.ff);
            if ((message.fff == null) || (message.fff.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.fff.Length);
                int length30 = message.fff.Length;
                for (int i31 = 0; i31 < length30; i31++)
                {
                    double element32 = message.fff[i31];
                    buffer.WriteDouble(element32);
                }
            }
            if ((message.ffff == null) || (message.ffff.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ffff.Length);
                int length33 = message.ffff.Length;
                for (int i34 = 0; i34 < length33; i34++)
                {
                    double element35 = message.ffff[i34];
                    buffer.WriteDouble(element35);
                }
            }
            buffer.WriteBool(message.g);
            buffer.WriteBool(message.gg);
            if ((message.ggg == null) || (message.ggg.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ggg.Length);
                int length36 = message.ggg.Length;
                for (int i37 = 0; i37 < length36; i37++)
                {
                    bool element38 = message.ggg[i37];
                    buffer.WriteBool(element38);
                }
            }
            if ((message.gggg == null) || (message.gggg.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.gggg.Length);
                int length39 = message.gggg.Length;
                for (int i40 = 0; i40 < length39; i40++)
                {
                    bool element41 = message.gggg[i40];
                    buffer.WriteBool(element41);
                }
            }
            buffer.WriteChar(message.h);
            buffer.WriteChar(message.hh);
            if ((message.hhh == null) || (message.hhh.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.hhh.Length);
                int length42 = message.hhh.Length;
                for (int i43 = 0; i43 < length42; i43++)
                {
                    char element44 = message.hhh[i43];
                    buffer.WriteChar(element44);
                }
            }
            if ((message.hhhh == null) || (message.hhhh.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.hhhh.Length);
                int length45 = message.hhhh.Length;
                for (int i46 = 0; i46 < length45; i46++)
                {
                    char element47 = message.hhhh[i46];
                    buffer.WriteChar(element47);
                }
            }
            buffer.WriteString(message.jj);
            if ((message.jjj == null) || (message.jjj.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.jjj.Length);
                int length48 = message.jjj.Length;
                for (int i49 = 0; i49 < length48; i49++)
                {
                    string element50 = message.jjj[i49];
                    buffer.WriteString(element50);
                }
            }
            ProtocolManager.GetProtocol(1116).Write(buffer, message.kk);
            if ((message.kkk == null) || (message.kkk.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.kkk.Length);
                int length51 = message.kkk.Length;
                for (int i52 = 0; i52 < length51; i52++)
                {
                    ObjectA element53 = message.kkk[i52];
                    ProtocolManager.GetProtocol(1116).Write(buffer, element53);
                }
            }
            if (message.l == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.l.Count);
                int length54 = message.l.Count;
                for (int i55 = 0; i55 < length54; i55++)
                {
                    var element56 = message.l[i55];
                    buffer.WriteInt(element56);
                }
            }
            if (message.ll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ll.Count);
                int length57 = message.ll.Count;
                for (int i58 = 0; i58 < length57; i58++)
                {
                    var element59 = message.ll[i58];
                    if (element59 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(element59.Count);
                        int length60 = element59.Count;
                        for (int i61 = 0; i61 < length60; i61++)
                        {
                            var element62 = element59[i61];
                            if (element62 == null)
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(element62.Count);
                                int length63 = element62.Count;
                                for (int i64 = 0; i64 < length63; i64++)
                                {
                                    var element65 = element62[i64];
                                    buffer.WriteInt(element65);
                                }
                            }
                        }
                    }
                }
            }
            if (message.lll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.lll.Count);
                int length66 = message.lll.Count;
                for (int i67 = 0; i67 < length66; i67++)
                {
                    var element68 = message.lll[i67];
                    if (element68 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(element68.Count);
                        int length69 = element68.Count;
                        for (int i70 = 0; i70 < length69; i70++)
                        {
                            var element71 = element68[i70];
                            ProtocolManager.GetProtocol(1116).Write(buffer, element71);
                        }
                    }
                }
            }
            if (message.llll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.llll.Count);
                int length72 = message.llll.Count;
                for (int i73 = 0; i73 < length72; i73++)
                {
                    var element74 = message.llll[i73];
                    buffer.WriteString(element74);
                }
            }
            if (message.lllll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.lllll.Count);
                int length75 = message.lllll.Count;
                for (int i76 = 0; i76 < length75; i76++)
                {
                    var element77 = message.lllll[i76];
                    if ((element77 == null) || (element77.Count == 0))
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(element77.Count);
                        foreach (var i78 in element77)
                        {
                            var keyElement79 = i78.Key;
                            var valueElement80 = i78.Value;
                            buffer.WriteInt(keyElement79);
                            buffer.WriteString(valueElement80);
                        }
                    }
                }
            }
            if ((message.m == null) || (message.m.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.m.Count);
                foreach (var i81 in message.m)
                {
                    var keyElement82 = i81.Key;
                    var valueElement83 = i81.Value;
                    buffer.WriteInt(keyElement82);
                    buffer.WriteString(valueElement83);
                }
            }
            if ((message.mm == null) || (message.mm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mm.Count);
                foreach (var i84 in message.mm)
                {
                    var keyElement85 = i84.Key;
                    var valueElement86 = i84.Value;
                    buffer.WriteInt(keyElement85);
                    ProtocolManager.GetProtocol(1116).Write(buffer, valueElement86);
                }
            }
            if ((message.mmm == null) || (message.mmm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mmm.Count);
                foreach (var i87 in message.mmm)
                {
                    var keyElement88 = i87.Key;
                    var valueElement89 = i87.Value;
                    ProtocolManager.GetProtocol(1116).Write(buffer, keyElement88);
                    if (valueElement89 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(valueElement89.Count);
                        int length90 = valueElement89.Count;
                        for (int i91 = 0; i91 < length90; i91++)
                        {
                            var element92 = valueElement89[i91];
                            buffer.WriteInt(element92);
                        }
                    }
                }
            }
            if ((message.mmmm == null) || (message.mmmm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mmmm.Count);
                foreach (var i93 in message.mmmm)
                {
                    var keyElement94 = i93.Key;
                    var valueElement95 = i93.Value;
                    if (keyElement94 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(keyElement94.Count);
                        int length96 = keyElement94.Count;
                        for (int i97 = 0; i97 < length96; i97++)
                        {
                            var element98 = keyElement94[i97];
                            if (element98 == null)
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(element98.Count);
                                int length99 = element98.Count;
                                for (int i100 = 0; i100 < length99; i100++)
                                {
                                    var element101 = element98[i100];
                                    ProtocolManager.GetProtocol(1116).Write(buffer, element101);
                                }
                            }
                        }
                    }
                    if (valueElement95 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(valueElement95.Count);
                        int length102 = valueElement95.Count;
                        for (int i103 = 0; i103 < length102; i103++)
                        {
                            var element104 = valueElement95[i103];
                            if (element104 == null)
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(element104.Count);
                                int length105 = element104.Count;
                                for (int i106 = 0; i106 < length105; i106++)
                                {
                                    var element107 = element104[i106];
                                    if (element107 == null)
                                    {
                                        buffer.WriteInt(0);
                                    }
                                    else
                                    {
                                        buffer.WriteInt(element107.Count);
                                        int length108 = element107.Count;
                                        for (int i109 = 0; i109 < length108; i109++)
                                        {
                                            var element110 = element107[i109];
                                            buffer.WriteInt(element110);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ((message.mmmmm == null) || (message.mmmmm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mmmmm.Count);
                foreach (var i111 in message.mmmmm)
                {
                    var keyElement112 = i111.Key;
                    var valueElement113 = i111.Value;
                    if (keyElement112 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(keyElement112.Count);
                        int length114 = keyElement112.Count;
                        for (int i115 = 0; i115 < length114; i115++)
                        {
                            var element116 = keyElement112[i115];
                            if ((element116 == null) || (element116.Count == 0))
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(element116.Count);
                                foreach (var i117 in element116)
                                {
                                    var keyElement118 = i117.Key;
                                    var valueElement119 = i117.Value;
                                    buffer.WriteInt(keyElement118);
                                    buffer.WriteString(valueElement119);
                                }
                            }
                        }
                    }
                    if (valueElement113 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(valueElement113.Count);
                        foreach (var i120 in valueElement113)
                        {
                            if ((i120 == null) || (i120.Count == 0))
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(i120.Count);
                                foreach (var i121 in i120)
                                {
                                    var keyElement122 = i121.Key;
                                    var valueElement123 = i121.Value;
                                    buffer.WriteInt(keyElement122);
                                    buffer.WriteString(valueElement123);
                                }
                            }
                        }
                    }
                }
            }
            if (message.s == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.s.Count);
                foreach (var i124 in message.s)
                {
                    buffer.WriteInt(i124);
                }
            }
            if (message.ss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ss.Count);
                foreach (var i125 in message.ss)
                {
                    if (i125 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(i125.Count);
                        foreach (var i126 in i125)
                        {
                            if (i126 == null)
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(i126.Count);
                                int length127 = i126.Count;
                                for (int i128 = 0; i128 < length127; i128++)
                                {
                                    var element129 = i126[i128];
                                    buffer.WriteInt(element129);
                                }
                            }
                        }
                    }
                }
            }
            if (message.sss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.sss.Count);
                foreach (var i130 in message.sss)
                {
                    if (i130 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(i130.Count);
                        foreach (var i131 in i130)
                        {
                            ProtocolManager.GetProtocol(1116).Write(buffer, i131);
                        }
                    }
                }
            }
            if (message.ssss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ssss.Count);
                foreach (var i132 in message.ssss)
                {
                    buffer.WriteString(i132);
                }
            }
            if (message.sssss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.sssss.Count);
                foreach (var i133 in message.sssss)
                {
                    if ((i133 == null) || (i133.Count == 0))
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(i133.Count);
                        foreach (var i134 in i133)
                        {
                            var keyElement135 = i134.Key;
                            var valueElement136 = i134.Value;
                            buffer.WriteInt(keyElement135);
                            buffer.WriteString(valueElement136);
                        }
                    }
                }
            }
        }

        public IPacket Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            ComplexObject packet = new ComplexObject();
            byte result137 = buffer.ReadByte();
            packet.a = result137;
            byte result138 = buffer.ReadByte();
            packet.aa = result138;
            int size141 = buffer.ReadInt();
            byte[] result139 = new byte[size141];
            if (size141 > 0)
            {
                for (int index140 = 0; index140 < size141; index140++)
                {
                    byte result142 = buffer.ReadByte();
                    result139[index140] = result142;
                }
            }
            packet.aaa = result139;
            int size145 = buffer.ReadInt();
            byte[] result143 = new byte[size145];
            if (size145 > 0)
            {
                for (int index144 = 0; index144 < size145; index144++)
                {
                    byte result146 = buffer.ReadByte();
                    result143[index144] = result146;
                }
            }
            packet.aaaa = result143;
            short result147 = buffer.ReadShort();
            packet.b = result147;
            short result148 = buffer.ReadShort();
            packet.bb = result148;
            int size151 = buffer.ReadInt();
            short[] result149 = new short[size151];
            if (size151 > 0)
            {
                for (int index150 = 0; index150 < size151; index150++)
                {
                    short result152 = buffer.ReadShort();
                    result149[index150] = result152;
                }
            }
            packet.bbb = result149;
            int size155 = buffer.ReadInt();
            short[] result153 = new short[size155];
            if (size155 > 0)
            {
                for (int index154 = 0; index154 < size155; index154++)
                {
                    short result156 = buffer.ReadShort();
                    result153[index154] = result156;
                }
            }
            packet.bbbb = result153;
            int result157 = buffer.ReadInt();
            packet.c = result157;
            int result158 = buffer.ReadInt();
            packet.cc = result158;
            int size161 = buffer.ReadInt();
            int[] result159 = new int[size161];
            if (size161 > 0)
            {
                for (int index160 = 0; index160 < size161; index160++)
                {
                    int result162 = buffer.ReadInt();
                    result159[index160] = result162;
                }
            }
            packet.ccc = result159;
            int size165 = buffer.ReadInt();
            int[] result163 = new int[size165];
            if (size165 > 0)
            {
                for (int index164 = 0; index164 < size165; index164++)
                {
                    int result166 = buffer.ReadInt();
                    result163[index164] = result166;
                }
            }
            packet.cccc = result163;
            long result167 = buffer.ReadLong();
            packet.d = result167;
            long result168 = buffer.ReadLong();
            packet.dd = result168;
            int size171 = buffer.ReadInt();
            long[] result169 = new long[size171];
            if (size171 > 0)
            {
                for (int index170 = 0; index170 < size171; index170++)
                {
                    long result172 = buffer.ReadLong();
                    result169[index170] = result172;
                }
            }
            packet.ddd = result169;
            int size175 = buffer.ReadInt();
            long[] result173 = new long[size175];
            if (size175 > 0)
            {
                for (int index174 = 0; index174 < size175; index174++)
                {
                    long result176 = buffer.ReadLong();
                    result173[index174] = result176;
                }
            }
            packet.dddd = result173;
            float result177 = buffer.ReadFloat();
            packet.e = result177;
            float result178 = buffer.ReadFloat();
            packet.ee = result178;
            int size181 = buffer.ReadInt();
            float[] result179 = new float[size181];
            if (size181 > 0)
            {
                for (int index180 = 0; index180 < size181; index180++)
                {
                    float result182 = buffer.ReadFloat();
                    result179[index180] = result182;
                }
            }
            packet.eee = result179;
            int size185 = buffer.ReadInt();
            float[] result183 = new float[size185];
            if (size185 > 0)
            {
                for (int index184 = 0; index184 < size185; index184++)
                {
                    float result186 = buffer.ReadFloat();
                    result183[index184] = result186;
                }
            }
            packet.eeee = result183;
            double result187 = buffer.ReadDouble();
            packet.f = result187;
            double result188 = buffer.ReadDouble();
            packet.ff = result188;
            int size191 = buffer.ReadInt();
            double[] result189 = new double[size191];
            if (size191 > 0)
            {
                for (int index190 = 0; index190 < size191; index190++)
                {
                    double result192 = buffer.ReadDouble();
                    result189[index190] = result192;
                }
            }
            packet.fff = result189;
            int size195 = buffer.ReadInt();
            double[] result193 = new double[size195];
            if (size195 > 0)
            {
                for (int index194 = 0; index194 < size195; index194++)
                {
                    double result196 = buffer.ReadDouble();
                    result193[index194] = result196;
                }
            }
            packet.ffff = result193;
            bool result197 = buffer.ReadBool();
            packet.g = result197;
            bool result198 = buffer.ReadBool();
            packet.gg = result198;
            int size201 = buffer.ReadInt();
            bool[] result199 = new bool[size201];
            if (size201 > 0)
            {
                for (int index200 = 0; index200 < size201; index200++)
                {
                    bool result202 = buffer.ReadBool();
                    result199[index200] = result202;
                }
            }
            packet.ggg = result199;
            int size205 = buffer.ReadInt();
            bool[] result203 = new bool[size205];
            if (size205 > 0)
            {
                for (int index204 = 0; index204 < size205; index204++)
                {
                    bool result206 = buffer.ReadBool();
                    result203[index204] = result206;
                }
            }
            packet.gggg = result203;
            char result207 = buffer.ReadChar();
            packet.h = result207;
            char result208 = buffer.ReadChar();
            packet.hh = result208;
            int size211 = buffer.ReadInt();
            char[] result209 = new char[size211];
            if (size211 > 0)
            {
                for (int index210 = 0; index210 < size211; index210++)
                {
                    char result212 = buffer.ReadChar();
                    result209[index210] = result212;
                }
            }
            packet.hhh = result209;
            int size215 = buffer.ReadInt();
            char[] result213 = new char[size215];
            if (size215 > 0)
            {
                for (int index214 = 0; index214 < size215; index214++)
                {
                    char result216 = buffer.ReadChar();
                    result213[index214] = result216;
                }
            }
            packet.hhhh = result213;
            string result217 = buffer.ReadString();
            packet.jj = result217;
            int size220 = buffer.ReadInt();
            string[] result218 = new string[size220];
            if (size220 > 0)
            {
                for (int index219 = 0; index219 < size220; index219++)
                {
                    string result221 = buffer.ReadString();
                    result218[index219] = result221;
                }
            }
            packet.jjj = result218;
            ObjectA result222 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
            packet.kk = result222;
            int size225 = buffer.ReadInt();
            ObjectA[] result223 = new ObjectA[size225];
            if (size225 > 0)
            {
                for (int index224 = 0; index224 < size225; index224++)
                {
                    ObjectA result226 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                    result223[index224] = result226;
                }
            }
            packet.kkk = result223;
            int size229 = buffer.ReadInt();
            var result227 = new List<int>(size229);
            if (size229 > 0)
            {
                for (int index228 = 0; index228 < size229; index228++)
                {
                    int result230 = buffer.ReadInt();
                    result227.Add(result230);
                }
            }
            packet.l = result227;
            int size233 = buffer.ReadInt();
            var result231 = new List<List<List<int>>>(size233);
            if (size233 > 0)
            {
                for (int index232 = 0; index232 < size233; index232++)
                {
                    int size236 = buffer.ReadInt();
                    var result234 = new List<List<int>>(size236);
                    if (size236 > 0)
                    {
                        for (int index235 = 0; index235 < size236; index235++)
                        {
                            int size239 = buffer.ReadInt();
                            var result237 = new List<int>(size239);
                            if (size239 > 0)
                            {
                                for (int index238 = 0; index238 < size239; index238++)
                                {
                                    int result240 = buffer.ReadInt();
                                    result237.Add(result240);
                                }
                            }
                            result234.Add(result237);
                        }
                    }
                    result231.Add(result234);
                }
            }
            packet.ll = result231;
            int size243 = buffer.ReadInt();
            var result241 = new List<List<ObjectA>>(size243);
            if (size243 > 0)
            {
                for (int index242 = 0; index242 < size243; index242++)
                {
                    int size246 = buffer.ReadInt();
                    var result244 = new List<ObjectA>(size246);
                    if (size246 > 0)
                    {
                        for (int index245 = 0; index245 < size246; index245++)
                        {
                            ObjectA result247 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                            result244.Add(result247);
                        }
                    }
                    result241.Add(result244);
                }
            }
            packet.lll = result241;
            int size250 = buffer.ReadInt();
            var result248 = new List<string>(size250);
            if (size250 > 0)
            {
                for (int index249 = 0; index249 < size250; index249++)
                {
                    string result251 = buffer.ReadString();
                    result248.Add(result251);
                }
            }
            packet.llll = result248;
            int size254 = buffer.ReadInt();
            var result252 = new List<Dictionary<int, string>>(size254);
            if (size254 > 0)
            {
                for (int index253 = 0; index253 < size254; index253++)
                {
                    int size256 = buffer.ReadInt();
                    var result255 = new Dictionary<int, string>(size256);
                    if (size256 > 0)
                    {
                        for (var index257 = 0; index257 < size256; index257++)
                        {
                            int result258 = buffer.ReadInt();
                            string result259 = buffer.ReadString();
                            result255[result258] = result259;
                        }
                    }
                    result252.Add(result255);
                }
            }
            packet.lllll = result252;
            int size261 = buffer.ReadInt();
            var result260 = new Dictionary<int, string>(size261);
            if (size261 > 0)
            {
                for (var index262 = 0; index262 < size261; index262++)
                {
                    int result263 = buffer.ReadInt();
                    string result264 = buffer.ReadString();
                    result260[result263] = result264;
                }
            }
            packet.m = result260;
            int size266 = buffer.ReadInt();
            var result265 = new Dictionary<int, ObjectA>(size266);
            if (size266 > 0)
            {
                for (var index267 = 0; index267 < size266; index267++)
                {
                    int result268 = buffer.ReadInt();
                    ObjectA result269 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                    result265[result268] = result269;
                }
            }
            packet.mm = result265;
            int size271 = buffer.ReadInt();
            var result270 = new Dictionary<ObjectA, List<int>>(size271);
            if (size271 > 0)
            {
                for (var index272 = 0; index272 < size271; index272++)
                {
                    ObjectA result273 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                    int size276 = buffer.ReadInt();
                    var result274 = new List<int>(size276);
                    if (size276 > 0)
                    {
                        for (int index275 = 0; index275 < size276; index275++)
                        {
                            int result277 = buffer.ReadInt();
                            result274.Add(result277);
                        }
                    }
                    result270[result273] = result274;
                }
            }
            packet.mmm = result270;
            int size279 = buffer.ReadInt();
            var result278 = new Dictionary<List<List<ObjectA>>, List<List<List<int>>>>(size279);
            if (size279 > 0)
            {
                for (var index280 = 0; index280 < size279; index280++)
                {
                    int size283 = buffer.ReadInt();
                    var result281 = new List<List<ObjectA>>(size283);
                    if (size283 > 0)
                    {
                        for (int index282 = 0; index282 < size283; index282++)
                        {
                            int size286 = buffer.ReadInt();
                            var result284 = new List<ObjectA>(size286);
                            if (size286 > 0)
                            {
                                for (int index285 = 0; index285 < size286; index285++)
                                {
                                    ObjectA result287 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                                    result284.Add(result287);
                                }
                            }
                            result281.Add(result284);
                        }
                    }
                    int size290 = buffer.ReadInt();
                    var result288 = new List<List<List<int>>>(size290);
                    if (size290 > 0)
                    {
                        for (int index289 = 0; index289 < size290; index289++)
                        {
                            int size293 = buffer.ReadInt();
                            var result291 = new List<List<int>>(size293);
                            if (size293 > 0)
                            {
                                for (int index292 = 0; index292 < size293; index292++)
                                {
                                    int size296 = buffer.ReadInt();
                                    var result294 = new List<int>(size296);
                                    if (size296 > 0)
                                    {
                                        for (int index295 = 0; index295 < size296; index295++)
                                        {
                                            int result297 = buffer.ReadInt();
                                            result294.Add(result297);
                                        }
                                    }
                                    result291.Add(result294);
                                }
                            }
                            result288.Add(result291);
                        }
                    }
                    result278[result281] = result288;
                }
            }
            packet.mmmm = result278;
            int size299 = buffer.ReadInt();
            var result298 = new Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>>(size299);
            if (size299 > 0)
            {
                for (var index300 = 0; index300 < size299; index300++)
                {
                    int size303 = buffer.ReadInt();
                    var result301 = new List<Dictionary<int, string>>(size303);
                    if (size303 > 0)
                    {
                        for (int index302 = 0; index302 < size303; index302++)
                        {
                            int size305 = buffer.ReadInt();
                            var result304 = new Dictionary<int, string>(size305);
                            if (size305 > 0)
                            {
                                for (var index306 = 0; index306 < size305; index306++)
                                {
                                    int result307 = buffer.ReadInt();
                                    string result308 = buffer.ReadString();
                                    result304[result307] = result308;
                                }
                            }
                            result301.Add(result304);
                        }
                    }
                    int size311 = buffer.ReadInt();
                    var result309 = new HashSet<Dictionary<int, string>>();
                    if (size311 > 0)
                    {
                        for (int index310 = 0; index310 < size311; index310++)
                        {
                            int size313 = buffer.ReadInt();
                            var result312 = new Dictionary<int, string>(size313);
                            if (size313 > 0)
                            {
                                for (var index314 = 0; index314 < size313; index314++)
                                {
                                    int result315 = buffer.ReadInt();
                                    string result316 = buffer.ReadString();
                                    result312[result315] = result316;
                                }
                            }
                            result309.Add(result312);
                        }
                    }
                    result298[result301] = result309;
                }
            }
            packet.mmmmm = result298;
            int size319 = buffer.ReadInt();
            var result317 = new HashSet<int>();
            if (size319 > 0)
            {
                for (int index318 = 0; index318 < size319; index318++)
                {
                    int result320 = buffer.ReadInt();
                    result317.Add(result320);
                }
            }
            packet.s = result317;
            int size323 = buffer.ReadInt();
            var result321 = new HashSet<HashSet<List<int>>>();
            if (size323 > 0)
            {
                for (int index322 = 0; index322 < size323; index322++)
                {
                    int size326 = buffer.ReadInt();
                    var result324 = new HashSet<List<int>>();
                    if (size326 > 0)
                    {
                        for (int index325 = 0; index325 < size326; index325++)
                        {
                            int size329 = buffer.ReadInt();
                            var result327 = new List<int>(size329);
                            if (size329 > 0)
                            {
                                for (int index328 = 0; index328 < size329; index328++)
                                {
                                    int result330 = buffer.ReadInt();
                                    result327.Add(result330);
                                }
                            }
                            result324.Add(result327);
                        }
                    }
                    result321.Add(result324);
                }
            }
            packet.ss = result321;
            int size333 = buffer.ReadInt();
            var result331 = new HashSet<HashSet<ObjectA>>();
            if (size333 > 0)
            {
                for (int index332 = 0; index332 < size333; index332++)
                {
                    int size336 = buffer.ReadInt();
                    var result334 = new HashSet<ObjectA>();
                    if (size336 > 0)
                    {
                        for (int index335 = 0; index335 < size336; index335++)
                        {
                            ObjectA result337 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                            result334.Add(result337);
                        }
                    }
                    result331.Add(result334);
                }
            }
            packet.sss = result331;
            int size340 = buffer.ReadInt();
            var result338 = new HashSet<string>();
            if (size340 > 0)
            {
                for (int index339 = 0; index339 < size340; index339++)
                {
                    string result341 = buffer.ReadString();
                    result338.Add(result341);
                }
            }
            packet.ssss = result338;
            int size344 = buffer.ReadInt();
            var result342 = new HashSet<Dictionary<int, string>>();
            if (size344 > 0)
            {
                for (int index343 = 0; index343 < size344; index343++)
                {
                    int size346 = buffer.ReadInt();
                    var result345 = new Dictionary<int, string>(size346);
                    if (size346 > 0)
                    {
                        for (var index347 = 0; index347 < size346; index347++)
                        {
                            int result348 = buffer.ReadInt();
                            string result349 = buffer.ReadString();
                            result345[result348] = result349;
                        }
                    }
                    result342.Add(result345);
                }
            }
            packet.sssss = result342;
            return packet;
        }
    }
}
