package socketsOperations.utils;

import java.util.function.Consumer;

public class ConsoleOutput {
	private static Consumer<String> console;
	
	public static void setConsole(Consumer<String> console) {
		ConsoleOutput.console = console;
	}
	
	public static void print(String text) {
		if(console != null) {
			console.accept(text);
		}
	}
	
	public static void println(String text) {
		if(console != null) {
			console.accept(text + System.lineSeparator());
		}
	}
}