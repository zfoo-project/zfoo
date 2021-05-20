using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    // @author jaysunxiao
    // @version 1.0
    // @since 2021-03-27 15:18
    public class SimpleObject : IPacket
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
            return 1163;
        }
    }


    public class SimpleObjectRegistration : IProtocolRegistration
    {
        public short ProtocolId()
        {
            return 1163;
        }

        public void Write(ByteBuffer buffer, IPacket packet)
        {
            if (packet == null)
            {
                buffer.WriteBool(false);
                return;
            }
            buffer.WriteBool(true);
            SimpleObject message = (SimpleObject) packet;
            buffer.WriteInt(message.c);
            buffer.WriteBool(message.g);
        }

        public IPacket Read(ByteBuffer buffer)
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
