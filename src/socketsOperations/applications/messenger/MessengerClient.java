package socketsOperations.applications.messenger;

import java.io.IOException;
import java.util.function.Consumer;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestData;
import socketsOperations.utils.RequestHandler;

public class MessengerClient implements Consumer<RequestHandler> {

    private RequestHandler requestHandler;
    private String message;
    private String recipient;
    private final Object lock = new Object();
    private boolean stop;
    private boolean listAllMessages;

    public void sendMessage(String recipient, String message) {
        this.message = message;
        this.recipient = recipient;
        finishWaiting();
    }

    public void stopClient() {
        stop = true;
    }

    public void askForAllMessages() {
        listAllMessages = true;
        finishWaiting();
    }
    
    public void registryClient(String name) {
    	var request = new RequestData(CommunicationConstants.REGISTER_CLIENT, name);
        requestHandler.sendRequest(request);
    }

    @Override
    public void accept(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;

        new Thread(this::handleServerMessages).start();
        
        while (!stop) {
            if (message != null && !message.isBlank()) {
            	var request = new RequestData(CommunicationConstants.MESSAGE, recipient + ":" + message);
                requestHandler.sendRequest(request);
                message = "";
                recipient = "";
            }
            if (listAllMessages) {
                listAllMessages = false;
                try {
                	var request = new RequestData(CommunicationConstants.LISTREQUEST, "Please, list messages");
                    RequestData answer = requestHandler.sendRequestAndWaitAnswer(request);
                    switch (answer.requestType()) {
                        case CommunicationConstants.ERROR ->
                            ConsoleOutput.println("Erro: " + answer.requestContent());
                        case CommunicationConstants.SUCCESS ->
                            ConsoleOutput.println(answer.requestContent());
                        case CommunicationConstants.BADREQUEST ->
                            ConsoleOutput.println("Problema na request: " + answer.requestContent());
                        case CommunicationConstants.LISTANSWER ->
                        	ConsoleOutput.println("Lista de mensagens:\n" + answer.requestContent());
                        default ->
                            ConsoleOutput.println("Resposta desconhecida: " + answer.requestContent());
                    };
                } catch (IOException e) {
                    ConsoleOutput.println("Erro ao receber lista de mensagem" + e.getMessage());
                }
            }
            waitRequests();
        }
    }

    private void handleServerMessages() {
        while (true) {
            RequestData requestData;

            try {
                requestData = requestHandler.receiveRequest();
            } catch (IOException e) {
                ConsoleOutput.println("Erro ao receber request do servidor: " + e.getMessage());
                continue;
            }

            switch (requestData.requestType()) {
                case CommunicationConstants.MESSAGE -> {
                    ConsoleOutput.println("Mensagem recebida pelo servidor: " + requestData.requestContent());
                    var request = new RequestData(CommunicationConstants.SUCCESS, "mensagem recebida!");
                    requestHandler.sendRequest(request);
                }
                case CommunicationConstants.ERROR ->
                    handleRequestError(requestData.requestContent());
                case CommunicationConstants.SUCCESS ->
                    handleRequestSuccess(requestData.requestContent());
                default ->
                    unknownRequest(requestData.requestContent());
            }
        }
    }

    private void handleRequestSuccess(String requestContent) {
        ConsoleOutput.println("Erro ao receber request do servidor: " + requestContent);
    }

    private void handleRequestError(String requestContent) {
        ConsoleOutput.println("Erro ao receber request do servidor: " + requestContent);
    }

    private void unknownRequest(String requestType) {
    	var request = new RequestData(CommunicationConstants.BADREQUEST, "Unknown request type: " + requestType);
        requestHandler.sendRequest(request);
    }

    private void finishWaiting() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void waitRequests() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                ConsoleOutput.println("Erro: " + e.getMessage());
            }
        }
    }
}
