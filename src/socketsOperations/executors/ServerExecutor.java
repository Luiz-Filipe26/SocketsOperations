
package socketsOperations.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;

public class ServerExecutor {

    private Thread serverThread;
    private final int port;
    private final BiConsumer<BufferedReader, PrintWriter> requestProcessor;

    public ServerExecutor(int port, BiConsumer<BufferedReader, PrintWriter> requestProcessor) {
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

            requestProcessor.accept(in, out);
            out.println(CommunicationConstants.SUCCESS + "| Servidor criado com sucesso!");
            
        } catch (IOException e) {
            ConsoleOutput.println("Erro ao processar a conexão do cliente: " + e.getMessage());
        }
    }
}
