using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    // @author jaysunxiao
    // @version 1.0
    // @since 2017 10.12 15:39
    public class ObjectA : IPacket
    {
        public int a;
        public Dictionary<int, string> m;
        public ObjectB objectB;

        public static ObjectA ValueOf(int a, Dictionary<int, string> m, ObjectB objectB)
        {
            var packet = new ObjectA();
            packet.a = a;
            packet.m = m;
            packet.objectB = objectB;
            return packet;
        }


        public short ProtocolId()
        {
            return 1116;
        }
    }


    public class ObjectARegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 1116;
        }

        public void Write(ByteBuffer buffer, IPacket packet)
        {
            if (packet == null)
            {
                buffer.WriteBool(false);
                return;
            }
            buffer.WriteBool(true);
            ObjectA message = (ObjectA) packet;
            buffer.WriteInt(message.a);
            if ((message.m == null) || (message.m.Count == 0))
            {
                buffer.WriteInt(0);
            }
            else
            {
                buffer.WriteInt(message.m.Count);
                foreach (var i0 in message.m)
                {
                    var keyElement1 = i0.Key;
                    var valueElement2 = i0.Value;
                    buffer.WriteInt(keyElement1);
                    buffer.WriteString(valueElement2);
                }
            }
            ProtocolManager.GetProtocol(1117).Write(buffer, message.objectB);
        }

        public IPacket Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            ObjectA packet = new ObjectA();
            int result3 = buffer.ReadInt();
            packet.a = result3;
            int size5 = buffer.ReadInt();
            var result4 = new Dictionary<int, string>(size5);
            if (size5 > 0)
            {
                for (var index6 = 0; index6 < size5; index6++)
                {
                    int result7 = buffer.ReadInt();
                    string result8 = buffer.ReadString();
                    result4[result7] = result8;
                }
            }
            packet.m = result4;
            ObjectB result9 = (ObjectB) ProtocolManager.GetProtocol(1117).Read(buffer);
            packet.objectB = result9;
            return packet;
        }
    }
}
