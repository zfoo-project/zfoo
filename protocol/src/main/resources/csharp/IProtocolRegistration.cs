namespace CsProtocol.Buffer
{
    public interface IProtocolRegistration
    {
        short ProtocolId();

        void Write(ByteBuffer buffer, IProtocol packet);
        
        IProtocol Read(ByteBuffer buffer);

    }
}