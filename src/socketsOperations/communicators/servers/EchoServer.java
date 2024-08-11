package socketsOperations.communicators.servers;

import java.util.function.*;

import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

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
