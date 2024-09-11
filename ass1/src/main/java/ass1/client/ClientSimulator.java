package ass1.client;

import ass1.client.Client;
import ass1.server.InstructionInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientSimulator {
	private static Thread[] clientThreads;

	private static int BASE_PORT = 1099;
	private static String filepath = "ass1/info/exercise_1_input.txt";



	/**
	 * Goes through and parses instructions from a given file to a hashmap, later used by clients
	 *
	 * @return map : filled hashmap with instructions
	 */
	private static ArrayList<InstructionInfo> parseInstructions()
	{
		ArrayList<InstructionInfo> instructions = new ArrayList<>();

		File file = new File(filepath);
		if (!file.exists()) {
			System.err.println("File not found: " + "ass1/info/exercise_1_input.txt");
			return instructions; // Return empty list if file is not found
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" "); // <- | function | arg1 | arg2 | arg3 | zone+


				// parse method name and zone
				String function = parts[0];
				String zonePart = parts[parts.length - 1];
				int zone = Integer.parseInt(zonePart.split(":")[1]); // Extract the number after "Zone:"

				List<String> args = new ArrayList<>();

				// combine all elements between
				StringBuilder currentArgs = new StringBuilder();
				for (int i = 1; i < parts.length - 1; i++) {
					String part = parts[i];

					// if arg is a number
					if (part.matches("\\d+")) {
						if (currentArgs.length() > 0) {
							args.add(currentArgs.toString());
							currentArgs.setLength(0);
						}
						args.add(part);
					}
					// if arg is a string
					else {
						if (currentArgs.length() > 0) {
							currentArgs.append(" ");
						}
						currentArgs.append(part);
					}
				}

				if (currentArgs.length() > 0) {
					args.add(currentArgs.toString());
				}

				InstructionInfo info = new InstructionInfo(function, args, zone);
				instructions.add(info);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return instructions;
	}


	/**
	 * Creates clients which is started within
	 *
	 * @param numClients : how many clients to create
	 * @param port : base-port used
	 * @param instructions : instruction-set that clients use
	 */
	private static void createClients(int numClients, int port, ArrayList<InstructionInfo> instructions)
	{
		clientThreads = new Thread[numClients];

		// Create clients
		for (int i = 0; i < numClients; i++) {
			int clientZone = i;
			int clientPort = port + numClients + i;
			clientThreads[i] = new Thread(() -> new Client(clientZone, clientPort, instructions));
			clientThreads[i].start();
		}

		// join all clients at the end
		for (Thread t : clientThreads) {
			try { t.join(); }
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args)
	{
		// Parse instructions
		ArrayList<InstructionInfo> instructions = parseInstructions();

		// How many clients to run at once
		int numDevices = 5;

		// main port used
		int port = BASE_PORT;

		// start clients
		createClients(numDevices, port, instructions);
	}
}
