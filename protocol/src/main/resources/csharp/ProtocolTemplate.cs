using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    {}
    public class {} : IPacket
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

        public void Write(ByteBuffer buffer, IPacket packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            {} message = ({}) packet;
            {}
        }

        public IPacket Read(ByteBuffer buffer)
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
