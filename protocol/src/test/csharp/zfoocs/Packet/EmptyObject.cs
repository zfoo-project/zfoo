using System;
using System.Collections.Generic;
namespace zfoocs
{
    
    public class EmptyObject
    {
        
    }

    public class EmptyObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 0;
        }
    
        public void Write(ByteBuffer buffer, object packet)
        {
            if (packet == null)
            {
                buffer.WriteInt(0);
                return;
            }
            EmptyObject message = (EmptyObject) packet;
            buffer.WriteInt(-1);
        }
    
        public object Read(ByteBuffer buffer)
        {
            int length = buffer.ReadInt();
            if (length == 0)
            {
                return null;
            }
            int beforeReadIndex = buffer.ReadOffset();
            EmptyObject packet = new EmptyObject();
            
            if (length > 0)
            {
                buffer.SetReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    }
}