namespace zfoocs
{
    public interface IProtocolRegistration
    {
        short ProtocolId();

        void Write(ByteBuffer buffer, object packet);
        
        object Read(ByteBuffer buffer);

    }
}