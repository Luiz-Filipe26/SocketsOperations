package socketsOperations.applications.p2pmessenger;

import java.io.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerNodeClient implements Consumer<RequestHandler> {

    private final String message;
    
    public MessengerNodeClient(String message) {
        this.message = message;
    }

    @Override
    public void accept(RequestHandler requestHandler) {
        try {
            RequestData requestData = requestHandler.sendRequestAndWaitAnswer(CommunicationConstants.MESSAGE, message);
            ConsoleOutput.println("Mensagem enviada: " + message);
            ConsoleOutput.println("Resposta do servidor: " + requestData.requestContent());
            handleServerMessages(requestData);
        } catch (IOException ex) {
            Logger.getLogger(MessengerNodeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleServerMessages(RequestData requestData) {
        switch (requestData.requestType()) {
            case CommunicationConstants.ERROR ->
                handleRequestError(requestData.requestContent());
            case CommunicationConstants.SUCCESS ->
                handleRequestSuccess(requestData.requestContent());
            case CommunicationConstants.NODE_INFO_ANSWER ->
                ConsoleOutput.print("Nó registrado!!!");
            default ->
                unknownRequest(requestData.requestContent());
        }
    }

    private void handleRequestSuccess(String requestContent) {
        ConsoleOutput.println("Erro ao receber request do servidor: " + requestContent);
    }

    private void handleRequestError(String requestContent) {
        ConsoleOutput.println("Erro ao receber request do servidor: " + requestContent);
    }

    private void unknownRequest(String requestType) {
        ConsoleOutput.println( "Unknown request type: " + requestType);
    }
}
