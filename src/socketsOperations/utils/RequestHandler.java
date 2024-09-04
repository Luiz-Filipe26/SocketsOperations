package socketsOperations.utils;

import java.io.*;

public class RequestHandler {

    private final BufferedReader socketReader;
    private final PrintWriter socketWriter;
    private volatile boolean waitingAnswer = false;
    private volatile boolean waitLineConsumption = false;
    private final Object waitingRequestLock = new Object();
    private final Object waitingAnswerLock = new Object();
    private final Object waitingFinishSocketConsumption = new Object();
    private String currentSocketLine;

    public record RequestData(String requestType, String requestContent) {

    }

    public RequestHandler(BufferedReader socketReader, PrintWriter socketWriter) {
        this.socketReader = socketReader;
        this.socketWriter = socketWriter;
        Thread socketLineReader = new Thread(getSocketLineReader());
        socketLineReader.start();
    }
    
	private Runnable getSocketLineReader() {
		return () -> {
			while(true) {
				try {
					if(waitLineConsumption) {
						synchronized (waitingFinishSocketConsumption) {
							waitingFinishSocketConsumption.wait();
						}
					}
					
					currentSocketLine = socketReader.readLine();
					if (waitingAnswer) {
						synchronized (waitingAnswerLock) {
							waitingAnswerLock.notifyAll();
						}
					} else {
						synchronized (waitingRequestLock) {
							waitingRequestLock.notifyAll();
						}
					}
	
				} catch (IOException e) {
					ConsoleOutput.println("Fechou a conexão");
				    synchronized (waitingRequestLock) {
				        waitingRequestLock.notifyAll();
				    }
				    synchronized (waitingAnswerLock) {
				        waitingAnswerLock.notifyAll();
				    }
					return;
				} catch (InterruptedException e) {
					ConsoleOutput.println("Erro ao esperar consumir linha" + e.getMessage());
				    synchronized (waitingRequestLock) {
				        waitingRequestLock.notifyAll();
				    }
				    synchronized (waitingAnswerLock) {
				        waitingAnswerLock.notifyAll();
				    }
					return;
				}
			}
		};
	}

    public RequestData receiveRequest() throws IOException {
    	
    	if(currentSocketLine == null || waitingAnswer) {
    		try {
    			synchronized (waitingRequestLock) {
    				waitingRequestLock.wait();
				}
			} catch (InterruptedException e) {
				ConsoleOutput.println("Erro ao esperar request: " + e.getMessage());
			}
    	}
    	
    	String line = currentSocketLine;
    	currentSocketLine = null;


		ConsoleOutput.println("A resposta do receiveRequest:" + line);
		
		if (line == null) {
			return new RequestData(CommunicationConstants.CONNECTION_CLOSED, "Conexão fechada");
		}

		String[] responseParts = line.split("\\| ", 2);
		String requestType = responseParts[0];
		String requestContent = responseParts.length > 1 ? responseParts[1] : "";


        return new RequestData(requestType, requestContent);
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

    private RequestData handleResponseByType(String requestType, String initialResponse) throws IOException {
        String answer;

        ConsoleOutput.println(requestType + "---" + initialResponse);
        
        waitLineConsumption = true;
        
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
        
        waitLineConsumption = false;
        
        synchronized (waitingFinishSocketConsumption) {
        	waitingFinishSocketConsumption.notifyAll();
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
