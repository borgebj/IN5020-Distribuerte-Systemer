package ass1;

import ass1.client.ClientSimulator;
import ass1.server.ServerSimulator;

public class Starter {
	public static void main(String[] args) throws InterruptedException {
		// Create thread for ServerSimulator
		Thread serverThread = new Thread(() -> {
			try {
				ServerSimulator.main(new String[]{});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});



		// Create thread for ClientSimulator
		Thread clientThread = new Thread(() -> {
			try {
				// Add a delay to give the server some time to start
				Thread.sleep(500);
				ClientSimulator.main(new String[]{});
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// Start both threads
		serverThread.start();
		clientThread.start();

		// Optionally join the threads to wait for them to complete
		serverThread.join();
		clientThread.join();
	}
}
