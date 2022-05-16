namespace CsProtocol.Buffer
{
    public interface IProtocolRegistration
    {
        short ProtocolId();

        void Write(ByteBuffer buffer, IPacket packet);
        
        IPacket Read(ByteBuffer buffer);

    }
}