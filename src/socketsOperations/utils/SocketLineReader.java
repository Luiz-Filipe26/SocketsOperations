package socketsOperations.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class SocketLineReader implements Runnable {

	private final BufferedReader socketReader;
	private String currentSocketLine;
	private final Map<Object, Integer> priorityByObject = new HashMap<>();
	private final PriorityQueue<Object> readingQueue;
	private final Semaphore waitConsumerSemaphore;

	public SocketLineReader(BufferedReader socketReader) {
		this.socketReader = socketReader;
		this.waitConsumerSemaphore = new Semaphore(0);
		this.readingQueue = new PriorityQueue<>(
				(item1, item2) -> Integer.compare(priorityByObject.get(item2), priorityByObject.get(item1)));
	}

	@Override
	public void run() {
		try {
			while (true) {
				currentSocketLine = socketReader.readLine();
				
				if (currentSocketLine == null) {
					throw new IOException("Conexão fechada");
				}
				
				if(readingQueue.isEmpty()) {
					waitConsumerSemaphore.acquire();
				}
				
				ConsoleOutput.println("Linha atual: " + currentSocketLine);

				Object lock = readingQueue.peek();
				
				System.out.println("Tamanho da fila: " + readingQueue.size());

				if (lock == null) {
					continue;
				}

				synchronized (lock) {
					lock.notifyAll();
				}
				
				ConsoleOutput.println("Notificado.");
			}
		} catch (IOException e) {
			if(!readingQueue.isEmpty()) {
				currentSocketLine = CommunicationConstants.CONNECTION_CLOSED + CommunicationConstants.TYPEANDCONTENTSEPARATOR + "Conexão fechada.";
				notifyAllLocks();
			}
		} catch (InterruptedException e) {
			ConsoleOutput.println("Erro: " + e.getMessage());
		} finally {
			try {
				socketReader.close();
			} catch (IOException e) {
				
			}
		}
	}

	private void notifyAllLocks() {
		for (Object lock : priorityByObject.keySet()) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}

	public void signalFinishedReading(Object lock) {
		priorityByObject.remove(lock);
		readingQueue.remove(lock);
	}

	public String getLine(Object lock, int priority) {
		priorityByObject.put(lock, priority);
		readingQueue.add(lock);
		
		if(waitConsumerSemaphore.hasQueuedThreads()) {
			waitConsumerSemaphore.release();
		}

		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Restore interrupted status
				ConsoleOutput.println("Thread interrompida enquanto esperava pela linha: " + e.getMessage());
			}
		}
		
		ConsoleOutput.println("Obtido a linha: " + currentSocketLine);

		return currentSocketLine;
	}
	
	public String getLine(int priority) {
		Object lock = new Object();
		getLine(lock, priority);
		signalFinishedReading(lock);
		return currentSocketLine;
	}
}