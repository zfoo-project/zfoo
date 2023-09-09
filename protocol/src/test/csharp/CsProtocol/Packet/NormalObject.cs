using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    
    public class NormalObject : IProtocol
    {
        public byte a;
        public byte[] aaa;
        public short b;
        public int c;
        public long d;
        public float e;
        public double f;
        public bool g;
        public string jj;
        public ObjectA kk;
        public List<int> l;
        public List<long> ll;
        public List<ObjectA> lll;
        public List<string> llll;
        public Dictionary<int, string> m;
        public Dictionary<int, ObjectA> mm;
        public HashSet<int> s;
        public HashSet<string> ssss;

        public static NormalObject ValueOf(byte a, byte[] aaa, short b, int c, long d, float e, double f, bool g, string jj, ObjectA kk, List<int> l, List<long> ll, List<ObjectA> lll, List<string> llll, Dictionary<int, string> m, Dictionary<int, ObjectA> mm, HashSet<int> s, HashSet<string> ssss)
        {
            var packet = new NormalObject();
            packet.a = a;
            packet.aaa = aaa;
            packet.b = b;
            packet.c = c;
            packet.d = d;
            packet.e = e;
            packet.f = f;
            packet.g = g;
            packet.jj = jj;
            packet.kk = kk;
            packet.l = l;
            packet.ll = ll;
            packet.lll = lll;
            packet.llll = llll;
            packet.m = m;
            packet.mm = mm;
            packet.s = s;
            packet.ssss = ssss;
            return packet;
        }


        public short ProtocolId()
        {
            return 101;
        }
    }


    public class NormalObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 101;
        }

        public void Write(ByteBuffer buffer, IProtocol packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            NormalObject message = (NormalObject) packet;
            buffer.WriteByte(message.a);
            buffer.WriteByteArray(message.aaa);
            buffer.WriteShort(message.b);
            buffer.WriteInt(message.c);
            buffer.WriteLong(message.d);
            buffer.WriteFloat(message.e);
            buffer.WriteDouble(message.f);
            buffer.WriteBool(message.g);
            buffer.WriteString(message.jj);
            buffer.WritePacket(message.kk, 102);
            buffer.WriteIntList(message.l);
            buffer.WriteLongList(message.ll);
            buffer.WritePacketList(message.lll, 102);
            buffer.WriteStringList(message.llll);
            buffer.WriteIntStringMap(message.m);
            buffer.WriteIntPacketMap(message.mm, 102);
            buffer.WriteIntSet(message.s);
            buffer.WriteStringSet(message.ssss);
        }

        public IProtocol Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            NormalObject packet = new NormalObject();
            byte result0 = buffer.ReadByte();
            packet.a = result0;
            var array1 = buffer.ReadByteArray();
            packet.aaa = array1;
            short result2 = buffer.ReadShort();
            packet.b = result2;
            int result3 = buffer.ReadInt();
            packet.c = result3;
            long result4 = buffer.ReadLong();
            packet.d = result4;
            float result5 = buffer.ReadFloat();
            packet.e = result5;
            double result6 = buffer.ReadDouble();
            packet.f = result6;
            bool result7 = buffer.ReadBool();
            packet.g = result7;
            string result8 = buffer.ReadString();
            packet.jj = result8;
            ObjectA result9 = buffer.ReadPacket<ObjectA>(102);
            packet.kk = result9;
            var list10 = buffer.ReadIntList();
            packet.l = list10;
            var list11 = buffer.ReadLongList();
            packet.ll = list11;
            var list12 = buffer.ReadPacketList<ObjectA>(102);
            packet.lll = list12;
            var list13 = buffer.ReadStringList();
            packet.llll = list13;
            var map14 = buffer.ReadIntStringMap();
            packet.m = map14;
            var map15 = buffer.ReadIntPacketMap<ObjectA>(102);
            packet.mm = map15;
            var set16 = buffer.ReadIntSet();
            packet.s = set16;
            var set17 = buffer.ReadStringSet();
            packet.ssss = set17;
            return packet;
        }
    }
}
