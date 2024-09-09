package socketsOperations.applications.p2pmessenger;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestData;
import socketsOperations.utils.RequestHandler;

public class MessengerNodeClient implements Consumer<RequestHandler> {

    private final String message;
    
    public MessengerNodeClient(String message) {
        this.message = message;
    }

    @Override
    public void accept(RequestHandler requestHandler) {
        try {
        	var request = new RequestData(CommunicationConstants.MESSAGE, message);
            RequestData answer = requestHandler.sendRequestAndWaitAnswer(request);
            ConsoleOutput.println("Mensagem enviada: " + message);
            ConsoleOutput.println("Resposta do servidor: " + answer.requestContent());
            handleServerMessages(answer);
        } catch (IOException ex) {
            Logger.getLogger(MessengerNodeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleServerMessages(RequestData answer) {
        switch (answer.requestType()) {
            case CommunicationConstants.ERROR ->
                handleRequestError(answer.requestContent());
            case CommunicationConstants.SUCCESS ->
                handleRequestSuccess(answer.requestContent());
            case CommunicationConstants.NODE_INFO_ANSWER ->
                ConsoleOutput.print("Nó registrado!!!");
            default ->
                unknownRequest(answer.requestContent());
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
