package p2pmessenger;

import java.io.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerNodeClient implements Consumer<RequestHandler> {

    private RequestHandler requestHandler;
    private String message;
    private String recipient;
    private final Object lock = new Object();
    private boolean stop;
    
    public MessengerNodeClient() {
    }

    public void sendMessage(String recipient, String message) {
        this.message = message;
        this.recipient = recipient;
        finishWaiting();
    }

    public void stopClient() {
        stop = true;
    }
    
    public void registryClient(String name, String IP, int port) {
        requestHandler.sendRequest(CommunicationConstants.REGISTER_NODE, "Name:" + name + "-IP:" + IP + "-Port:"+port);
    }

    @Override
    public void accept(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;

        new Thread(this::handleServerMessages).start();
        
        while (!stop) {
            if (message != null && !message.isBlank()) {
                requestHandler.sendRequest(CommunicationConstants.MESSAGE, recipient + ":" + message);
                message = "";
                recipient = "";
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
                    requestHandler.sendRequest(CommunicationConstants.SUCCESS, "mensagem recebida!");
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
        requestHandler.sendRequest(CommunicationConstants.BADREQUEST, "Unknown request type: " + requestType);
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
