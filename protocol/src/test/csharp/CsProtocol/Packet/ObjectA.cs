using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    
    public class ObjectA : IProtocol
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
            return 102;
        }
    }


    public class ObjectARegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 102;
        }

        public void Write(ByteBuffer buffer, IProtocol packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            ObjectA message = (ObjectA) packet;
            buffer.WriteInt(message.a);
            buffer.WriteIntStringMap(message.m);
            buffer.WritePacket(message.objectB, 103);
        }

        public IProtocol Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            ObjectA packet = new ObjectA();
            int result0 = buffer.ReadInt();
            packet.a = result0;
            var map1 = buffer.ReadIntStringMap();
            packet.m = map1;
            ObjectB result2 = buffer.ReadPacket<ObjectB>(103);
            packet.objectB = result2;
            return packet;
        }
    }
}
