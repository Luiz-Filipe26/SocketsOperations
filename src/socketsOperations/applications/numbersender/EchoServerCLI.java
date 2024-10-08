package socketsOperations.applications.numbersender;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import socketsOperations.executors.ServerExecutor;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;

public class EchoServerCLI {

	private static final Map<Integer, ServerExecutor> servers = new HashMap<>();
	private static int serverCounter = 0;

	private static final StringBuilder outputBuffer = new StringBuilder();
	private static final AtomicBoolean serverHasToWaitToPrint = new AtomicBoolean(false);

	public static void main(String[] args) {
		KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
		ConsoleOutput.setConsole(EchoServerCLI::printMessage);
		boolean running = true;

		while (running) {
			printMenu();

			int choice = keyboardHandler.getIntInput(input -> input >= 1 && input <= 4, "Escolha uma opcao: ", "Opcao invalida. Tente novamente.");

			System.out.println("=".repeat(30));

			switch (choice) {
			case 1:
				createServer(keyboardHandler);
				break;
			case 2:
				listServers();
				break;
			case 3:
				stopServer(keyboardHandler);
				break;
			case 4:
				stopAllServers();
				running = false;
				break;
			default:
				System.out.println("Opcao invalida. Tente novamente.");
			}
		}

		keyboardHandler.closeKeyboardEntry();
	}

	private static void printMenu() {
		lockOutput();
		System.out.println("=".repeat(30));
		System.out.println("*Menu:");
		System.out.println("1. Criar novo servidor");
		System.out.println("2. Listar servidores");
		System.out.println("3. Parar servidor");
		System.out.println("4. Sair");
		flushOutput();
	}

	public static void printMessage(String message) {
		if (serverHasToWaitToPrint.get()) {
			appendToBuffer(message);
		} else {
			System.out.println(message);
		}
	}

	private static void appendToBuffer(String message) {
		synchronized (outputBuffer) {
			outputBuffer.append(message).append("\n");
		}
	}

	private static void lockOutput() {
		serverHasToWaitToPrint.set(true);
	}

	private static void flushOutput() {
		System.out.println("=".repeat(30));
		System.out.println("*Output dos servidores:");
		synchronized (outputBuffer) {
			if (outputBuffer.length() > 0) {
				System.out.println(outputBuffer.toString());
				outputBuffer.setLength(0);
			}
		}
		serverHasToWaitToPrint.set(false);
	}

	private static void createServer(KeyboardHandler keyboardHandler) {
		lockOutput();
		int port = keyboardHandler.getIntInput(input -> input > 0 && input <= 65535, "Digite a porta do novo servidor: ", "Porta invalida. Tente novamente.");

		var exampleServer = new EchoServer();
		ServerExecutor serverRunner = new ServerExecutor(port, exampleServer);

		serverCounter++;
		servers.put(serverCounter, serverRunner);
		serverRunner.start();
		System.out.println("Servidor " + serverCounter + " iniciado na porta " + port);
		flushOutput();
	}

	private static void listServers() {
		lockOutput();
		System.out.println("Servidores em execucao:");
		if (servers.isEmpty()) {
			System.out.println("Nenhum servidor em execucao.");
		} else {
			for (var entry : servers.entrySet()) {
				System.out.println("ID: " + entry.getKey() + ", Porta: " + entry.getValue().getPort());
			}
		}
		flushOutput();
	}

	private static void stopServer(KeyboardHandler keyboardHandler) {
		lockOutput();
		int serverId = keyboardHandler.getIntInput(input -> servers.containsKey(input),
				"Digite o ID do servidor para parar: ", "Servidor com ID nao encontrado. Tente novamente.");

		ServerExecutor serverRunner = servers.remove(serverId);
		if (serverRunner != null) {
			serverRunner.stop();
			System.out.println("Servidor " + serverId + " parado.");
		} else {
			System.out.println("Servidor com ID " + serverId + " nao encontrado.");
		}
		flushOutput();
	}

	private static void stopAllServers() {
		lockOutput();
		for (ServerExecutor serverRunner : servers.values()) {
			serverRunner.stop();
		}
		servers.clear();
		System.out.println("Todos os servidores foram parados.");
		flushOutput();
	}
}