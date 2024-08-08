package socketsOperations.clis;

import socketsOperations.communicators.clients.MessageSender;
import socketsOperations.executors.ClientExecutor;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;

public class ClienteMessengerCLI {
    
    private static final String IP_REGEX = "^((\\d{1,3}\\.){3}\\d{1,3})|(localhost)$";
    
    public static void main(String[] args) {
        String ip = "";
        int port;
        int option;
        boolean exit = false;
        
        KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
        ConsoleOutput.setConsole(ClienteMessengerCLI::printServerOutput);

        MessageSender client = new MessageSender();    
        
        ip = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP: ", "[!] IP inválido\n");
        port = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta: ", "[!] Porta inválida!\n");

        ClientExecutor.runClient(ip, port, client);      
        
        while(!exit) {
            printMenu();
            option = keyboardHandler.getIntInput(input -> input >= 1 && input <= 5, "Digite a opcao:\n", "[!] Opcao invalida!\n");
            switch(option) {
            	case 1 -> ip = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP: ", "[!] IP inválido\n");
            	case 2 -> port = keyboardHandler.getIntInput(input -> input>=1 && input<=5, "Por favor, digite a porta: ", "[!] Porta inválida!\n");
            	case 3 -> {
            		String message = keyboardHandler.getInput(input -> !input.isBlank(), "Digite a mensagem: ", "[!!!] Mensagem inválida!\n");
            		client.sendMessage(message);
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
