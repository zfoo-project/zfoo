using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    
    public class SimpleObject : IProtocol
    {
        public int c;
        public bool g;

        public static SimpleObject ValueOf(int c, bool g)
        {
            var packet = new SimpleObject();
            packet.c = c;
            packet.g = g;
            return packet;
        }


        public short ProtocolId()
        {
            return 104;
        }
    }


    public class SimpleObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 104;
        }

        public void Write(ByteBuffer buffer, IProtocol packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            SimpleObject message = (SimpleObject) packet;
            buffer.WriteInt(message.c);
            buffer.WriteBool(message.g);
        }

        public IProtocol Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            SimpleObject packet = new SimpleObject();
            int result0 = buffer.ReadInt();
            packet.c = result0;
            bool result1 = buffer.ReadBool();
            packet.g = result1;
            return packet;
        }
    }
}
