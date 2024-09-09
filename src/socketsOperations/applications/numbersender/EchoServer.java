package socketsOperations.applications.numbersender;

import java.util.function.Consumer;

import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestData;
import socketsOperations.utils.RequestHandler;

public class EchoServer implements Consumer<RequestHandler> {

	@Override
	public void accept(RequestHandler requestHandler) {
		while(true) {
			try {
				RequestData requestData = requestHandler.receiveRequest();
				ConsoleOutput.println("Recebido: " + requestData.requestContent());
			} catch (Exception e) {
				ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
			}
		}
	}
}
