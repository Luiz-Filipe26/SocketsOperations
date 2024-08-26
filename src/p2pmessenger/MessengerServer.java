package p2pmessenger;

import java.io.IOException;
import java.util.function.*;

import p2pmessenger.NodesRegistry.NodeInfo;
import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.RequestHandler;
import socketsOperations.utils.RequestHandler.RequestData;

public class MessengerServer implements Consumer<RequestHandler> {

    private RequestHandler requestHandler;

    @Override
    public void accept(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;

        while (true) {
            try {
                RequestData request = requestHandler.receiveRequest();

                switch (request.requestType()) {

                    case CommunicationConstants.ERROR ->
                        handleRequestError(request.requestContent());
                    case CommunicationConstants.REGISTER_NODE ->
                        registerNode(request.requestContent());
                    case CommunicationConstants.GET_NODE ->
                    	retrieveNode(request.requestContent());
                    default ->
                        unknownRequest(request.requestContent());

                }
            } catch (IOException e) {
                ConsoleOutput.println("Erro ao ler a requisição: " + e.getMessage());
            }
        }
    }
    
    private void retrieveNode(String name) {
		NodeInfo nodeInfo = NodesRegistry.getNodeInfo(name);
		String answer = nodeInfo.IP() + "-" + nodeInfo.port();
		
		requestHandler.sendRequest(CommunicationConstants.NODE_INFO_ANSWER, answer);
	}

	private void registerNode(String nodeInfo) {
        String[] nodeProps = nodeInfo.split("-");
        String name = nodeProps[0].split(":")[1];
        String IP = nodeProps[1].split(":")[1];
        int port = Integer.parseInt(nodeProps[2].split(":")[1]);
        
        NodesRegistry.registryClientChannel(name, IP, port);
    }

    private void handleRequestError(String requestContent) {
        ConsoleOutput.println("Request error from client: " + requestContent);
    }

    private void unknownRequest(String requestType) {
        requestHandler.sendRequest(CommunicationConstants.BADREQUEST, "Unknown request type: " + requestType);
    }
}
