package ass2;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

public class Starter {
	public static void main(String[] args) throws SpreadException, UnknownHostException {
		SpreadConnection connection = new SpreadConnection();
		connection.connect(InetAddress.getByName("127.0.0.1"), 4803, "client_name", false, true);

		SpreadGroup group = new SpreadGroup();
		group.join(connection, "group8");

		ClientInterface client = new Client();
		Listener listener = new Listener();

		connection.add(listener);

		int res = client.deposit(500);
		System.out.println(res);

		while (true) {
			double ress = client.getQuickBalance();
			System.out.println(ress);
		}
	}
}
