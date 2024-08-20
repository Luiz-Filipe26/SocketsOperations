package p2pmessenger;

import java.io.IOException;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerNodeServer implements Consumer<RequestHandler> {

    private RequestHandler requestHandler;
    private String currentClient;

    @Override
    public void accept(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;

        while (true) {
            try {
                RequestData request = requestHandler.receiveRequest();

                if(request.requestType().equals(CommunicationConstants.CONNECTION_CLOSED)) {
                    return;
                }
                
                switch (request.requestType()) {
                    case CommunicationConstants.MESSAGE ->
                        receiveMessage(request);
                    case CommunicationConstants.ERROR ->
                        handleRequestError(request.requestContent());
                    default ->
                        unknownRequest(request.requestContent());
                }
            } catch (IOException e) {
                ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
            }
        }
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

        ConsoleOutput.println("Mensagem recebida: " + message);
    }
}