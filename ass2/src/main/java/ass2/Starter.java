package ass2;

import spread.SpreadException;
import java.net.UnknownHostException;

public class Starter {
	public static void main(String[] args) {
		int numOfReplicas = 1;
		String account = "group8";

		for (int i = 0; i < numOfReplicas; i++) {
			final int j = i + 1;
			new Thread(() -> {
				try {
					Client client = new Client("127.0.0.72", account, numOfReplicas, j);
					// Additional logic for client actions can be added here
				} catch (UnknownHostException | SpreadException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}
}
