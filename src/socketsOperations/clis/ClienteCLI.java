package socketsOperations.clis;

import socketsOperations.clients.RandomNumberSender;
import socketsOperations.executors.ClientExecutor;

import java.util.*;

public class ClienteCLI {

    private static final String IP_REGEX = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";

    public static void main(String[] args) {
        String ip;
        int port;
        int numberOfRandomNumbers;
        
        if (args.length < 3) {
            Scanner keyboardInput = new Scanner(System.in);
            
            System.out.println("Por favor, forneça o IP (ou 'localhost'), a porta e a quantidade de números aleatórios (isto pode ser feito por argumentos).");
            System.out.print("Digite o IP: ");
            ip = keyboardInput.nextLine();
            try {
                System.out.print("Digite a porta: ");
                port = Integer.valueOf(keyboardInput.nextLine());
            } catch(Exception e) {
                System.err.println("Número de porta digitado errado! Fechando aplicação");
                return;
            }
            try {
                System.out.print("Digite a quantidade de números aleatórios a serem enviados: ");
                numberOfRandomNumbers = Integer.valueOf(keyboardInput.nextLine());
            } catch(Exception e) {
                System.err.println("Número digitado errado! Fechando aplicação");
                return;
            }
            
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
        }

        if (!ip.equals("localhost") && !ip.matches(IP_REGEX)) {
            System.out.println("O IP fornecido é inválido. Deve ser um IP válido ou 'localhost'.");
            return;
        }

        // Criar e passar o número de aleatórios para o RandomNumberClient
        RandomNumberSender client = new RandomNumberSender(numberOfRandomNumbers);
        ClientExecutor.runClient(ip, port, client);
    }
}
