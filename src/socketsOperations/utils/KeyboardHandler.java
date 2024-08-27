package socketsOperations.utils;

import java.util.Scanner;
import java.util.function.Predicate;

public class KeyboardHandler {

    private static KeyboardHandler keyboardHandler;
    private final Scanner keyboardInput;

    public static synchronized KeyboardHandler getInstance() {
        if (keyboardHandler == null) {
            keyboardHandler = new KeyboardHandler();
        }
        return keyboardHandler;
    }

    private KeyboardHandler() {
        keyboardInput = new Scanner(System.in);
    }

    public void closeKeyboardEntry() {
        keyboardInput.close();
    }

    public int getIntInput(Predicate<Integer> validityCondition, String entryMessage, String errorMessage) {
        int input = 0;
        while (true) {
            try {
                System.out.print(entryMessage);
                input = Integer.valueOf(keyboardInput.nextLine());
                if (!validityCondition.test(input)) {
                    System.out.println(errorMessage);
                } else {
                    break;
                }
            } catch (Exception e) {
                System.out.println(errorMessage);
            }
        }
        return input;
    }

    public String getInput(Predicate<String> validityCondition, String entryMessage, String errorMessage) {
        String input = "";
        while (true) {
            try {
                System.out.print(entryMessage);
                input = keyboardInput.nextLine();
                if (!validityCondition.test(input)) {
                    System.out.println(errorMessage);
                } else {
                    break;
                }
            } catch (Exception e) {
                System.out.println(errorMessage);
            }
        }
        return input;
    }
}
