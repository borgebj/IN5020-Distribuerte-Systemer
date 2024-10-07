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
		for (int i = 0; i <3; i++) {
			System.out.println("yahoo");
			Client client = new Client("localhost", "Top5Bombaclats", i);
		}

		
	}
}
