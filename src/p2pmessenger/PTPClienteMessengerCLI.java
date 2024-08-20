package p2pmessenger;

import java.net.*;

import socketsOperations.executors.ClientExecutor;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;

public class PTPClienteMessengerCLI {
    
    private static final String IP_REGEX = "^((\\d{1,3}\\.){3}\\d{1,3})|(localhost)$";
    
    public static void main(String[] args) {
        String serverIP;
        String clientIP;
        int port;
        int option;
        boolean exit = false;
        String name;
        
        KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
        ConsoleOutput.setConsole(PTPClienteMessengerCLI::printServerOutput);

        MessengerNodeClient client = new MessengerNodeClient();
        
        clientIP = getLocalIP();
        if(clientIP == null) {
            System.out.println("[!] Erro ao buscar IP local.");
            return;
        }
        
        name = keyboardHandler.getInput(input -> !input.isBlank(), "Por favor, digite o nome: ", "[!] Nome invalido!\n");
        serverIP = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP do servidor: ", "[!] IP invalido\n");
        port = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta do servidor: ", "[!] Porta invalida!\n");
        
        
        ClientExecutor.runClient(serverIP, port, client);      
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            System.out.println("Erro no sleep: " + ex);
        }
        
        client.registryClient(name, serverIP, port);
        
        while(!exit) {
            printMenu();
            option = keyboardHandler.getIntInput(input -> input >= 1 && input <= 5, "Digite a opcao: \n", "[!] Opcao invalida!\n");
            switch(option) {
            	case 1 -> {
                    String recipient = keyboardHandler.getInput(input -> !input.isBlank(), "Digite o destinatario: ", "[!!!] Destinatario invalido!\n");
                    String message = keyboardHandler.getInput(input -> !input.isBlank(), "Digite a mensagem: ", "[!!!] Mensagem invalida!\n");
                    client.sendMessage(recipient, message);
            	}
            	case 2 -> exit = true;
            }
        }
        
        keyboardHandler.closeKeyboardEntry();
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
