package socketsOperations.applications.messenger;

import java.util.*;

import socketsOperations.utils.RequestHandler;

public class ClientsRegistry {
    private static HashMap<String, RequestHandler> clients = new HashMap<>();
    private static HashMap<String, List<String>> messagesOfClients = new HashMap<>();
    
    public static void registryClientChannel(String name, RequestHandler requestHandler) {
        clients.put(name, requestHandler);
        messagesOfClients.computeIfAbsent(name, list -> new ArrayList<>());
    }
    
    public static RequestHandler getClientChannel(String name) {
        return clients.get(name);
    }
    
    public static void registryMessage(String client, String message) {
        messagesOfClients.computeIfAbsent(client, list -> new ArrayList<>()).add(message);
    }
    
    public static List<String> getMessagesOfClient(String client) {
        return messagesOfClients.get(client);
    }
}
