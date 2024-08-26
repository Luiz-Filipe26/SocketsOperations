package socketsOperations.applications.messenger;

import java.util.logging.Level;
import java.util.logging.Logger;

import socketsOperations.executors.ClientExecutor;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;

public class ClienteMessengerCLI {
    
    private static final String IP_REGEX = "^((\\d{1,3}\\.){3}\\d{1,3})|(localhost)$";
    
    public static void main(String[] args) {
        String ip;
        int port;
        int option;
        boolean exit = false;
        String name;
        
        KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
        ConsoleOutput.setConsole(ClienteMessengerCLI::printServerOutput);

        MessengerClient client = new MessengerClient();    
        
        name = keyboardHandler.getInput(input -> !input.isBlank(), "Por favor, digite o nome: ", "[!] Nome invalido!\n");
        ip = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP: ", "[!] IP invalido\n");
        port = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta: ", "[!] Porta invalida!\n");
        
        ClientExecutor.runClient(ip, port, client);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClienteMessengerCLI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        client.registryClient(name);
        
        while(!exit) {
            printMenu();
            option = keyboardHandler.getIntInput(input -> input >= 1 && input <= 5, "Digite a opcao: \n", "[!] Opcao invalida!\n");
            switch(option) {
            	case 1 -> ip = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP: ", "[!] IP invalido\n");
            	case 2 -> port = keyboardHandler.getIntInput(input -> input>=0 && input<=65535, "Por favor, digite a porta: ", "[!] Porta invalida!\n");
            	case 3 -> {
                    String recipient = keyboardHandler.getInput(input -> !input.isBlank(), "Digite o destinatario: ", "[!!!] Destinatario invalido!\n");
            		String message = keyboardHandler.getInput(input -> !input.isBlank(), "Digite a mensagem: ", "[!!!] Mensagem invalida!\n");
            		client.sendMessage(recipient, message);
            	}
            	case 4 -> client.askForAllMessages();
            	case 5 -> exit = true;
            }
        }
        
        keyboardHandler.closeKeyboardEntry();
    }
    
    public static void printServerOutput(String text) {
    	System.out.print(text);
    }
    
    public static void printMenu() {
        System.out.println("+-------Menu de opcoes---------");
        System.out.println("|1. Escolher outro IP");
        System.out.println("|2. Escolher outra porta");
        System.out.println("|3. Mandar mensagem");
        System.out.println("|4. Mostrar mensagens enviadas");
        System.out.println("|5. Sair");
        System.out.println("+------------------------------");
    }
}
