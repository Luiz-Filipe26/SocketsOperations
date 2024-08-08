package socketsOperations.communicators.servers;

import java.io.*;
import java.util.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;

public class MessengerServer implements BiConsumer<BufferedReader, PrintWriter> {

    @SuppressWarnings("unused")
	private BufferedReader socketReader;
    private PrintWriter socketOutput;

    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    
    @Override
    public void accept(BufferedReader socketReader, PrintWriter socketOutput) {
        this.socketReader = socketReader;
        this.socketOutput = socketOutput;

        try {
            String line;
            while ((line = socketReader.readLine()) != null && !line.isEmpty()) {
                String requestType = getRequestType(line);
                String requestContent = getRequestContent(line);

                switch (requestType) {
                    case CommunicationConstants.MESSAGE -> receiveMessage(requestContent);
                    case CommunicationConstants.LISTREQUEST -> sendAllMessages();
                    case CommunicationConstants.ERROR -> handleRequestError(requestContent);
                    default -> unknownRequest(requestType);
                }
            }
        } catch (Exception e) {
            ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
        }
    }

    private void handleRequestError(String requestContent) {
    	ConsoleOutput.println("Request error from client: " + requestContent);
    }

	private String getRequestType(String line) {
        int indexOfTypeSeparator = line.indexOf('|');
        if (indexOfTypeSeparator != -1) {
            return line.substring(0, indexOfTypeSeparator);
        }
        return "";
    }

    private String getRequestContent(String line) {
        int indexOfTypeSeparator = line.indexOf('|');
        if (indexOfTypeSeparator != -1 && indexOfTypeSeparator + 1 < line.length()) {
            return line.substring(indexOfTypeSeparator + 1);
        }
        return "";
    }

    private void unknownRequest(String requestType) {
        socketOutput.println(CommunicationConstants.ERROR + "| Unknown request type: " + requestType);
    }

    private void receiveMessage(String message) {
        messages.add(message);
        //socketOutput.println("Mensagem recebida.");
        ConsoleOutput.println("Mensagem recebida: " + message);
    }

    private void sendAllMessages() {
        synchronized (messages) {
            socketOutput.println(CommunicationConstants.LISTANSWER + "| Lista de todas as mensagens:");
            for(String message : messages) {
            	socketOutput.println(message);
            }
            socketOutput.println(CommunicationConstants.LISTEND + "| Fim da lista.");
        }
    }
}
