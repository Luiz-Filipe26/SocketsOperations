package socketsOperations.applications.p2pmessenger;

import java.io.IOException;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerNodeServer implements Consumer<RequestHandler> {


    @Override
    public void accept(RequestHandler requestHandler) {

        try {
            RequestData request = requestHandler.receiveRequest();

            if(request.requestType().equals(CommunicationConstants.CONNECTION_CLOSED)) {
                return;
            }

            switch (request.requestType()) {
                case CommunicationConstants.MESSAGE ->
                    receiveMessage(request, requestHandler);
                case CommunicationConstants.ERROR ->
                    handleRequestError(request.requestContent());
                default ->
                    unknownRequest(request.requestContent(), requestHandler);
            }
        } catch (IOException e) {
            ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
        }
    }

    private void handleRequestError(String requestContent) {
        ConsoleOutput.println("Request error from client: " + requestContent);
    }

    private void unknownRequest(String requestType, RequestHandler requestHandler) {
        requestHandler.sendRequest(CommunicationConstants.BADREQUEST, "Unknown request type: " + requestType);
    }

    private void receiveMessage(RequestData requestData, RequestHandler requestHandler) {
        String message = requestData.requestContent();

        ConsoleOutput.println("Mensagem recebida: " + message);
        requestHandler.sendRequest(CommunicationConstants.SUCCESS, " recebido a mensagem!");
    }
}