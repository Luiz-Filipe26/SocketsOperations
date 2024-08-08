package socketsOperations.clis;

import socketsOperations.communicators.clients.RandomNumberSender;
import socketsOperations.executors.ClientExecutor;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;

public class RandomNumberSenderCLI {

    private static final String IP_REGEX = "^((\\d{1,3}\\.){3}\\d{1,3})|(localhost)$";

    public static void main(String[] args) {
        String ip;
        int port;
        int numberOfRandomNumbers;
        
        ConsoleOutput.setConsole(RandomNumberSenderCLI::printText);        
        
        if (args.length < 3) {
            KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
            
            System.out.println("Por favor, forneça o IP (ou 'localhost'), a porta e a quantidade de números aleatórios (isto pode ser feito por argumentos).");
            
            ip = keyboardHandler.getInput(input -> input.matches(IP_REGEX), "Digite o IP: ", "O IP fornecido é inválido. Deve ser um IP válido ou 'localhost'.");
            port = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta: ", "[!] Porta inválida!\n");
            numberOfRandomNumbers = keyboardHandler.getIntInput(input -> input > 0, "Digite a quantidade de números aleatórios a serem enviados: ", "Quantidade de números aleatórios deve ser positivo");
            
            keyboardHandler.closeKeyboardEntry();
        }
        else {
        	ip = args[0];

        	try {
        		port = Integer.parseInt(args[1]);
        		numberOfRandomNumbers = Integer.parseInt(args[2]);
        	} catch (NumberFormatException e) {
        		System.out.println("A porta e a quantidade de números aleatórios devem ser números inteiros.");
        		return;
        	}
        		
        	if(!(numberOfRandomNumbers > 0)) {
        		System.out.println("Quantidadede números aleatórios deve ser positivo.");
        		return;
        	}
        	
        	if(!(port >=0 && port <= 65535)) {
        		System.out.println("[!] Porta inválida!\n");
        		return;
        	}

        	if (!ip.matches(IP_REGEX)) {
        		System.out.println("O IP fornecido é inválido. Deve ser um IP válido ou 'localhost'.");
        		return;
        	}
        }

        RandomNumberSender client = new RandomNumberSender(numberOfRandomNumbers);
        ClientExecutor.runClient(ip, port, client);
    }
    
    public static void printText(String text) {
    	System.out.print(text);
    }
}
