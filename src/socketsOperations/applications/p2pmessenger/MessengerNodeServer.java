package socketsOperations.applications.p2pmessenger;

import java.io.IOException;
import java.util.function.Consumer;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestData;
import socketsOperations.utils.RequestHandler;

public class MessengerNodeServer implements Consumer<RequestHandler> {


    @Override
    public void accept(RequestHandler requestHandler) {

        try {
            RequestData request = requestHandler.receiveRequest();
            
            ConsoleOutput.println("Mensagem recebida: " + request.requestContent());

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
    	var request = new RequestData(CommunicationConstants.BADREQUEST, "Unknown request type: " + requestType);
        requestHandler.sendRequest(request);
    }

    private void receiveMessage(RequestData requestData, RequestHandler requestHandler) {
        String message = requestData.requestContent();

        ConsoleOutput.println("Mensagem recebida: " + message);
    	var request = new RequestData(CommunicationConstants.SUCCESS, " recebido a mensagem!");
        requestHandler.sendRequest(request);
    }
}