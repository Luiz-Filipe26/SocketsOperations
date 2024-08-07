package socketsOperations.clis;

import java.util.*;
import java.util.function.*;

import socketsOperations.clients.MessageSender;
import socketsOperations.executors.ClientExecutor;

public class ClienteMessengerCLI {
    
    private static final String IP_REGEX = "^((\\d{1,3}\\.){3}\\d{1,3})|(localhost)$";
    private static final Scanner keyboardInput = new Scanner(System.in);
    
    public static void main(String[] args) {
        String ip = "";
        int port;
        int option;
        boolean exit = false;
        
        Predicate<String> validPort = (input) -> {
        	int intInput = Integer.parseInt(input);
        	return intInput >= 0 && intInput < 65535;
        };
        
        Predicate<String> validOption = (input) -> {
        	int intInput = Integer.parseInt(input);
        	return intInput >= 1 && intInput <= 5;
        };

        MessageSender client = new MessageSender(ClienteMessengerCLI::printServerOutput);    
        
        ip = getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP: ", "[!] IP inválido\n");
        
        port = Integer.valueOf(getInput(validPort, "Por favor, digite a porta: ", "[!] Porta inválida!\n"));

        ClientExecutor.runClient(ip, port, client);      
        
        while(!exit) {
            printMenu();
            option = Integer.valueOf(getInput(validOption, "Digite a opcao:\n", "[!] Opcao invalida!\n"));
            switch(option) {
            	case 1 -> ip = getInput(input -> input.matches(IP_REGEX), "Por favor, digite o IP: ", "[!] IP inválido\n");
            	case 2 -> port = Integer.valueOf(getInput(validPort, "Por favor, digite a porta: ", "[!] Porta inválida!\n"));
            	case 3 -> {
            		String message = getInput(input -> !input.isBlank(), "Digite a mensagem: ", "[!!!] Mensagem inválida!\n");
            		client.sendMessage(message);
            	}
            	case 4 -> client.askForAllMessages();
            	case 5 -> exit = true;
            }
            
        }
    }
    
    public static void printServerOutput(String text) {
    	System.out.println(text);
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
    
    public static String getInput(Predicate<String> validitCondition, String entryMessage, String errorMessage) {
    	String input = "";
    	
    	while(true) {
    		try {
    			System.out.print(entryMessage);
    			input = keyboardInput.nextLine();

    			if(!validitCondition.test(input)) {
    				System.out.println(errorMessage);
    			} else {
    				break;
    			}
    		} catch (Exception e) {
				System.out.print(errorMessage);
			}
        }
    	
    	return input;
    }
}
