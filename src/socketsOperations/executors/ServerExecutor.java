package socketsOperations.executors;

import java.io.*;
import java.net.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;

public class ServerExecutor {

    private Thread serverThread;
    private final int port;
    private final Consumer<RequestHandler> requestProcessor;

    public ServerExecutor(int port, Consumer<RequestHandler> requestProcessor) {
        this.port = port;
        this.requestProcessor = requestProcessor;
    }

    public void start() {
        serverThread = new Thread(this::runServer);
        serverThread.start();
    }

    public void stop() {
        if (serverThread != null) {
            serverThread.interrupt();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                ConsoleOutput.println("Erro ao interromper a thread do servidor: " + e.getMessage());
            }
        }
    }

    public int getPort() {
        return port;
    }
    
    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	
            ConsoleOutput.println("Servidor ouvindo na porta " + port);
            
            serverLoop(serverSocket);
            
        } catch (Exception e) {
            ConsoleOutput.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
    
    private void serverLoop(ServerSocket serverSocket) {
    	while (!Thread.currentThread().isInterrupted()) {
    		try {
    			Socket clientSocket = serverSocket.accept();
    			new Thread(() -> processNewServer(clientSocket)).start();
    		} catch (Exception e) {
    			ConsoleOutput.println("Erro ao aceitar conexão: " + e.getMessage());
    		}
    	}    	
    }

    private void processNewServer(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
        	
        	var requestHandler = new RequestHandler(in, out);
            requestProcessor.accept(requestHandler);
            requestHandler.sendRequest(CommunicationConstants.SUCCESS, "Servidor criado com sucesso!");
            
        } catch (IOException e) {
            ConsoleOutput.println("Erro ao processar a conexão do cliente: " + e.getMessage());
        }
    }
}
