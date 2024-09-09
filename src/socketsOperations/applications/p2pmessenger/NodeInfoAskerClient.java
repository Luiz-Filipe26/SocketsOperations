package socketsOperations.applications.p2pmessenger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import socketsOperations.applications.p2pmessenger.NodesRegistry.NodeInfo;
import socketsOperations.utils.*;

public class NodeInfoAskerClient implements Consumer<RequestHandler> {
	
	private volatile NodeInfo nodeInfo;
	private final String recipient;
	private final CountDownLatch nodeInfoWaiterlatch = new CountDownLatch(1);

	public NodeInfoAskerClient(String recipient) {
		this.recipient = recipient;
	}

	@Override
	public void accept(RequestHandler requestHandler) {
		RequestData request = new RequestData(CommunicationConstants.GET_NODE, recipient);
		RequestData answer;
		
		try {
			answer = requestHandler.sendRequestAndWaitAnswer(request);
		} catch (IOException e) {
			ConsoleOutput.println("Erro ao esperar a resposta: " + e.getMessage());
			return;
		}
		
		System.out.println("A resposta foi esta: " + answer);
		String[] answerData = answer.requestContent().split("-");
		
		if (answerData.length != 2 || !answerData[1].matches("\\d+")) {
			ConsoleOutput.println("Resposta fora do formato esperado");
			nodeInfoWaiterlatch.countDown();
			return;
		}		
		
		nodeInfo = new NodeInfo(answerData[0], Integer.parseInt(answerData[1]));
		nodeInfoWaiterlatch.countDown();
	}
	
	public NodeInfo getNodeInfo() {
		try {
			nodeInfoWaiterlatch.await();
		} catch (InterruptedException e) {
			ConsoleOutput.println("Erro ao esperar a resposta: " + e.getMessage());
		}
		
		return nodeInfo;
	}
}