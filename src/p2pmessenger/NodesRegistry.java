
package p2pmessenger;

import java.util.*;

public class NodesRegistry {
    public record NodeInfo(String IP, int port) {};
    
    private static final HashMap<String, NodeInfo> clients = new HashMap<>();
    
    public static void registryClientChannel(String name, String IP, int port) {
        clients.put(name, new NodeInfo(IP, port));
    }
    
    public static NodeInfo getNodeInfo(String name) {
        return clients.get(name);
    }
}
