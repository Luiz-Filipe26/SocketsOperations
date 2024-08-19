package socketsOperations.utils;

import java.io.*;

public class RequestHandler {

    private final BufferedReader socketReader;
    private final PrintWriter socketWriter;
    private volatile boolean waitingAnswer = false;
    private final Object waitingAnswerLock = new Object();
    private String currentSocketLine;

    public record RequestData(String requestType, String requestContent) {

    }

    public RequestHandler(BufferedReader socketReader, PrintWriter socketWriter) {
        this.socketReader = socketReader;
        this.socketWriter = socketWriter;
    }

    public void sendRequest(String requestType, String requestContent) {
        socketWriter.println(requestType + "| " + requestContent);
    }

    public RequestData sendRequestAndWaitAnswer(String requestType, String requestContent) throws IOException {
        waitingAnswer = true;
        sendRequest(requestType, requestContent);
        return receiveAnswer(requestType);
    }

    public RequestData receiveAnswer(String requestType) throws IOException {
        waitingAnswer = true;
        waitReceiveAnswer();

        if (currentSocketLine == null) {
            throw new IOException("Conexão fechada inesperadamente pelo servidor.");
        }

        return handleResponseByType(requestType, currentSocketLine);
    }

    private void waitReceiveAnswer() {
        synchronized (waitingAnswerLock) {
            try {
                waitingAnswerLock.wait();
            } catch (InterruptedException e) {
                ConsoleOutput.println("Erro ao esperar resposta: " + e.getMessage());
            }
        }
    }

    public RequestData receiveRequest() throws IOException {

        RequestData requestData = null;

        while (requestData == null) {
            try {
                currentSocketLine = socketReader.readLine();
                if(waitingAnswer) {
                	synchronized (waitingAnswerLock) {
                		waitingAnswerLock.notifyAll();
					}
                	continue;
                }
                
                ConsoleOutput.println("A resposta do runAsync:" + currentSocketLine);
                if (currentSocketLine == null) {
                    throw new IOException("Conexão fechada inesperadamente pelo servidor.");
                }

                String[] responseParts = currentSocketLine.split("\\| ", 2);
                String requestType = responseParts[0];
                String requestContent = responseParts.length > 1 ? responseParts[1] : "";

                requestData = new RequestData(requestType, requestContent);
            } catch (IOException e) {
                if (!waitingAnswer) {
                    throw new IOException("Erro ao ler o socket: ", e);
                } else {
                	ConsoleOutput.println("Erro ao ler o scoket: " + e);
                }
            }
        }

        return requestData;
    }

    private RequestData handleResponseByType(String requestType, String initialResponse) throws IOException {
        String answer;
        String resultType;
        
        ConsoleOutput.println(requestType + "---" + initialResponse);

        try {
            answer = switch (requestType) {
                case CommunicationConstants.LISTREQUEST ->
                    handleListRequestResponse(initialResponse);
                default ->
                    handleDefaultResponse(initialResponse);
            };
            resultType = "SUCCESS";
        } catch (IOException e) {
            answer = e.getMessage();
            resultType = "ERROR";
        }

        waitingAnswer = false;
        synchronized (waitingAnswerLock) {
            waitingAnswerLock.notifyAll();
        }

        return new RequestData(resultType, answer);
    }

    private String handleListRequestResponse(String initialResponse) throws IOException {
        StringBuilder listResponse = new StringBuilder(initialResponse).append("\n");

        String response;
        while (!(response = socketReader.readLine()).startsWith(CommunicationConstants.LISTEND + "| ")) {
            listResponse.append(response).append("\n");
        }
        return listResponse.toString();
    }

    private String handleDefaultResponse(String initialResponse) {
        return initialResponse;
    }
}
