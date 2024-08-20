package p2pmessenger;

import java.net.*;

import socketsOperations.executors.ServerExecutor;
import socketsOperations.utils.ConsoleOutput;
import socketsOperations.utils.KeyboardHandler;

public class PTPServerMessengerCLI {

    public static void main(String[] args) {
        KeyboardHandler keyboardHandler = KeyboardHandler.getInstance();
        ConsoleOutput.setConsole(PTPServerMessengerCLI::printMessage);
        
        System.out.println("=====SERVIDOR DO MESSENGER P2P=====");

        int port = keyboardHandler.getIntInput(input -> input >= 0 && input < 65535, "Por favor, digite a porta do servidor: ", "[!] Porta inválida!\n");

        var messengerServer = new MessengerServer();
        ServerExecutor serverRunner = new ServerExecutor(port, messengerServer);
        serverRunner.start();
        
        String ip = getLocalIP();
        if(ip == null) {
            ip = "não reconhecido";
        }
        
        System.out.println("Servidor iniciado no IP local " + ip + " e na porta " + port);
        
        System.out.println("=====OUTPUT DO SERVIDOR=====");

        keyboardHandler.closeKeyboardEntry();
    }

    public static void printMessage(String message) {
        System.out.println(message);
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
}