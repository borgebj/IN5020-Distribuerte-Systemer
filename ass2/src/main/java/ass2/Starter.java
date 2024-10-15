package ass2;

import spread.SpreadException;
import java.net.UnknownHostException;

public class Starter {
	public static void main(String[] args) {

		int id = 0;
		String filename = null;

		// Check for the required arguments
		try {
			// Ensure ID is provided
			if (args.length < 1) {
				System.err.println("Usage: java Starter <id> [filename]");
				return;
			}
			id = Integer.parseInt(args[0]);

			// Check if filename is provided
			if (args.length > 1) {
				filename = args[1];
			}
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.err.println("Invalid input for ID. Please provide a valid ID.");
			return;
		}

		int numOfReplicas = 1;      // <---------- THIS DECIDES HOW MANY REPLICAS IN USE
		String account = "group8";

		try {
			if (filename != null) {
				new Client("127.0.0.1", account, numOfReplicas, id, filename);
			} else {
				 new Client("127.0.0.1", account, numOfReplicas, id);
			}

		} catch (UnknownHostException | SpreadException e) {
			e.printStackTrace();
		}
	}
}
