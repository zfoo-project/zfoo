using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    {}
    public class {} : IProtocol
    {
        {}

        public static {} ValueOf({})
        {
            var packet = new {}();
            {}
            return packet;
        }


        public short ProtocolId()
        {
            return {};
        }
    }


    public class {}Registration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return {};
        }

        public void Write(ByteBuffer buffer, IProtocol packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            {} message = ({}) packet;
            {}
        }

        public IProtocol Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            {} packet = new {}();
            {}
            return packet;
        }
    }
}
