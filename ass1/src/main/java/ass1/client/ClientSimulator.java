package ass1.client;

import ass1.data.InstructionInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientSimulator {
	private static Thread[] clientThreads;

	private static final int BASE_PORT = 1099;
	private static final String filepath = "info/exercise_1_input.txt";



	/**
	 * Goes through and parses instructions from a given file to a hashmap, later used by clients
	 *
	 * @return map : filled hashmap with instructions
	 */
	private static ArrayList<InstructionInfo> parseInstructions()
	{
		ArrayList<InstructionInfo> instructions = new ArrayList<>();

		InputStream inputStream = ClientSimulator.class.getClassLoader().getResourceAsStream(filepath);
		if (inputStream == null) {
			System.err.printf("File '%s' not found!\n", filepath);
			return instructions; // Return empty list if file is not found
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
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
	 * Creates client which is started within the class
	 *
	 * @param numServers : how many servers are created
	 * @param port : base-port used
	 * @param instructions : instruction-set that clients use
	 */
	private static void createClients(
			int numServers, int port, ArrayList<InstructionInfo> instructions, boolean usingCache,int cacheMode, int lineDelay)
	{
		// Create clients
		int clientPort = port + numServers;
		new Client(clientPort, instructions, usingCache, cacheMode,lineDelay);
	}

	public static void main(String[] args)
	{
		// Parse instructions
		ArrayList<InstructionInfo> instructions = parseInstructions();

		// how many servers are created
		int numServers = 5;

		// main port used
		int port = BASE_PORT;

		// using intenral cache or not
		boolean usingCache = Boolean.parseBoolean(args[0]);

		int cacheMode = Integer.parseInt(args[1]);

		// delay for each request - test cases either T = 20 or T = 50
		int lineDelay = Integer.parseInt(args[2]);

		// start clients
		createClients(numServers, port, instructions, usingCache, cacheMode ,lineDelay);
	}
}
