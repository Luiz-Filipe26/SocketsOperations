package socketsOperations.communicators.servers;

import java.io.*;
import java.util.function.*;

import socketsOperations.utils.ConsoleOutput;

public class EchoServer implements BiConsumer<BufferedReader, PrintWriter> {

    @Override
    public void accept(BufferedReader socketReader, PrintWriter socketOutput) {
        try {
            String line;
            while ((line = socketReader.readLine()) != null && !line.isEmpty()) {
                ConsoleOutput.println("Recebido: " + line);
            }
        } catch (Exception e) {
        	ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
        }
    }

}
