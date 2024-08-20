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

    public RequestData receiveRequest() throws IOException {

        RequestData requestData = null;

        while (requestData == null) {
            try {
                currentSocketLine = socketReader.readLine();
                if (waitingAnswer) {
                    waitConsumeLine();
                    continue;
                }

                ConsoleOutput.println("A resposta do receiveRequest sem ser answer:" + currentSocketLine);
                if (currentSocketLine == null) {
                    return new RequestData(CommunicationConstants.CONNECTION_CLOSED, "Conexão fechada");
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

    public RequestData sendRequestAndWaitAnswer(String requestType, String requestContent) throws IOException {
        waitingAnswer = true;
        sendRequest(requestType, requestContent);
        return receiveAnswer();
    }

    public void sendRequest(String requestType, String requestContent) {
        socketWriter.println(requestType + "| " + requestContent);
    }

    public RequestData receiveAnswer() throws IOException {
        waitReceiveAnswer();

        String answer = currentSocketLine;
        currentSocketLine = null;

        if (answer == null) {
            return new RequestData(CommunicationConstants.CONNECTION_CLOSED, "Conexão fechada");
        }

        String[] responseParts = answer.split("\\| ", 2);
        String requestType = responseParts[0];
        String requestContent = responseParts.length > 1 ? responseParts[1] : "";

        return handleResponseByType(requestType, requestContent);
    }

    private void waitReceiveAnswer() {
        waitingAnswer = true;
        synchronized (waitingAnswerLock) {
            try {
                waitingAnswerLock.wait();
            } catch (InterruptedException e) {
                ConsoleOutput.println("Erro ao esperar resposta: " + e.getMessage());
            }
        }
    }

    private void waitConsumeLine() {
        while (currentSocketLine != null) {
            synchronized (waitingAnswerLock) {
                waitingAnswerLock.notifyAll();
            }
        }
    }

    private RequestData handleResponseByType(String requestType, String initialResponse) throws IOException {
        String answer;

        ConsoleOutput.println(requestType + "---" + initialResponse);

        try {
            answer = switch (requestType) {
                case CommunicationConstants.LISTANSWER ->
                    handleListRequestResponse(initialResponse);
                default ->
                    handleDefaultResponse(initialResponse);
            };
        } catch (IOException e) {
            answer = e.getMessage();
            requestType = CommunicationConstants.ERROR;
        }

        waitingAnswer = false;

        return new RequestData(requestType, answer);
    }

    private String handleListRequestResponse(String initialResponse) throws IOException {
        StringBuilder listResponse = new StringBuilder(initialResponse).append("\n");

        String response;

        while (true) {
            waitReceiveAnswer();
            response = currentSocketLine;
            currentSocketLine = null;
            listResponse.append(response).append("\n");
            if (response.startsWith(CommunicationConstants.LISTEND + "| ")) {
                break;
            }
        }
        return listResponse.toString();
    }

    private String handleDefaultResponse(String initialResponse) {
        return initialResponse;
    }
}
