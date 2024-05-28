using System;
using System.Collections.Generic;
namespace zfoocs
{
    // 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
    public class ComplexObject
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
        // 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
        public int myCompatible;
        public ObjectA myObject;
    }

    public class ComplexObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 100;
        }
    
        public void Write(ByteBuffer buffer, object packet)
        {
            if (packet == null)
            {
                buffer.WriteInt(0);
                return;
            }
            ComplexObject message = (ComplexObject) packet;
            int beforeWriteIndex = buffer.WriteOffset();
            buffer.WriteInt(36962);
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
            buffer.AdjustPadding(36962, beforeWriteIndex);
        }
    
        public object Read(ByteBuffer buffer)
        {
            int length = buffer.ReadInt();
            if (length == 0)
            {
                return null;
            }
            int beforeReadIndex = buffer.ReadOffset();
            ComplexObject packet = new ComplexObject();
            byte result0 = buffer.ReadByte();
            packet.a = result0;
            byte result1 = buffer.ReadByte();
            packet.aa = result1;
            var array2 = buffer.ReadByteArray();
            packet.aaa = array2;
            var array3 = buffer.ReadByteArray();
            packet.aaaa = array3;
            short result4 = buffer.ReadShort();
            packet.b = result4;
            short result5 = buffer.ReadShort();
            packet.bb = result5;
            var array6 = buffer.ReadShortArray();
            packet.bbb = array6;
            var array7 = buffer.ReadShortArray();
            packet.bbbb = array7;
            int result8 = buffer.ReadInt();
            packet.c = result8;
            int result9 = buffer.ReadInt();
            packet.cc = result9;
            var array10 = buffer.ReadIntArray();
            packet.ccc = array10;
            var array11 = buffer.ReadIntArray();
            packet.cccc = array11;
            long result12 = buffer.ReadLong();
            packet.d = result12;
            long result13 = buffer.ReadLong();
            packet.dd = result13;
            var array14 = buffer.ReadLongArray();
            packet.ddd = array14;
            var array15 = buffer.ReadLongArray();
            packet.dddd = array15;
            float result16 = buffer.ReadFloat();
            packet.e = result16;
            float result17 = buffer.ReadFloat();
            packet.ee = result17;
            var array18 = buffer.ReadFloatArray();
            packet.eee = array18;
            var array19 = buffer.ReadFloatArray();
            packet.eeee = array19;
            double result20 = buffer.ReadDouble();
            packet.f = result20;
            double result21 = buffer.ReadDouble();
            packet.ff = result21;
            var array22 = buffer.ReadDoubleArray();
            packet.fff = array22;
            var array23 = buffer.ReadDoubleArray();
            packet.ffff = array23;
            bool result24 = buffer.ReadBool();
            packet.g = result24;
            bool result25 = buffer.ReadBool();
            packet.gg = result25;
            var array26 = buffer.ReadBooleanArray();
            packet.ggg = array26;
            var array27 = buffer.ReadBooleanArray();
            packet.gggg = array27;
            string result28 = buffer.ReadString();
            packet.jj = result28;
            var array29 = buffer.ReadStringArray();
            packet.jjj = array29;
            ObjectA result30 = buffer.ReadPacket<ObjectA>(102);
            packet.kk = result30;
            var array31 = buffer.ReadPacketArray<ObjectA>(102);
            packet.kkk = array31;
            var list32 = buffer.ReadIntList();
            packet.l = list32;
            int size35 = buffer.ReadInt();
            var result33 = new List<List<List<int>>>(size35);
            if (size35 > 0)
            {
                for (int index34 = 0; index34 < size35; index34++)
                {
                    int size38 = buffer.ReadInt();
                    var result36 = new List<List<int>>(size38);
                    if (size38 > 0)
                    {
                        for (int index37 = 0; index37 < size38; index37++)
                        {
                            var list39 = buffer.ReadIntList();
                            result36.Add(list39);
                        }
                    }
                    result33.Add(result36);
                }
            }
            packet.ll = result33;
            int size42 = buffer.ReadInt();
            var result40 = new List<List<ObjectA>>(size42);
            if (size42 > 0)
            {
                for (int index41 = 0; index41 < size42; index41++)
                {
                    var list43 = buffer.ReadPacketList<ObjectA>(102);
                    result40.Add(list43);
                }
            }
            packet.lll = result40;
            var list44 = buffer.ReadStringList();
            packet.llll = list44;
            int size47 = buffer.ReadInt();
            var result45 = new List<Dictionary<int, string>>(size47);
            if (size47 > 0)
            {
                for (int index46 = 0; index46 < size47; index46++)
                {
                    var map48 = buffer.ReadIntStringMap();
                    result45.Add(map48);
                }
            }
            packet.lllll = result45;
            var map49 = buffer.ReadIntStringMap();
            packet.m = map49;
            var map50 = buffer.ReadIntPacketMap<ObjectA>(102);
            packet.mm = map50;
            int size52 = buffer.ReadInt();
            var result51 = new Dictionary<ObjectA, List<int>>(size52);
            if (size52 > 0)
            {
                for (var index53 = 0; index53 < size52; index53++)
                {
                    ObjectA result54 = buffer.ReadPacket<ObjectA>(102);
                    var list55 = buffer.ReadIntList();
                    result51[result54] = list55;
                }
            }
            packet.mmm = result51;
            int size57 = buffer.ReadInt();
            var result56 = new Dictionary<List<List<ObjectA>>, List<List<List<int>>>>(size57);
            if (size57 > 0)
            {
                for (var index58 = 0; index58 < size57; index58++)
                {
                    int size61 = buffer.ReadInt();
                    var result59 = new List<List<ObjectA>>(size61);
                    if (size61 > 0)
                    {
                        for (int index60 = 0; index60 < size61; index60++)
                        {
                            var list62 = buffer.ReadPacketList<ObjectA>(102);
                            result59.Add(list62);
                        }
                    }
                    int size65 = buffer.ReadInt();
                    var result63 = new List<List<List<int>>>(size65);
                    if (size65 > 0)
                    {
                        for (int index64 = 0; index64 < size65; index64++)
                        {
                            int size68 = buffer.ReadInt();
                            var result66 = new List<List<int>>(size68);
                            if (size68 > 0)
                            {
                                for (int index67 = 0; index67 < size68; index67++)
                                {
                                    var list69 = buffer.ReadIntList();
                                    result66.Add(list69);
                                }
                            }
                            result63.Add(result66);
                        }
                    }
                    result56[result59] = result63;
                }
            }
            packet.mmmm = result56;
            int size71 = buffer.ReadInt();
            var result70 = new Dictionary<List<Dictionary<int, string>>, HashSet<Dictionary<int, string>>>(size71);
            if (size71 > 0)
            {
                for (var index72 = 0; index72 < size71; index72++)
                {
                    int size75 = buffer.ReadInt();
                    var result73 = new List<Dictionary<int, string>>(size75);
                    if (size75 > 0)
                    {
                        for (int index74 = 0; index74 < size75; index74++)
                        {
                            var map76 = buffer.ReadIntStringMap();
                            result73.Add(map76);
                        }
                    }
                    int size79 = buffer.ReadInt();
                    var result77 = new HashSet<Dictionary<int, string>>();
                    if (size79 > 0)
                    {
                        for (int index78 = 0; index78 < size79; index78++)
                        {
                            var map80 = buffer.ReadIntStringMap();
                            result77.Add(map80);
                        }
                    }
                    result70[result73] = result77;
                }
            }
            packet.mmmmm = result70;
            var set81 = buffer.ReadIntSet();
            packet.s = set81;
            int size84 = buffer.ReadInt();
            var result82 = new HashSet<HashSet<List<int>>>();
            if (size84 > 0)
            {
                for (int index83 = 0; index83 < size84; index83++)
                {
                    int size87 = buffer.ReadInt();
                    var result85 = new HashSet<List<int>>();
                    if (size87 > 0)
                    {
                        for (int index86 = 0; index86 < size87; index86++)
                        {
                            var list88 = buffer.ReadIntList();
                            result85.Add(list88);
                        }
                    }
                    result82.Add(result85);
                }
            }
            packet.ss = result82;
            int size91 = buffer.ReadInt();
            var result89 = new HashSet<HashSet<ObjectA>>();
            if (size91 > 0)
            {
                for (int index90 = 0; index90 < size91; index90++)
                {
                    var set92 = buffer.ReadPacketSet<ObjectA>(102);
                    result89.Add(set92);
                }
            }
            packet.sss = result89;
            var set93 = buffer.ReadStringSet();
            packet.ssss = set93;
            int size96 = buffer.ReadInt();
            var result94 = new HashSet<Dictionary<int, string>>();
            if (size96 > 0)
            {
                for (int index95 = 0; index95 < size96; index95++)
                {
                    var map97 = buffer.ReadIntStringMap();
                    result94.Add(map97);
                }
            }
            packet.sssss = result94;
            if (buffer.CompatibleRead(beforeReadIndex, length)) {
                int result98 = buffer.ReadInt();
                packet.myCompatible = result98;
            }
            if (buffer.CompatibleRead(beforeReadIndex, length)) {
                ObjectA result99 = buffer.ReadPacket<ObjectA>(102);
                packet.myObject = result99;
            }
            if (length > 0)
            {
                buffer.SetReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    }
}