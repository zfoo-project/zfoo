using System;
using System.Collections.Generic;
using CsProtocol.Buffer;

namespace CsProtocol
{
    public class ProtocolManager
    {
        public static readonly short MAX_PROTOCOL_NUM = short.MaxValue;


        private static readonly IProtocolRegistration[] protocolList = new IProtocolRegistration[MAX_PROTOCOL_NUM];


        public static void InitProtocol()
        {
            var protocolRegistrationTypeList = new List<Type>();

            foreach (var assembly in AppDomain.CurrentDomain.GetAssemblies())
            {
                if (assembly.Equals(typeof(ProtocolManager).Assembly))
                {
                    var results = new List<Type>();
                    results.AddRange(assembly.GetTypes());
                    foreach (var type in results)
                    {
                        if (type.IsClass && !type.IsAbstract && typeof(IProtocolRegistration).IsAssignableFrom(type))
                        {
                            protocolRegistrationTypeList.Add(type);
                        }
                    }
                }
            }

            foreach (var protocolRegistrationType in protocolRegistrationTypeList)
            {
                var protocolRegistration = (IProtocolRegistration) Activator.CreateInstance(protocolRegistrationType);
                protocolList[protocolRegistration.ProtocolId()] = protocolRegistration;
            }
        }

        public static IProtocolRegistration GetProtocol(short protocolId)
        {
            var protocol = protocolList[protocolId];
            if (protocol == null)
            {
                throw new Exception("[protocolId:" + protocolId + "]协议不存在");
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