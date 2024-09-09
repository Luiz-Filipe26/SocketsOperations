package socketsOperations.applications.p2pmessenger;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import socketsOperations.applications.p2pmessenger.NodesRegistry.NodeInfo;
import socketsOperations.executors.ClientExecutor;
import socketsOperations.executors.ServerExecutor;
import socketsOperations.utils.CommunicationConstants;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;
import socketsOperations.utils.RequestData;

public class PTPClienteMessengerCLI {

	private static final String IP_REGEX = "^((\\d{1,3}\\.){3}\\d{1,3})|(localhost)$";

	private static final HashMap<String, NodeInfo> clients = new HashMap<>();

	public static void main(String[] args) {
		String serverIP;
		String clientIP;
		int serverPort;
		int clientPort;
		int option;
		boolean exit = false;
		String name;

		KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
		ConsoleOutput.setConsole(PTPClienteMessengerCLI::printServerOutput);

		clientIP = getLocalIP();
		if (clientIP == null) {
			System.out.println("[!] Erro ao buscar IP local.");
			return;
		}

		name = keyboardHandler.getInput(input -> !input.isBlank(), "Por favor, digite o nome: ", "[!] Nome invalido!\n");
		clientPort = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta do client: ", "[!] Porta invalida!\n");
		serverIP = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP do servidor: ", "[!] IP invalido\n");
		serverPort = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta do servidor central: ", "[!] Porta invalida!\n");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			System.out.println("Erro no sleep: " + ex);
		}

		MessengerNodeServer messengerNodeServer = new MessengerNodeServer();

		ServerExecutor serverRunner = new ServerExecutor(clientPort, messengerNodeServer);
		serverRunner.start();

		ClientExecutor.runClient(serverIP, serverPort, requestHandler -> {
			RequestData request = new RequestData(CommunicationConstants.REGISTER_NODE,
					"Name:" + name + "-IP:" + clientIP + "-Port:" + clientPort);
			requestHandler.sendRequest(request);
		});

		while (!exit) {
			printMenu();
			option = keyboardHandler.getIntInput(input -> input >= 1 && input <= 2, "Digite a opcao: \n",
					"[!] Opcao invalida!\n");
			switch (option) {
			case 1 -> {
				sendMessage(serverIP, serverPort);
			}
			case 2 -> exit = true;
			}
		}

		keyboardHandler.closeKeyboardEntry();
	}

	private static void sendMessage(String serverIP, int serverPort) {
		var keyboardHandler = KeyboardHandler.getInstance();

		String recipient = keyboardHandler.getInput(input -> !input.isBlank(), "Digite o destinatario: ", "[!!!] Destinatario invalido!\n");
		String message = keyboardHandler.getInput(input -> !input.isBlank(), "Digite a mensagem: ", "[!!!] Mensagem invalida!\n");
		NodeInfo recipientData = clients.get(recipient);

		if (recipientData == null) {
			var nodeInfoAskerClient = new NodeInfoAskerClient(recipient); 
			ClientExecutor.runClient(serverIP, serverPort, nodeInfoAskerClient);
			recipientData = nodeInfoAskerClient.getNodeInfo();
			
			if(recipientData == null) {
				System.out.println("Não foi encontrado o destinatário.");
				return;
			}
		}

		System.out.println("Dados do destinatário: " + recipientData);

		MessengerNodeClient client = new MessengerNodeClient(message);
		ClientExecutor.runClient(recipientData.IP(), recipientData.port(), client);
	}

	public static String getLocalIP() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("google.com", 80));
			String ip = socket.getLocalAddress().getHostAddress();

			return ip;
		} catch (Exception ex) {
			return null;
		}
	}

	public static void printServerOutput(String text) {
		System.out.print(text);
	}

	public static void printMenu() {
		System.out.println("+-------Menu de opcoes---------");
		System.out.println("|1. Mandar mensagem");
		System.out.println("|2. Sair");
		System.out.println("+------------------------------");
	}
}
