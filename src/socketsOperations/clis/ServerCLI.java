package socketsOperations.clis;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import socketsOperations.executors.ServerExecutor;
import socketsOperations.servers.EchoServer;

public class ServerCLI {

    private static final Map<Integer, ServerExecutor> servers = new HashMap<>();
    private static int serverCounter = 0;

    private static final StringBuilder outputBuffer = new StringBuilder();
    private static final AtomicBoolean serverHasToWaitToPrint = new AtomicBoolean(false);

    public static void main(String[] args) {
        Scanner keyboardInput = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();

            int choice = keyboardInput.nextInt();
            keyboardInput.nextLine(); //clean buffer

            System.out.println("=".repeat(30));
            
            switch (choice) {
                case 1:
                    createServer(keyboardInput);
                    break;
                case 2:
                    listServers();
                    break;
                case 3:
                    stopServer(keyboardInput);
                    break;
                case 4:
                    stopAllServers();
                    running = false;
                    break;
                default:
                    System.out.println("Opcao invalida. Tente novamente.");
            }
        }

        keyboardInput.close();
    }

    private static void printMenu() {
        lockOutput();
        System.out.println("=".repeat(30));
        System.out.println("*Menu:");
        System.out.println("1. Criar novo servidor");
        System.out.println("2. Listar servidores");
        System.out.println("3. Parar servidor");
        System.out.println("4. Sair");
        System.out.println("Escolha uma opcao");
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

    private static void createServer(Scanner keyboardInput) {
        lockOutput();
        System.out.print("Digite a porta do novo servidor: ");
        int port = keyboardInput.nextInt();
        keyboardInput.nextLine();

        var exampleServer = new EchoServer(ServerCLI::printMessage);
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

    private static void stopServer(Scanner keyboardInput) {
        lockOutput();
        System.out.print("Digite o ID do servidor para parar: ");
        int serverId = keyboardInput.nextInt();
        keyboardInput.nextLine(); // clean buffer

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