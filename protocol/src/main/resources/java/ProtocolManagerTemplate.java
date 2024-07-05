${protocol_root_path}
import java.util.HashMap;
import java.util.Map;
${protocol_imports}

public class ProtocolManager {

    public static final short MAX_PROTOCOL_NUM = Short.MAX_VALUE;

    public static final IProtocolRegistration[] protocols = new IProtocolRegistration[MAX_PROTOCOL_NUM];

    public static Map<Class<?>, Short> protocolIdMap = new HashMap<>();

    public static void initProtocol() {
        // initProtocol
        ${protocol_manager_registrations}
    }

    public static short getProtocolId(Class<?> clazz) {
        return protocolIdMap.get(clazz);
    }

    public static IProtocolRegistration getProtocol(short protocolId) {
        var protocol = protocols[protocolId];
        if (protocol == null) {
            throw new RuntimeException("[protocolId:" + protocolId + "] not exist");
        }
        return protocol;
    }

    public static void write(ByteBuffer buffer, Object packet) {
        var protocolId = getProtocolId(packet.getClass());
        // write protocol id to buffer
        buffer.writeShort(protocolId);
        // write packet
        getProtocol(protocolId).write(buffer, packet);
    }

    public static Object read(ByteBuffer buffer) {
        var protocolId = buffer.readShort();
        return getProtocol(protocolId).read(buffer);
    }

}
