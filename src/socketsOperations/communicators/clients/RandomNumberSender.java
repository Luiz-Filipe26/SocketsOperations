package socketsOperations.communicators.clients;

import java.util.*;
import java.util.function.*;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;

public class RandomNumberSender implements Consumer<RequestHandler> {

    private final int numberOfRandomNumbers;

    public RandomNumberSender(int numberOfRandomNumbers) {
        this.numberOfRandomNumbers = numberOfRandomNumbers;
    }

    @Override
    public void accept(RequestHandler requestHandler) {
        Random random = new java.util.Random();
        for (int i = 0; i < numberOfRandomNumbers; i++) {
            int randomNumber = random.nextInt(100);
            String message = "oi from " + randomNumber;
            requestHandler.sendRequest(CommunicationConstants.MESSAGE, message);
            ConsoleOutput.println("Enviado: " + message);
        }
    }
}
