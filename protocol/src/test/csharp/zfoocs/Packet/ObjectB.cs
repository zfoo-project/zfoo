using System;
using System.Collections.Generic;
namespace zfoocs
{
    
    public class ObjectB
    {
        public bool flag;
        public int innerCompatibleValue;
    }

    public class ObjectBRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 103;
        }
    
        public void Write(ByteBuffer buffer, object packet)
        {
            if (packet == null)
            {
                buffer.WriteInt(0);
                return;
            }
            ObjectB message = (ObjectB) packet;
            int beforeWriteIndex = buffer.WriteOffset();
            buffer.WriteInt(4);
            buffer.WriteBool(message.flag);
            buffer.WriteInt(message.innerCompatibleValue);
            buffer.AdjustPadding(4, beforeWriteIndex);
        }
    
        public object Read(ByteBuffer buffer)
        {
            int length = buffer.ReadInt();
            if (length == 0)
            {
                return null;
            }
            int beforeReadIndex = buffer.ReadOffset();
            ObjectB packet = new ObjectB();
            bool result0 = buffer.ReadBool();
            packet.flag = result0;
            if (buffer.CompatibleRead(beforeReadIndex, length)) {
                int result1 = buffer.ReadInt();
                packet.innerCompatibleValue = result1;
            }
            if (length > 0)
            {
                buffer.SetReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    }
}