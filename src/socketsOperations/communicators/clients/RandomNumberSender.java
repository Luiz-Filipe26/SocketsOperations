package socketsOperations.communicators.clients;

import java.io.*;
import java.util.*;
import java.util.function.*;

import socketsOperations.utils.ConsoleOutput;

public class RandomNumberSender implements BiConsumer<BufferedReader, PrintWriter> {

    private final int numberOfRandomNumbers;

    public RandomNumberSender(int numberOfRandomNumbers) {
        this.numberOfRandomNumbers = numberOfRandomNumbers;
    }

    @Override
    public void accept(BufferedReader in, PrintWriter out) {
        Random random = new Random();
        for (int i = 0; i < numberOfRandomNumbers; i++) {
            int randomNumber = random.nextInt(100);
            String message = "oi from " + randomNumber;
            out.println(message);
            ConsoleOutput.println("Enviado: " + message);
        }
    }
}
