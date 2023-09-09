using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    
    public class EmptyObject : IProtocol
    {
        

        public static EmptyObject ValueOf()
        {
            var packet = new EmptyObject();
            
            return packet;
        }


        public short ProtocolId()
        {
            return 0;
        }
    }


    public class EmptyObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 0;
        }

        public void Write(ByteBuffer buffer, IProtocol packet)
        {
            if (buffer.WritePacketFlag(packet))
            {
                return;
            }
            EmptyObject message = (EmptyObject) packet;
            
        }

        public IProtocol Read(ByteBuffer buffer)
        {
            if (!buffer.ReadBool())
            {
                return null;
            }
            EmptyObject packet = new EmptyObject();
            
            return packet;
        }
    }
}
