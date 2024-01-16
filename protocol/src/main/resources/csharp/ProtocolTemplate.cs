using System;
using System.Collections.Generic;

namespace zfoocs
{
    {}
    public class {}
    {
        {}
    }


    public class {}Registration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return {};
        }

        public void Write(ByteBuffer buffer, object packet)
        {
            if (packet == null)
            {
                buffer.WriteInt(0);
                return;
            }
            {} message = ({}) packet;
            {}
        }

        public object Read(ByteBuffer buffer)
        {
            int length = buffer.ReadInt();
            if (length == 0)
            {
                return null;
            }
            int beforeReadIndex = buffer.ReadOffset();
            {} packet = new {}();
            {}
            if (length > 0) {
                buffer.SetReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    }
}
