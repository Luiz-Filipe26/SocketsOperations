package socketsOperations.servers;

import java.io.*;
import java.util.function.*;

public class EchoServer implements BiConsumer<BufferedReader, PrintWriter> {
    
    public Consumer<String> output;
    
    public EchoServer(Consumer<String> output) {
        this.output = output;
    }

    @Override
    public void accept(BufferedReader socketReader, PrintWriter socketOutput) {
        try {
            String line;
            while ((line = socketReader.readLine()) != null && !line.isEmpty()) {
                output.accept("Recebido: " + line);
            }
        } catch (Exception e) {
        	output.accept("Erro ao ler a requisição: " + e.getMessage());
        }
    }

}
