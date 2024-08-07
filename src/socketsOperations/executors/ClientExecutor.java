
package socketsOperations.executors;

import java.io.*;
import java.net.*;
import java.util.function.*;

public class ClientExecutor {

    public static void runClient(String host, int port, BiConsumer<BufferedReader, PrintWriter> messageSender) {

        new Thread(() -> {
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                messageSender.accept(in, out);

            } catch (IOException e) {
                System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
            }
        }).start();
    }
}
