package socketsOperations.communicators.clients;

import java.io.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessageSender implements Consumer<RequestHandler> {
	
	private RequestHandler requestHandler;
	private String message;
	private final Object lock = new Object();
	private boolean stop;
	private boolean listAllMessages;
	
	public void sendMessage(String message) {
		this.message = message;
		finishWaiting();
	}

	public void stopClient() {
		stop = true;
	}

	public void askForAllMessages() {
		listAllMessages = true;
		finishWaiting();
	}

	@Override
	public void accept(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
		
//		new Thread(this::handleServerMessages).start();
		
		while (!stop) {
			if (message != null && !message.isBlank()) {
				requestHandler.sendRequest(CommunicationConstants.MESSAGE, message);
				message = "";
			}
			if (listAllMessages) {
				listAllMessages = false;
				try {
					RequestData answer = requestHandler.sendRequestAndWaitAnswer(CommunicationConstants.LISTREQUEST, "Please, list messages");
					switch(answer.requestType()) {
					case CommunicationConstants.ERROR -> ConsoleOutput.println("Erro: " + answer.requestContent());
					case CommunicationConstants.SUCCESS -> ConsoleOutput.println(answer.requestContent());
					case CommunicationConstants.BADREQUEST -> ConsoleOutput.println("Problema na request: " + answer.requestContent());
					default -> ConsoleOutput.println("Resposta desconhecida: " + answer.requestContent());
					};
				} catch (IOException e) {
					ConsoleOutput.println("Erro ao receber lista de mensagem" + e.getMessage());
				}
			}
			waitRequests();
		}
	}
	
	//Don't makes for client-server application
	@SuppressWarnings("unused")
	private void handleServerMessages() {
		while(true) {
			RequestData requestData;
			
			try {
				requestData = requestHandler.receiveRequest();
			} catch (IOException e) {
				ConsoleOutput.println("Erro ao receber request do servidor: " + e.getMessage());
				continue;
			}

			switch (requestData.requestType()) {
				case CommunicationConstants.ERROR -> handleRequestError(requestData.requestContent());
				case CommunicationConstants.SUCCESS -> handleRequestSuccess(requestData.requestContent());
				default -> unknownRequest(requestData.requestContent());
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