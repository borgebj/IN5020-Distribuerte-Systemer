package ass1;

import ass1.client.ClientSimulator;
import ass1.server.ServerSimulator;

public class Starter {
	public static void main(String[] args) throws InterruptedException {
		String[] simArgs = new String[]{};
		ServerSimulator.main(simArgs);
		Thread.sleep(500);
		ClientSimulator.main(simArgs);
	}
}
