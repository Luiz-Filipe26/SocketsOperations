package socketsOperations.clients;

import java.io.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;

public class MessageSender implements BiConsumer<BufferedReader, PrintWriter> {
	
    private BufferedReader socketReader;
    private PrintWriter socketOutput;
	private String message;
	private final Object lock = new Object();
	private boolean stop;
	private boolean listAllMessages;
	
	public Consumer<String> output;
    
    public MessageSender(Consumer<String> output) {
        this.output = output;
    }

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
	public void accept(BufferedReader socketReader, PrintWriter socketOutput) {
		this.socketReader = socketReader;
		this.socketOutput = socketOutput;
		
		new Thread(this::handleServerMessages).start();
		
		while (!stop) {
			if (message != null && !message.isBlank()) {
				socketOutput.println("msg| " +message);
				message = "";
			}
			if (listAllMessages) {
				listAllMessages = false;
				socketOutput.println(CommunicationConstants.LISTREQUEST + "| Please, list messages");
			}
			waitRequests();
		}
	}
	
	private void handleServerMessages() {
		try {
			String line;
			while ((line = socketReader.readLine()) != null && !line.isEmpty()) {
				String requestType = getRequestType(line);
				String requestContent = getRequestContent(line);

				switch (requestType) {
					case CommunicationConstants.LISTANSWER -> receiveList(socketReader, requestContent);
					case CommunicationConstants.ERROR -> handleRequestError(requestContent);
					case CommunicationConstants.SUCCESS -> handleRequestSuccess(requestContent);
					default -> unknownRequest(requestType);
				}
			}
		} catch (Exception e) {
			output.accept("Erro ao ler a requisição: " + e.getMessage());
		}
	}
	
    private void handleRequestSuccess(String requestContent) {
    	output.accept("Request success from server: " + requestContent);
	}

	private void handleRequestError(String requestContent) {
    	output.accept("Request error from server: " + requestContent);
	}

	private void receiveList(BufferedReader socketReader, String requestContent) {
		output.accept(requestContent);
		String line;
		try {
			while ((line = socketReader.readLine()) != null && !line.isEmpty()) {
				if(line.contains("|")) {
					String requestType = getRequestType(line);
					requestContent = getRequestContent(line);
					output.accept(requestContent);

					if(requestType.equals(CommunicationConstants.LISTEND)) {
						break;
					}
				}
				else {
					output.accept(line);
				}
			} 
		} catch (Exception e) {
			output.accept("Erro ao ler a requisição: " + e.getMessage());
		}
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
				output.accept("Erro: " + e.getMessage());
			}
		}
	}
}
