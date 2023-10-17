using System;
using System.Collections.Generic;

namespace zfoocs
{
    public class ProtocolManager
    {
        public static readonly short MAX_PROTOCOL_NUM = short.MaxValue;


        private static readonly IProtocolRegistration[] protocolList = new IProtocolRegistration[MAX_PROTOCOL_NUM];
        private static readonly Dictionary<Type, short> protocolIdMap = new Dictionary<Type, short>();


        public static void InitProtocol()
        {
            {}
        }

        public static IProtocolRegistration GetProtocol(short protocolId)
        {
            var protocol = protocolList[protocolId];
            if (protocol == null)
            {
                throw new Exception("[protocolId:" + protocolId + "] not exist");
            }

            return protocol;
        }

        public static void Write(ByteBuffer buffer, IProtocol packet)
        {
            var protocolId = packet.ProtocolId();
            // 写入协议号
            buffer.WriteShort(protocolId);

            // 写入包体
            GetProtocol(protocolId).Write(buffer, packet);
        }

        public static IProtocol Read(ByteBuffer buffer)
        {
            var protocolId = buffer.ReadShort();
            return GetProtocol(protocolId).Read(buffer);
        }
    }
}