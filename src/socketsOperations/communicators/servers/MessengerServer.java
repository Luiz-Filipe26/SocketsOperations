package socketsOperations.communicators.servers;

import java.util.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerServer implements Consumer<RequestHandler> {

	private RequestHandler requestHandler;

	private final List<String> messages = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void accept(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;

		while (true) {
			try {
				RequestData request = requestHandler.receiveRequest();

				switch (request.requestType()) {

				case CommunicationConstants.MESSAGE -> receiveMessage(request.requestContent());
				case CommunicationConstants.LISTREQUEST -> sendAllMessages();
				case CommunicationConstants.ERROR -> handleRequestError(request.requestContent());
				default -> unknownRequest(request.requestContent());

				}
			} catch (Exception e) {
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

	private void receiveMessage(String message) {
		messages.add(message);
		// socketOutput.println("Mensagem recebida.");
		ConsoleOutput.println("Mensagem recebida: " + message);
	}

	private void sendAllMessages() {
		synchronized (messages) {
			requestHandler.sendRequest(CommunicationConstants.LISTANSWER, "Lista de todas as mensagens:");
			StringBuilder completeList = new StringBuilder(50 * messages.size());

			for (String message : messages) {
				completeList.append(message + "\n");
			}
			requestHandler.sendRequest("", completeList.toString());
			requestHandler.sendRequest(CommunicationConstants.LISTEND, "Fim da lista.");
		}
	}
}
