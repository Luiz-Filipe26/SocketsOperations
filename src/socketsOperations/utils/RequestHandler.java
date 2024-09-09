package socketsOperations.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class RequestHandler {

	private final PrintWriter socketWriter;
	private final SocketLineReader socketLineReader;
	private static final int ANSWER_PRIORITY = 1;
	private static final int GENERAL_REQUEST_PRIORITY = 2;

	public RequestHandler(BufferedReader socketReader, PrintWriter socketWriter) {
		this.socketWriter = socketWriter;
		this.socketLineReader = new SocketLineReader(socketReader);
		Thread socketLineReaderThread = new Thread(socketLineReader);
		socketLineReaderThread.start();
	}

	public RequestData receiveRequest() throws IOException {
		String line = socketLineReader.getLine(GENERAL_REQUEST_PRIORITY);

		String[] responseParts = line.split(Pattern.quote(CommunicationConstants.TYPEANDCONTENTSEPARATOR), 2);
		String requestType = responseParts[0];
		String requestContent = responseParts.length > 1 ? responseParts[1] : "";
		
		System.out.println("Request recebida: " + requestType + "-" + requestContent);

		return new RequestData(requestType, requestContent);
	}

	public RequestData sendRequestAndWaitAnswer(RequestData requestData) throws IOException {
		sendRequest(requestData);

		Object answerLock = new Object();
		String answer = socketLineReader.getLine(answerLock, ANSWER_PRIORITY);

		if (answer == null) {
			return new RequestData(CommunicationConstants.CONNECTION_CLOSED, "Conexão fechada");
		}

		String[] responseParts = answer.split(Pattern.quote(CommunicationConstants.TYPEANDCONTENTSEPARATOR), 2);
		String responseType = responseParts[0];
		String responseContent = responseParts.length > 1 ? responseParts[1] : "";

		return handleResponseByType(new RequestData(responseType, responseContent), answerLock);
	}

	public void sendRequest(RequestData requestData) {
		System.out.println("Request enviada: " + requestData);
		socketWriter.println(requestData.requestType() + CommunicationConstants.TYPEANDCONTENTSEPARATOR + requestData.requestContent());
		socketWriter.flush();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private RequestData handleResponseByType(RequestData initialResponse, Object answerLock) throws IOException {
		String answer;

		ConsoleOutput.println("Resposta inicial: " + initialResponse.requestType() + "---" + initialResponse.requestContent());

		answer = switch (initialResponse.requestType()) {
			case CommunicationConstants.LISTANSWER ->
				handleListRequestResponse(initialResponse, answerLock);
			default ->
				handleDefaultResponse(initialResponse);
		};

		socketLineReader.signalFinishedReading(answerLock);

		return new RequestData(initialResponse.requestType(), answer);
	}

	private String handleListRequestResponse(RequestData initialResponse, Object answerLock) throws IOException {
		StringBuilder listResponse = new StringBuilder(initialResponse.requestContent()).append("\n");

		String response;

		while (true) {
			response = socketLineReader.getLine(answerLock, ANSWER_PRIORITY);
			if (response.startsWith(CommunicationConstants.CONNECTION_CLOSED)
					|| response.startsWith(CommunicationConstants.LISTEND)) {
				break;
			}
			listResponse.append(response).append("\n");
		}

		return listResponse.toString();
	}

	private String handleDefaultResponse(RequestData initialResponse) {
		return initialResponse.requestContent();
	}
}