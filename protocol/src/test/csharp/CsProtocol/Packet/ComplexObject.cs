using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    // 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
    public class ComplexObject : IProtocol
    {
        // byte类型，最简单的整形
        public byte a;
        // byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
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
        // 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
        public int myCompatible;
        public ObjectA myObject;

        public static ComplexObject ValueOf(byte a, byte aa, byte[] aaa, byte[] aaaa, short b, short bb, short[] bbb, short[] bbbb, int c, int cc, int[] ccc, int[] cccc, long d, long dd, long[] ddd, long[] dddd, float e, float ee, float[] eee, float[] eeee, double f, double ff, double[] fff, double[] ffff, bool g, bool gg, bool[] ggg, bool[] gggg, char h, char hh, char[] hhh, char[] hhhh, string jj, string[] jjj, ObjectA kk, ObjectA[] kkk, List<int> l, List<List<List<int>>> ll, List<List<ObjectA>> lll, List<string> llll, List<Dictionary<int, string>> lllll, Dictionary<int, string> m, Dictionary<int, ObjectA> mm, Dictionary<ObjectA, List<int>> mmm, Dictionary<List<List<ObjectA>>, List<List<List<int>>>> mmmm, Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>> mmmmm, HashSet<int> s, HashSet<HashSet<List<int>>> ss, HashSet<HashSet<ObjectA>> sss, HashSet<string> ssss, HashSet<Dictionary<int, string>> sssss, int myCompatible, ObjectA myObject)
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
            packet.myCompatible = myCompatible;
            packet.myObject = myObject;
            return packet;
        }


        public short ProtocolId()
        {
            return 100;
        }
    }


    public class ComplexObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 100;
        }

        public void Write(ByteBuffer buffer, IProtocol packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            ComplexObject message = (ComplexObject) packet;
            buffer.WriteByte(message.a);
            buffer.WriteByte(message.aa);
            buffer.WriteByteArray(message.aaa);
            buffer.WriteByteArray(message.aaaa);
            buffer.WriteShort(message.b);
            buffer.WriteShort(message.bb);
            buffer.WriteShortArray(message.bbb);
            buffer.WriteShortArray(message.bbbb);
            buffer.WriteInt(message.c);
            buffer.WriteInt(message.cc);
            buffer.WriteIntArray(message.ccc);
            buffer.WriteIntArray(message.cccc);
            buffer.WriteLong(message.d);
            buffer.WriteLong(message.dd);
            buffer.WriteLongArray(message.ddd);
            buffer.WriteLongArray(message.dddd);
            buffer.WriteFloat(message.e);
            buffer.WriteFloat(message.ee);
            buffer.WriteFloatArray(message.eee);
            buffer.WriteFloatArray(message.eeee);
            buffer.WriteDouble(message.f);
            buffer.WriteDouble(message.ff);
            buffer.WriteDoubleArray(message.fff);
            buffer.WriteDoubleArray(message.ffff);
            buffer.WriteBool(message.g);
            buffer.WriteBool(message.gg);
            buffer.WriteBooleanArray(message.ggg);
            buffer.WriteBooleanArray(message.gggg);
            buffer.WriteChar(message.h);
            buffer.WriteChar(message.hh);
            buffer.WriteCharArray(message.hhh);
            buffer.WriteCharArray(message.hhhh);
            buffer.WriteString(message.jj);
            buffer.WriteStringArray(message.jjj);
            buffer.WritePacket(message.kk, 102);
            buffer.WritePacketArray<ObjectA>(message.kkk, 102);
            buffer.WriteIntList(message.l);
            if (message.ll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ll.Count);
                int length0 = message.ll.Count;
                for (int i1 = 0; i1 < length0; i1++)
                {
                    var element2 = message.ll[i1];
                    if (element2 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(element2.Count);
                        int length3 = element2.Count;
                        for (int i4 = 0; i4 < length3; i4++)
                        {
                            var element5 = element2[i4];
                            buffer.WriteIntList(element5);
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
                int length6 = message.lll.Count;
                for (int i7 = 0; i7 < length6; i7++)
                {
                    var element8 = message.lll[i7];
                    buffer.WritePacketList(element8, 102);
                }
            }
            buffer.WriteStringList(message.llll);
            if (message.lllll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.lllll.Count);
                int length9 = message.lllll.Count;
                for (int i10 = 0; i10 < length9; i10++)
                {
                    var element11 = message.lllll[i10];
                    buffer.WriteIntStringMap(element11);
                }
            }
            buffer.WriteIntStringMap(message.m);
            buffer.WriteIntPacketMap(message.mm, 102);
            if ((message.mmm == null) || (message.mmm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mmm.Count);
                foreach (var i12 in message.mmm)
                {
                    var keyElement13 = i12.Key;
                    var valueElement14 = i12.Value;
                    buffer.WritePacket(keyElement13, 102);
                    buffer.WriteIntList(valueElement14);
                }
            }
            if ((message.mmmm == null) || (message.mmmm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mmmm.Count);
                foreach (var i15 in message.mmmm)
                {
                    var keyElement16 = i15.Key;
                    var valueElement17 = i15.Value;
                    if (keyElement16 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(keyElement16.Count);
                        int length18 = keyElement16.Count;
                        for (int i19 = 0; i19 < length18; i19++)
                        {
                            var element20 = keyElement16[i19];
                            buffer.WritePacketList(element20, 102);
                        }
                    }
                    if (valueElement17 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(valueElement17.Count);
                        int length21 = valueElement17.Count;
                        for (int i22 = 0; i22 < length21; i22++)
                        {
                            var element23 = valueElement17[i22];
                            if (element23 == null)
                            {
                                buffer.WriteInt(0);
                            }
                            else
                            {
                                buffer.WriteInt(element23.Count);
                                int length24 = element23.Count;
                                for (int i25 = 0; i25 < length24; i25++)
                                {
                                    var element26 = element23[i25];
                                    buffer.WriteIntList(element26);
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
                foreach (var i27 in message.mmmmm)
                {
                    var keyElement28 = i27.Key;
                    var valueElement29 = i27.Value;
                    if (keyElement28 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(keyElement28.Count);
                        int length30 = keyElement28.Count;
                        for (int i31 = 0; i31 < length30; i31++)
                        {
                            var element32 = keyElement28[i31];
                            buffer.WriteIntStringMap(element32);
                        }
                    }
                    if (valueElement29 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(valueElement29.Count);
                        foreach (var i33 in valueElement29)
                        {
                            buffer.WriteIntStringMap(i33);
                        }
                    }
                }
            }
            buffer.WriteIntSet(message.s);
            if (message.ss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ss.Count);
                foreach (var i34 in message.ss)
                {
                    if (i34 == null)
                    {
                        buffer.WriteInt(0);
                    }
                    else
                    {
                        buffer.WriteInt(i34.Count);
                        foreach (var i35 in i34)
                        {
                            buffer.WriteIntList(i35);
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
                foreach (var i36 in message.sss)
                {
                    buffer.WritePacketSet(i36, 102);
                }
            }
            buffer.WriteStringSet(message.ssss);
            if (message.sssss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.sssss.Count);
                foreach (var i37 in message.sssss)
                {
                    buffer.WriteIntStringMap(i37);
                }
            }
            buffer.WriteInt(message.myCompatible);
            buffer.WritePacket(message.myObject, 102);
        }

        public IProtocol Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            ComplexObject packet = new ComplexObject();
            byte result38 = buffer.ReadByte();
            packet.a = result38;
            byte result39 = buffer.ReadByte();
            packet.aa = result39;
            var array40 = buffer.ReadByteArray();
            packet.aaa = array40;
            var array41 = buffer.ReadByteArray();
            packet.aaaa = array41;
            short result42 = buffer.ReadShort();
            packet.b = result42;
            short result43 = buffer.ReadShort();
            packet.bb = result43;
            var array44 = buffer.ReadShortArray();
            packet.bbb = array44;
            var array45 = buffer.ReadShortArray();
            packet.bbbb = array45;
            int result46 = buffer.ReadInt();
            packet.c = result46;
            int result47 = buffer.ReadInt();
            packet.cc = result47;
            var array48 = buffer.ReadIntArray();
            packet.ccc = array48;
            var array49 = buffer.ReadIntArray();
            packet.cccc = array49;
            long result50 = buffer.ReadLong();
            packet.d = result50;
            long result51 = buffer.ReadLong();
            packet.dd = result51;
            var array52 = buffer.ReadLongArray();
            packet.ddd = array52;
            var array53 = buffer.ReadLongArray();
            packet.dddd = array53;
            float result54 = buffer.ReadFloat();
            packet.e = result54;
            float result55 = buffer.ReadFloat();
            packet.ee = result55;
            var array56 = buffer.ReadFloatArray();
            packet.eee = array56;
            var array57 = buffer.ReadFloatArray();
            packet.eeee = array57;
            double result58 = buffer.ReadDouble();
            packet.f = result58;
            double result59 = buffer.ReadDouble();
            packet.ff = result59;
            var array60 = buffer.ReadDoubleArray();
            packet.fff = array60;
            var array61 = buffer.ReadDoubleArray();
            packet.ffff = array61;
            bool result62 = buffer.ReadBool();
            packet.g = result62;
            bool result63 = buffer.ReadBool();
            packet.gg = result63;
            var array64 = buffer.ReadBooleanArray();
            packet.ggg = array64;
            var array65 = buffer.ReadBooleanArray();
            packet.gggg = array65;
            char result66 = buffer.ReadChar();
            packet.h = result66;
            char result67 = buffer.ReadChar();
            packet.hh = result67;
            var array68 = buffer.ReadCharArray();
            packet.hhh = array68;
            var array69 = buffer.ReadCharArray();
            packet.hhhh = array69;
            string result70 = buffer.ReadString();
            packet.jj = result70;
            var array71 = buffer.ReadStringArray();
            packet.jjj = array71;
            ObjectA result72 = buffer.ReadPacket<ObjectA>(102);
            packet.kk = result72;
            var array73 = buffer.ReadPacketArray<ObjectA>(102);
            packet.kkk = array73;
            var list74 = buffer.ReadIntList();
            packet.l = list74;
            int size77 = buffer.ReadInt();
            var result75 = new List<List<List<int>>>(size77);
            if (size77 > 0)
            {
                for (int index76 = 0; index76 < size77; index76++)
                {
                    int size80 = buffer.ReadInt();
                    var result78 = new List<List<int>>(size80);
                    if (size80 > 0)
                    {
                        for (int index79 = 0; index79 < size80; index79++)
                        {
                            var list81 = buffer.ReadIntList();
                            result78.Add(list81);
                        }
                    }
                    result75.Add(result78);
                }
            }
            packet.ll = result75;
            int size84 = buffer.ReadInt();
            var result82 = new List<List<ObjectA>>(size84);
            if (size84 > 0)
            {
                for (int index83 = 0; index83 < size84; index83++)
                {
                    var list85 = buffer.ReadPacketList<ObjectA>(102);
                    result82.Add(list85);
                }
            }
            packet.lll = result82;
            var list86 = buffer.ReadStringList();
            packet.llll = list86;
            int size89 = buffer.ReadInt();
            var result87 = new List<Dictionary<int, string>>(size89);
            if (size89 > 0)
            {
                for (int index88 = 0; index88 < size89; index88++)
                {
                    var map90 = buffer.ReadIntStringMap();
                    result87.Add(map90);
                }
            }
            packet.lllll = result87;
            var map91 = buffer.ReadIntStringMap();
            packet.m = map91;
            var map92 = buffer.ReadIntPacketMap<ObjectA>(102);
            packet.mm = map92;
            int size94 = buffer.ReadInt();
            var result93 = new Dictionary<ObjectA, List<int>>(size94);
            if (size94 > 0)
            {
                for (var index95 = 0; index95 < size94; index95++)
                {
                    ObjectA result96 = buffer.ReadPacket<ObjectA>(102);
                    var list97 = buffer.ReadIntList();
                    result93[result96] = list97;
                }
            }
            packet.mmm = result93;
            int size99 = buffer.ReadInt();
            var result98 = new Dictionary<List<List<ObjectA>>, List<List<List<int>>>>(size99);
            if (size99 > 0)
            {
                for (var index100 = 0; index100 < size99; index100++)
                {
                    int size103 = buffer.ReadInt();
                    var result101 = new List<List<ObjectA>>(size103);
                    if (size103 > 0)
                    {
                        for (int index102 = 0; index102 < size103; index102++)
                        {
                            var list104 = buffer.ReadPacketList<ObjectA>(102);
                            result101.Add(list104);
                        }
                    }
                    int size107 = buffer.ReadInt();
                    var result105 = new List<List<List<int>>>(size107);
                    if (size107 > 0)
                    {
                        for (int index106 = 0; index106 < size107; index106++)
                        {
                            int size110 = buffer.ReadInt();
                            var result108 = new List<List<int>>(size110);
                            if (size110 > 0)
                            {
                                for (int index109 = 0; index109 < size110; index109++)
                                {
                                    var list111 = buffer.ReadIntList();
                                    result108.Add(list111);
                                }
                            }
                            result105.Add(result108);
                        }
                    }
                    result98[result101] = result105;
                }
            }
            packet.mmmm = result98;
            int size113 = buffer.ReadInt();
            var result112 = new Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>>(size113);
            if (size113 > 0)
            {
                for (var index114 = 0; index114 < size113; index114++)
                {
                    int size117 = buffer.ReadInt();
                    var result115 = new List<Dictionary<int, string>>(size117);
                    if (size117 > 0)
                    {
                        for (int index116 = 0; index116 < size117; index116++)
                        {
                            var map118 = buffer.ReadIntStringMap();
                            result115.Add(map118);
                        }
                    }
                    int size121 = buffer.ReadInt();
                    var result119 = new HashSet<Dictionary<int, string>>();
                    if (size121 > 0)
                    {
                        for (int index120 = 0; index120 < size121; index120++)
                        {
                            var map122 = buffer.ReadIntStringMap();
                            result119.Add(map122);
                        }
                    }
                    result112[result115] = result119;
                }
            }
            packet.mmmmm = result112;
            var set123 = buffer.ReadIntSet();
            packet.s = set123;
            int size126 = buffer.ReadInt();
            var result124 = new HashSet<HashSet<List<int>>>();
            if (size126 > 0)
            {
                for (int index125 = 0; index125 < size126; index125++)
                {
                    int size129 = buffer.ReadInt();
                    var result127 = new HashSet<List<int>>();
                    if (size129 > 0)
                    {
                        for (int index128 = 0; index128 < size129; index128++)
                        {
                            var list130 = buffer.ReadIntList();
                            result127.Add(list130);
                        }
                    }
                    result124.Add(result127);
                }
            }
            packet.ss = result124;
            int size133 = buffer.ReadInt();
            var result131 = new HashSet<HashSet<ObjectA>>();
            if (size133 > 0)
            {
                for (int index132 = 0; index132 < size133; index132++)
                {
                    var set134 = buffer.ReadPacketSet<ObjectA>(102);
                    result131.Add(set134);
                }
            }
            packet.sss = result131;
            var set135 = buffer.ReadStringSet();
            packet.ssss = set135;
            int size138 = buffer.ReadInt();
            var result136 = new HashSet<Dictionary<int, string>>();
            if (size138 > 0)
            {
                for (int index137 = 0; index137 < size138; index137++)
                {
                    var map139 = buffer.ReadIntStringMap();
                    result136.Add(map139);
                }
            }
            packet.sssss = result136;
            if (!buffer.IsReadable())
            {
                return packet;
            }
            int result140 = buffer.ReadInt();
            packet.myCompatible = result140;
            if (!buffer.IsReadable())
            {
                return packet;
            }
            ObjectA result141 = buffer.ReadPacket<ObjectA>(102);
            packet.myObject = result141;
            return packet;
        }
    }
}
