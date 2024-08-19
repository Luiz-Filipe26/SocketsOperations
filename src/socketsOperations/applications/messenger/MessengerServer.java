package socketsOperations.applications.messenger;

import java.io.IOException;
import java.util.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerServer implements Consumer<RequestHandler> {

    private RequestHandler requestHandler;
    private String currentClient;

    @Override
    public void accept(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;

        while (true) {
            try {
                RequestData request = requestHandler.receiveRequest();

                switch (request.requestType()) {

                    case CommunicationConstants.MESSAGE ->
                        receiveMessage(request);
                    case CommunicationConstants.LISTREQUEST ->
                        sendAllMessages();
                    case CommunicationConstants.ERROR ->
                        handleRequestError(request.requestContent());
                    case CommunicationConstants.REGISTER_CLIENT ->
                        registerClient(request.requestContent());
                    default ->
                        unknownRequest(request.requestContent());

                }
            } catch (Exception e) {
                ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
            }
        }
    }

    private void registerClient(String clientName) {
        this.currentClient = clientName;
        ClientsRegistry.registryClientChannel(clientName, requestHandler);
        ConsoleOutput.println("Cliente registrado: " + clientName);
    }

    private void handleRequestError(String requestContent) {
        ConsoleOutput.println("Request error from client: " + requestContent);
    }

    private void unknownRequest(String requestType) {
        requestHandler.sendRequest(CommunicationConstants.BADREQUEST, "Unknown request type: " + requestType);
    }

    private void receiveMessage(RequestData requestData) {
        String[] messageContent = requestData.requestContent().split(":");
        String client = messageContent[0];
        String message = messageContent[1];

        RequestHandler requestChannel = ClientsRegistry.getClientChannel(client);
        RequestData success = null;
        try {
            success = requestChannel.sendRequestAndWaitAnswer(requestData.requestType(), requestData.requestContent());
        } catch (IOException ex) {
            ConsoleOutput.println("Erro ao enviar mensagem ao cliente: " + ex.getMessage());
            return;
        }
        
        if(success == null || !success.requestType().equals(CommunicationConstants.SUCCESS)) {
            ConsoleOutput.println("Erro ao mandar mensagem: " + requestData.requestContent());
            return;
        }

        ClientsRegistry.registryMessage(client, message);

        ConsoleOutput.println("Mensagem recebida: " + message);
    }

    private void sendAllMessages() {
        
        List<String> messages = ClientsRegistry.getMessagesOfClient(currentClient);

        requestHandler.sendRequest(CommunicationConstants.LISTANSWER, "Lista de todas as mensagens:");
        StringBuilder completeList = new StringBuilder(50 * messages.size());

        for (String message : messages) {
            completeList.append(message).append("\n");
        }
        requestHandler.sendRequest("", completeList.toString());
        requestHandler.sendRequest(CommunicationConstants.LISTEND, "Fim da lista.");
    }
}
