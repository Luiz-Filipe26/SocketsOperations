package socketsOperations.executors;

import java.io.*;
import java.net.*;
import java.util.function.*;

import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;

public class ClientExecutor {

    public static void runClient(String host, int port, Consumer<RequestHandler> messageSender) {

        new Thread(() -> {
            try (var socket = new Socket(host, port);
                 var out = new PrintWriter(socket.getOutputStream(), true);
                 var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            	
            	var requestHandler = new RequestHandler(in, out);
            	
                messageSender.accept(requestHandler);

            } catch (IOException e) {
                ConsoleOutput.println("Erro ao conectar ao servidor: " + e.getMessage());
            }
        }).start();
    }
}
