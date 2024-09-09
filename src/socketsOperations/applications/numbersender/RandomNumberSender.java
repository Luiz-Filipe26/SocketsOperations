package socketsOperations.applications.numbersender;

import java.util.Random;
import java.util.function.Consumer;

import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestData;
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
            var request = new RequestData(CommunicationConstants.MESSAGE, message);
            requestHandler.sendRequest(request);
            ConsoleOutput.println("Enviado: " + message);
        }
    }
}
