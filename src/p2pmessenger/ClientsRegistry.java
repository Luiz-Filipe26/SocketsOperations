
package p2pmessenger;

import java.util.*;

public class ClientsRegistry {
    public record ClientInfo(String IP, int port) {};
    
    private static final HashMap<String, ClientInfo> clients = new HashMap<>();
    
    public static void registryClientChannel(String name, String IP, int port) {
        clients.put(name, new ClientInfo(IP, port));
    }
    
    public static ClientInfo getNodeInfo(String name) {
        return clients.get(name);
    }
}
