package ass1.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Proxy {
	private Registry registry;
	private int numServers;
	private int port;

	private ServerInterface[] servers;

	public Proxy(int numServers, int port) {
		this.numServers = numServers;
		this.port = port;

		this.servers = new ServerInterface[numServers];
		startProxy();
	}

	private void startProxy()
	{
		// start proxy
	}
}
