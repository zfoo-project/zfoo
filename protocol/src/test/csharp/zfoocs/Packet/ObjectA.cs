using System;
using System.Collections.Generic;
namespace zfoocs
{
    
    public class ObjectA
    {
        public int a;
        public Dictionary<int, string> m;
        public ObjectB objectB;
        public int innerCompatibleValue;
    }

    public class ObjectARegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 102;
        }
    
        public void Write(ByteBuffer buffer, object packet)
        {
            if (packet == null)
            {
                buffer.WriteInt(0);
                return;
            }
            ObjectA message = (ObjectA) packet;
            int beforeWriteIndex = buffer.WriteOffset();
            buffer.WriteInt(201);
            buffer.WriteInt(message.a);
            buffer.WriteIntStringMap(message.m);
            buffer.WritePacket(message.objectB, 103);
            buffer.WriteInt(message.innerCompatibleValue);
            buffer.AdjustPadding(201, beforeWriteIndex);
        }
    
        public object Read(ByteBuffer buffer)
        {
            int length = buffer.ReadInt();
            if (length == 0)
            {
                return null;
            }
            int beforeReadIndex = buffer.ReadOffset();
            ObjectA packet = new ObjectA();
            int result0 = buffer.ReadInt();
            packet.a = result0;
            var map1 = buffer.ReadIntStringMap();
            packet.m = map1;
            ObjectB result2 = buffer.ReadPacket<ObjectB>(103);
            packet.objectB = result2;
            if (buffer.CompatibleRead(beforeReadIndex, length)) {
                int result3 = buffer.ReadInt();
                packet.innerCompatibleValue = result3;
            }
            if (length > 0)
            {
                buffer.SetReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    }
}