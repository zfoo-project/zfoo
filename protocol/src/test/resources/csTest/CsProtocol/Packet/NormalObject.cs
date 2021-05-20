using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    // @author jaysunxiao
    // @version 1.0
    // @since 2021-02-07 17:18
    public class NormalObject : IPacket
    {
        public byte a;
        public byte[] aaa;
        public short b;
        public short[] bbb;
        public int c;
        public int[] ccc;
        public long d;
        public long[] ddd;
        public float e;
        public float[] eee;
        public double f;
        public double[] fff;
        public bool g;
        public bool[] ggg;
        public char h;
        public char[] hhh;
        public string jj;
        public string[] jjj;
        public ObjectA kk;
        public ObjectA[] kkk;
        public List<int> l;
        public List<string> llll;
        public Dictionary<int, string> m;
        public Dictionary<int, ObjectA> mm;
        public HashSet<int> s;
        public HashSet<string> ssss;

        public static NormalObject ValueOf(byte a, byte[] aaa, short b, short[] bbb, int c, int[] ccc, long d, long[] ddd, float e, float[] eee, double f, double[] fff, bool g, bool[] ggg, char h, char[] hhh, string jj, string[] jjj, ObjectA kk, ObjectA[] kkk, List<int> l, List<string> llll, Dictionary<int, string> m, Dictionary<int, ObjectA> mm, HashSet<int> s, HashSet<string> ssss)
        {
            var packet = new NormalObject();
            packet.a = a;
            packet.aaa = aaa;
            packet.b = b;
            packet.bbb = bbb;
            packet.c = c;
            packet.ccc = ccc;
            packet.d = d;
            packet.ddd = ddd;
            packet.e = e;
            packet.eee = eee;
            packet.f = f;
            packet.fff = fff;
            packet.g = g;
            packet.ggg = ggg;
            packet.h = h;
            packet.hhh = hhh;
            packet.jj = jj;
            packet.jjj = jjj;
            packet.kk = kk;
            packet.kkk = kkk;
            packet.l = l;
            packet.llll = llll;
            packet.m = m;
            packet.mm = mm;
            packet.s = s;
            packet.ssss = ssss;
            return packet;
        }


        public short ProtocolId()
        {
            return 1161;
        }
    }


    public class NormalObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 1161;
        }

        public void Write(ByteBuffer buffer, IPacket packet)
        {
            if (packet == null)
            {
                buffer.WriteBool(false);
                return;
            }
            buffer.WriteBool(true);
            NormalObject message = (NormalObject) packet;
            buffer.WriteByte(message.a);
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
            buffer.WriteShort(message.b);
            if ((message.bbb == null) || (message.bbb.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.bbb.Length);
                int length3 = message.bbb.Length;
                for (int i4 = 0; i4 < length3; i4++)
                {
                    short element5 = message.bbb[i4];
                    buffer.WriteShort(element5);
                }
            }
            buffer.WriteInt(message.c);
            if ((message.ccc == null) || (message.ccc.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ccc.Length);
                int length6 = message.ccc.Length;
                for (int i7 = 0; i7 < length6; i7++)
                {
                    int element8 = message.ccc[i7];
                    buffer.WriteInt(element8);
                }
            }
            buffer.WriteLong(message.d);
            if ((message.ddd == null) || (message.ddd.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ddd.Length);
                int length9 = message.ddd.Length;
                for (int i10 = 0; i10 < length9; i10++)
                {
                    long element11 = message.ddd[i10];
                    buffer.WriteLong(element11);
                }
            }
            buffer.WriteFloat(message.e);
            if ((message.eee == null) || (message.eee.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.eee.Length);
                int length12 = message.eee.Length;
                for (int i13 = 0; i13 < length12; i13++)
                {
                    float element14 = message.eee[i13];
                    buffer.WriteFloat(element14);
                }
            }
            buffer.WriteDouble(message.f);
            if ((message.fff == null) || (message.fff.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.fff.Length);
                int length15 = message.fff.Length;
                for (int i16 = 0; i16 < length15; i16++)
                {
                    double element17 = message.fff[i16];
                    buffer.WriteDouble(element17);
                }
            }
            buffer.WriteBool(message.g);
            if ((message.ggg == null) || (message.ggg.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ggg.Length);
                int length18 = message.ggg.Length;
                for (int i19 = 0; i19 < length18; i19++)
                {
                    bool element20 = message.ggg[i19];
                    buffer.WriteBool(element20);
                }
            }
            buffer.WriteChar(message.h);
            if ((message.hhh == null) || (message.hhh.Length == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.hhh.Length);
                int length21 = message.hhh.Length;
                for (int i22 = 0; i22 < length21; i22++)
                {
                    char element23 = message.hhh[i22];
                    buffer.WriteChar(element23);
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
                int length24 = message.jjj.Length;
                for (int i25 = 0; i25 < length24; i25++)
                {
                    string element26 = message.jjj[i25];
                    buffer.WriteString(element26);
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
                int length27 = message.kkk.Length;
                for (int i28 = 0; i28 < length27; i28++)
                {
                    ObjectA element29 = message.kkk[i28];
                    ProtocolManager.GetProtocol(1116).Write(buffer, element29);
                }
            }
            if (message.l == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.l.Count);
                int length30 = message.l.Count;
                for (int i31 = 0; i31 < length30; i31++)
                {
                    var element32 = message.l[i31];
                    buffer.WriteInt(element32);
                }
            }
            if (message.llll == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.llll.Count);
                int length33 = message.llll.Count;
                for (int i34 = 0; i34 < length33; i34++)
                {
                    var element35 = message.llll[i34];
                    buffer.WriteString(element35);
                }
            }
            if ((message.m == null) || (message.m.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.m.Count);
                foreach (var i36 in message.m)
                {
                    var keyElement37 = i36.Key;
                    var valueElement38 = i36.Value;
                    buffer.WriteInt(keyElement37);
                    buffer.WriteString(valueElement38);
                }
            }
            if ((message.mm == null) || (message.mm.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.mm.Count);
                foreach (var i39 in message.mm)
                {
                    var keyElement40 = i39.Key;
                    var valueElement41 = i39.Value;
                    buffer.WriteInt(keyElement40);
                    ProtocolManager.GetProtocol(1116).Write(buffer, valueElement41);
                }
            }
            if (message.s == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.s.Count);
                foreach (var i42 in message.s)
                {
                    buffer.WriteInt(i42);
                }
            }
            if (message.ssss == null)
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.ssss.Count);
                foreach (var i43 in message.ssss)
                {
                    buffer.WriteString(i43);
                }
            }
        }

        public IPacket Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            NormalObject packet = new NormalObject();
            byte result44 = buffer.ReadByte();
            packet.a = result44;
            int size47 = buffer.ReadInt();
            byte[] result45 = new byte[size47];
            if (size47 > 0)
            {
                for (int index46 = 0; index46 < size47; index46++)
                {
                    byte result48 = buffer.ReadByte();
                    result45[index46] = result48;
                }
            }
            packet.aaa = result45;
            short result49 = buffer.ReadShort();
            packet.b = result49;
            int size52 = buffer.ReadInt();
            short[] result50 = new short[size52];
            if (size52 > 0)
            {
                for (int index51 = 0; index51 < size52; index51++)
                {
                    short result53 = buffer.ReadShort();
                    result50[index51] = result53;
                }
            }
            packet.bbb = result50;
            int result54 = buffer.ReadInt();
            packet.c = result54;
            int size57 = buffer.ReadInt();
            int[] result55 = new int[size57];
            if (size57 > 0)
            {
                for (int index56 = 0; index56 < size57; index56++)
                {
                    int result58 = buffer.ReadInt();
                    result55[index56] = result58;
                }
            }
            packet.ccc = result55;
            long result59 = buffer.ReadLong();
            packet.d = result59;
            int size62 = buffer.ReadInt();
            long[] result60 = new long[size62];
            if (size62 > 0)
            {
                for (int index61 = 0; index61 < size62; index61++)
                {
                    long result63 = buffer.ReadLong();
                    result60[index61] = result63;
                }
            }
            packet.ddd = result60;
            float result64 = buffer.ReadFloat();
            packet.e = result64;
            int size67 = buffer.ReadInt();
            float[] result65 = new float[size67];
            if (size67 > 0)
            {
                for (int index66 = 0; index66 < size67; index66++)
                {
                    float result68 = buffer.ReadFloat();
                    result65[index66] = result68;
                }
            }
            packet.eee = result65;
            double result69 = buffer.ReadDouble();
            packet.f = result69;
            int size72 = buffer.ReadInt();
            double[] result70 = new double[size72];
            if (size72 > 0)
            {
                for (int index71 = 0; index71 < size72; index71++)
                {
                    double result73 = buffer.ReadDouble();
                    result70[index71] = result73;
                }
            }
            packet.fff = result70;
            bool result74 = buffer.ReadBool();
            packet.g = result74;
            int size77 = buffer.ReadInt();
            bool[] result75 = new bool[size77];
            if (size77 > 0)
            {
                for (int index76 = 0; index76 < size77; index76++)
                {
                    bool result78 = buffer.ReadBool();
                    result75[index76] = result78;
                }
            }
            packet.ggg = result75;
            char result79 = buffer.ReadChar();
            packet.h = result79;
            int size82 = buffer.ReadInt();
            char[] result80 = new char[size82];
            if (size82 > 0)
            {
                for (int index81 = 0; index81 < size82; index81++)
                {
                    char result83 = buffer.ReadChar();
                    result80[index81] = result83;
                }
            }
            packet.hhh = result80;
            string result84 = buffer.ReadString();
            packet.jj = result84;
            int size87 = buffer.ReadInt();
            string[] result85 = new string[size87];
            if (size87 > 0)
            {
                for (int index86 = 0; index86 < size87; index86++)
                {
                    string result88 = buffer.ReadString();
                    result85[index86] = result88;
                }
            }
            packet.jjj = result85;
            ObjectA result89 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
            packet.kk = result89;
            int size92 = buffer.ReadInt();
            ObjectA[] result90 = new ObjectA[size92];
            if (size92 > 0)
            {
                for (int index91 = 0; index91 < size92; index91++)
                {
                    ObjectA result93 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                    result90[index91] = result93;
                }
            }
            packet.kkk = result90;
            int size96 = buffer.ReadInt();
            var result94 = new List<int>(size96);
            if (size96 > 0)
            {
                for (int index95 = 0; index95 < size96; index95++)
                {
                    int result97 = buffer.ReadInt();
                    result94.Add(result97);
                }
            }
            packet.l = result94;
            int size100 = buffer.ReadInt();
            var result98 = new List<string>(size100);
            if (size100 > 0)
            {
                for (int index99 = 0; index99 < size100; index99++)
                {
                    string result101 = buffer.ReadString();
                    result98.Add(result101);
                }
            }
            packet.llll = result98;
            int size103 = buffer.ReadInt();
            var result102 = new Dictionary<int, string>(size103);
            if (size103 > 0)
            {
                for (var index104 = 0; index104 < size103; index104++)
                {
                    int result105 = buffer.ReadInt();
                    string result106 = buffer.ReadString();
                    result102[result105] = result106;
                }
            }
            packet.m = result102;
            int size108 = buffer.ReadInt();
            var result107 = new Dictionary<int, ObjectA>(size108);
            if (size108 > 0)
            {
                for (var index109 = 0; index109 < size108; index109++)
                {
                    int result110 = buffer.ReadInt();
                    ObjectA result111 = (ObjectA) ProtocolManager.GetProtocol(1116).Read(buffer);
                    result107[result110] = result111;
                }
            }
            packet.mm = result107;
            int size114 = buffer.ReadInt();
            var result112 = new HashSet<int>();
            if (size114 > 0)
            {
                for (int index113 = 0; index113 < size114; index113++)
                {
                    int result115 = buffer.ReadInt();
                    result112.Add(result115);
                }
            }
            packet.s = result112;
            int size118 = buffer.ReadInt();
            var result116 = new HashSet<string>();
            if (size118 > 0)
            {
                for (int index117 = 0; index117 < size118; index117++)
                {
                    string result119 = buffer.ReadString();
                    result116.Add(result119);
                }
            }
            packet.ssss = result116;
            return packet;
        }
    }
}
