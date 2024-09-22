package ass1;

import ass1.client.ClientSimulator;
import ass1.server.ServerSimulator;

import java.util.Arrays;

public class Starter {
	public static void main(String[] args) throws InterruptedException {

		/**
		 * none = No cache
		 * client = Client cache
		 * server = Server Cache*
		 */
		String cacheOption = args[0];
		String[] clientArgs = {"false", "0"};
		String[] serverArgs = {"false", "0"};

		switch (cacheOption) {
			case "none":
				clientArgs[0] = "False";
				serverArgs[0] = "False";
				clientArgs[1] = "0";
				System.out.println("\n[ No cache used ]\n");
				break;
			case "client":
				serverArgs[0] = "False";
				clientArgs[0] = "True";
				clientArgs[1] = "1";
				System.out.println("\n[ Client cache used ]\n");
				break;
			case "server":
				serverArgs[0] = "True";
				clientArgs[0] = "False";
				clientArgs[1] = "2";
				System.out.println("\n[ Server cache used ]\n");
				break;
		}
		Thread.sleep(500);

		// Create thread for ServerSimulator
		Thread serverThread = new Thread(() -> {
			try {
				ServerSimulator.main(serverArgs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// Create thread for ClientSimulator
		Thread clientThread = new Thread(() -> {
			try {
				// Add a delay to give the server some time to start
				Thread.sleep(500);
				ClientSimulator.main(clientArgs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// Start both threads
		serverThread.start();
		clientThread.start();
	}
}
