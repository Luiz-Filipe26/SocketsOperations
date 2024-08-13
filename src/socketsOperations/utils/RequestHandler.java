package socketsOperations.utils;

import java.io.*;
import java.util.concurrent.*;

public class RequestHandler {

    private final BufferedReader socketReader;
    private final PrintWriter socketWriter;
    private CompletableFuture<RequestData> currentReadTask;
    private volatile boolean waitingAnswer = false;
    private final Object waitingAnswerLock = new Object();

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
        if (currentReadTask != null) {
            currentReadTask.cancel(true);
        }
        sendRequest(requestType, requestContent);
        return receiveAnswer(requestType);
    }

    public RequestData receiveAnswer(String requestType) throws IOException {
        waitingAnswer = true;
        if (currentReadTask != null) {
            currentReadTask.cancel(true);
        }
        String response = socketReader.readLine();

        if (response == null) {
            throw new IOException("Conexão fechada inesperadamente pelo servidor.");
        }

        return handleResponseByType(requestType, response);
    }

    private void waitAnswer() {
        synchronized (waitingAnswerLock) {
            try {
                waitingAnswerLock.wait();
            } catch (InterruptedException e) {
                ConsoleOutput.println("Erro ao esperar resposta: " + e.getMessage());
            }
        }
    }

    //  Don't use in a client-server architecture on the client-side: the client should only receive answers.
    public RequestData receiveRequest() throws IOException {

        RequestData requestData = null;

        while (requestData == null) {
            if (waitingAnswer) {
                waitAnswer();
            }

            currentReadTask = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
                try {
                    String response = socketReader.readLine();
                    if (response == null) {
                        throw new IOException("Conexão fechada inesperadamente pelo servidor.");
                    }

                    String[] responseParts = response.split("\\| ", 2);
                    String requestType = responseParts[0];
                    String requestContent = responseParts.length > 1 ? responseParts[1] : "";

                    currentReadTask.complete(new RequestData(requestType, requestContent));
                } catch (IOException e) {
                    currentReadTask.completeExceptionally(e);
                }
            });

            try {
                requestData = currentReadTask.get();
            } catch (Exception e) {
                if (!waitingAnswer) {
                    throw new IOException("Erro ao obter a solicitação", e);
                }
            }
        }

        return requestData;
    }

    private RequestData handleResponseByType(String requestType, String initialResponse) throws IOException {
        String answer;
        String resultType;

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
