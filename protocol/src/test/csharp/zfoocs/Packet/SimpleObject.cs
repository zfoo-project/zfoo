using System;
using System.Collections.Generic;
namespace zfoocs
{
    
    public class SimpleObject
    {
        public int c;
        public bool g;
    }

    public class SimpleObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 104;
        }
    
        public void Write(ByteBuffer buffer, object packet)
        {
            if (packet == null)
            {
                buffer.WriteInt(0);
                return;
            }
            SimpleObject message = (SimpleObject) packet;
            buffer.WriteInt(-1);
            buffer.WriteInt(message.c);
            buffer.WriteBool(message.g);
        }
    
        public object Read(ByteBuffer buffer)
        {
            int length = buffer.ReadInt();
            if (length == 0)
            {
                return null;
            }
            int beforeReadIndex = buffer.ReadOffset();
            SimpleObject packet = new SimpleObject();
            int result0 = buffer.ReadInt();
            packet.c = result0;
            bool result1 = buffer.ReadBool();
            packet.g = result1;
            if (length > 0)
            {
                buffer.SetReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    }
}