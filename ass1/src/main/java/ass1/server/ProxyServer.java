package ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


public class ProxyServer implements ProxyClientInterface {
	private int port;
	private HashMap<Integer, Server> servers;


	public ProxyServer(int port) {
		this.port = port;
		this.servers = new HashMap<>();

		startProxy();
	}

	// used to register servers to this proxy - when creating servers
	public void registerServer(int zone, Server server) {
		servers.put(zone, server);
	}

	@Override
	public String requestServer(int zone) throws RemoteException
	{
		int requestedZone = zone;

		zone = (zone % 5) + 1;

		Server server = servers.get(zone);

		System.out.println("Requested server" + requestedZone + ", Found " + server);

		// TODO
		// finn passende server

		return (server.getHost()) + ":" + (server.getPort());
	}

	private void startProxy()
	{
		try {
			// Create registry unique for proxy
			Registry registry = LocateRegistry.createRegistry(port);

			// export proxy to registry
			ProxyClientInterface proxyStub = (ProxyClientInterface) UnicastRemoteObject.exportObject(this, port);

			// define proxy name
			String proxyName = "proxy";

			registry.bind(proxyName, proxyStub);

			System.out.printf("%s:%d has started\n", proxyName, port);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
